package uk.ac.tees.mad.F5250116

import android.util.Patterns
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import uk.ac.tees.mad.F5250116.data.BankPreferences
import uk.ac.tees.mad.F5250116.data.BankPreferencesRepository
import uk.ac.tees.mad.F5250116.data.ExchangeRateApi
import uk.ac.tees.mad.F5250116.data.FirebaseAuthRepository
import uk.ac.tees.mad.F5250116.data.FirebaseUserProfile
import java.text.NumberFormat
import java.util.Locale

data class TransferReceipt(
    val payee: String = "",
    val amount: Double = 0.0,
    val reference: String = ""
)

data class ExchangeRateState(
    val isLoading: Boolean = false,
    val lastUpdated: String = "No live data yet",
    val usdRate: Double? = null,
    val eurRate: Double? = null,
    val errorMessage: String? = null
)

data class BankUiState(
    val preferences: BankPreferences = BankPreferences(),
    val isLoggedIn: Boolean = false,
    val isAuthLoading: Boolean = true,
    val exchangeRates: ExchangeRateState = ExchangeRateState()
)

@OptIn(ExperimentalCoroutinesApi::class)
class BankViewModel(
    private val preferencesRepository: BankPreferencesRepository,
    private val firebaseAuthRepository: FirebaseAuthRepository
) : ViewModel() {

    private val isLoggedIn = MutableStateFlow(false)
    private val isAuthLoading = MutableStateFlow(true)
    private val exchangeState = MutableStateFlow(ExchangeRateState())
    private val activeUserId = MutableStateFlow<String?>(null)

    private val preferencesFlow = activeUserId.flatMapLatest { userId ->
        preferencesRepository.preferences(userId)
    }

    val uiState: StateFlow<BankUiState> = combine(
        preferencesFlow,
        isLoggedIn,
        isAuthLoading,
        exchangeState
    ) { preferences, loggedIn, authLoading, exchange ->
        BankUiState(
            preferences = preferences,
            isLoggedIn = loggedIn,
            isAuthLoading = authLoading,
            exchangeRates = exchange.copy(
                lastUpdated = if (exchange.lastUpdated == "No live data yet") preferences.lastRateDate else exchange.lastUpdated,
                usdRate = exchange.usdRate ?: preferences.usdRate,
                eurRate = exchange.eurRate ?: preferences.eurRate
            )
        )
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5_000),
        initialValue = BankUiState()
    )

    init {
        restoreSession()
    }

    fun login(email: String, pin: String, onSuccess: () -> Unit, onError: (String) -> Unit) {
        when {
            email.isBlank() -> {
                onError("Enter your email address.")
                return
            }
            !Patterns.EMAIL_ADDRESS.matcher(email).matches() -> {
                onError("Enter a valid email address.")
                return
            }
            !isValidPin(pin) -> {
                onError("Use your 6-digit PIN to sign in.")
                return
            }
        }

        isAuthLoading.value = true
        firebaseAuthRepository.signIn(
            email = email.trim(),
            pin = pin,
            onSuccess = { profile ->
                viewModelScope.launch {
                    syncProfile(profile)
                    activeUserId.value = profile.uid
                    isLoggedIn.value = true
                    isAuthLoading.value = false
                    onSuccess()
                }
            },
            onError = { message ->
                isAuthLoading.value = false
                onError(message)
            }
        )
    }

    fun register(
        email: String,
        firstName: String,
        lastName: String,
        pin: String,
        confirmPin: String,
        onSuccess: () -> Unit,
        onError: (String) -> Unit
    ) {
        when {
            email.isBlank() -> onError("Enter your email address.")
            !Patterns.EMAIL_ADDRESS.matcher(email).matches() -> onError("Enter a valid email address.")
            firstName.isBlank() -> onError("Enter your first name.")
            lastName.isBlank() -> onError("Enter your last name.")
            !isValidPin(pin) -> onError("Create a 6-digit PIN for Firebase sign in.")
            pin != confirmPin -> onError("PIN values do not match.")
            else -> {
                isAuthLoading.value = true
                firebaseAuthRepository.registerUser(
                    email = email.trim(),
                    firstName = firstName.trim(),
                    lastName = lastName.trim(),
                    pin = pin,
                    onSuccess = { profile ->
                        viewModelScope.launch {
                            syncProfile(profile)
                            activeUserId.value = profile.uid
                            isLoggedIn.value = true
                            isAuthLoading.value = false
                            onSuccess()
                        }
                    },
                    onError = { message ->
                        isAuthLoading.value = false
                        onError(message)
                    }
                )
            }
        }
    }

    fun loginWithBiometrics(onSuccess: () -> Unit, onError: (String) -> Unit) {
        if (!firebaseAuthRepository.hasAuthenticatedSession()) {
            onError("Register or sign in once before using biometric login.")
            return
        }

        val currentUserId = firebaseAuthRepository.currentUserId()
        if (currentUserId == null) {
            onError("No Firebase user session found for biometric login.")
            return
        }

        activeUserId.value = currentUserId
        isLoggedIn.value = true
        onSuccess()
    }

    fun logout() {
        firebaseAuthRepository.signOut()
        activeUserId.value = null
        isLoggedIn.value = false
        exchangeState.value = ExchangeRateState()
    }

    fun toggleBiometrics(enabled: Boolean) {
        val userId = activeUserId.value ?: return
        viewModelScope.launch {
            preferencesRepository.setBiometricEnabled(userId, enabled)
        }
    }

    fun refreshRates() {
        val userId = activeUserId.value ?: return
        viewModelScope.launch {
            exchangeState.value = exchangeState.value.copy(isLoading = true, errorMessage = null)
            runCatching {
                ExchangeRateApi.service.getLatestRates(from = "GBP", to = "USD,EUR")
            }.onSuccess { response ->
                val usd = response.rates["USD"]
                val eur = response.rates["EUR"]
                if (usd != null && eur != null) {
                    preferencesRepository.saveRates(userId, response.date, usd, eur)
                    exchangeState.value = ExchangeRateState(
                        isLoading = false,
                        lastUpdated = response.date,
                        usdRate = usd,
                        eurRate = eur,
                        errorMessage = null
                    )
                } else {
                    exchangeState.value = exchangeState.value.copy(
                        isLoading = false,
                        errorMessage = "Rate data is incomplete right now."
                    )
                }
            }.onFailure {
                exchangeState.value = exchangeState.value.copy(
                    isLoading = false,
                    errorMessage = "Unable to refresh rates. Cached values are shown when available."
                )
            }
        }
    }

    fun submitTransfer(
        payee: String,
        amountText: String,
        reference: String,
        onSuccess: (TransferReceipt) -> Unit,
        onError: (String) -> Unit
    ) {
        val userId = activeUserId.value ?: run {
            onError("No active user account found.")
            return
        }
        val amount = amountText.toDoubleOrNull()
        val currentBalance = uiState.value.preferences.balance

        when {
            payee.isBlank() -> onError("Enter a payee name.")
            amount == null || amount <= 0.0 -> onError("Enter a valid transfer amount.")
            amount > currentBalance -> onError("Transfer amount exceeds the available balance.")
            reference.isBlank() -> onError("Enter a payment reference.")
            else -> {
                viewModelScope.launch {
                    preferencesRepository.saveTransfer(userId, payee.trim(), amount, reference.trim())
                    onSuccess(TransferReceipt(payee.trim(), amount, reference.trim()))
                }
            }
        }
    }

    fun formatCurrency(value: Double): String {
        return NumberFormat.getCurrencyInstance(Locale.UK).format(value)
    }

    private fun restoreSession() {
        if (!firebaseAuthRepository.hasAuthenticatedSession()) {
            isAuthLoading.value = false
            return
        }

        firebaseAuthRepository.restoreSession(
            onSuccess = { profile ->
                viewModelScope.launch {
                    syncProfile(profile)
                    activeUserId.value = profile.uid
                    isLoggedIn.value = true
                    isAuthLoading.value = false
                }
            },
            onError = {
                activeUserId.value = null
                isLoggedIn.value = false
                isAuthLoading.value = false
            }
        )
    }

    private suspend fun syncProfile(profile: FirebaseUserProfile) {
        preferencesRepository.saveProfile(
            userId = profile.uid,
            firstName = profile.firstName,
            lastName = profile.lastName,
            email = profile.email
        )
    }

    private fun isValidPin(pin: String): Boolean {
        return pin.length == 6 && pin.all(Char::isDigit)
    }

    class Factory(
        private val preferencesRepository: BankPreferencesRepository,
        private val firebaseAuthRepository: FirebaseAuthRepository
    ) : ViewModelProvider.Factory {
        @Suppress("UNCHECKED_CAST")
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            return BankViewModel(preferencesRepository, firebaseAuthRepository) as T
        }
    }
}
