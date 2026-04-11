package uk.ac.tees.mad.F5250116.data

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.doublePreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.emptyPreferences
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.map
import org.json.JSONArray
import org.json.JSONObject
import java.io.IOException

private val Context.bankDataStore: DataStore<Preferences> by preferencesDataStore(name = "bank_preferences")

data class RecentTransfer(
    val payee: String,
    val amount: Double,
    val reference: String
)

data class BankPreferences(
    val userName: String = "Richard",
    val firstName: String = "Richard",
    val lastName: String = "Oludare",
    val email: String = "",
    val accountNumber: String = "**** 0116",
    val balance: Double = 2450.75,
    val lastTransferPayee: String = "No transfer yet",
    val lastTransferAmount: Double = 0.0,
    val lastTransferReference: String = "Set up your first payment",
    val recentTransfers: List<RecentTransfer> = emptyList(),
    val lastRateDate: String = "No live data yet",
    val usdRate: Double? = null,
    val eurRate: Double? = null,
    val biometricEnabled: Boolean = true
) {
    val fullName: String
        get() = listOf(firstName, lastName).filter { it.isNotBlank() }.joinToString(" ").ifBlank { userName }
}

class BankPreferencesRepository(context: Context) {

    private val dataStore = context.bankDataStore

    fun preferences(userId: String?): Flow<BankPreferences> = dataStore.data
        .catch { exception ->
            if (exception is IOException) {
                emit(emptyPreferences())
            } else {
                throw exception
            }
        }
        .map { prefs ->
            val scopedUserId = userId.orEmpty().ifBlank { "guest" }
            val transfers = decodeTransfers(prefs[stringKey(scopedUserId, RECENT_TRANSFERS)])
            BankPreferences(
                userName = prefs[stringKey(scopedUserId, USER_NAME)] ?: "Richard",
                firstName = prefs[stringKey(scopedUserId, FIRST_NAME)] ?: "Richard",
                lastName = prefs[stringKey(scopedUserId, LAST_NAME)] ?: "Oludare",
                email = prefs[stringKey(scopedUserId, EMAIL)] ?: "",
                accountNumber = prefs[stringKey(scopedUserId, ACCOUNT_NUMBER)] ?: buildAccountNumber(scopedUserId),
                balance = prefs[doubleKey(scopedUserId, BALANCE)] ?: 2450.75,
                lastTransferPayee = prefs[stringKey(scopedUserId, LAST_TRANSFER_PAYEE)] ?: "No transfer yet",
                lastTransferAmount = prefs[doubleKey(scopedUserId, LAST_TRANSFER_AMOUNT)] ?: 0.0,
                lastTransferReference = prefs[stringKey(scopedUserId, LAST_TRANSFER_REFERENCE)] ?: "Set up your first payment",
                recentTransfers = transfers,
                lastRateDate = prefs[stringKey(scopedUserId, LAST_RATE_DATE)] ?: "No live data yet",
                usdRate = prefs[doubleKey(scopedUserId, USD_RATE)],
                eurRate = prefs[doubleKey(scopedUserId, EUR_RATE)],
                biometricEnabled = prefs[booleanKey(scopedUserId, BIOMETRIC_ENABLED)] ?: true
            )
        }

    suspend fun saveProfile(userId: String, firstName: String, lastName: String, email: String) {
        dataStore.edit { prefs ->
            prefs[stringKey(userId, FIRST_NAME)] = firstName
            prefs[stringKey(userId, LAST_NAME)] = lastName
            prefs[stringKey(userId, EMAIL)] = email
            prefs[stringKey(userId, USER_NAME)] = listOf(firstName, lastName)
                .filter { it.isNotBlank() }
                .joinToString(" ")
                .ifBlank { email.substringBefore("@", "Campus User") }

            if (prefs[stringKey(userId, ACCOUNT_NUMBER)] == null) {
                prefs[stringKey(userId, ACCOUNT_NUMBER)] = buildAccountNumber(userId)
            }
        }
    }

    suspend fun saveTransfer(userId: String, payee: String, amount: Double, reference: String) {
        dataStore.edit { prefs ->
            val balanceKey = doubleKey(userId, BALANCE)
            val currentBalance = prefs[balanceKey] ?: 2450.75
            val newTransfer = RecentTransfer(payee = payee, amount = amount, reference = reference)
            val updatedTransfers = listOf(newTransfer) + decodeTransfers(prefs[stringKey(userId, RECENT_TRANSFERS)])

            prefs[balanceKey] = (currentBalance - amount).coerceAtLeast(0.0)
            prefs[stringKey(userId, LAST_TRANSFER_PAYEE)] = payee
            prefs[doubleKey(userId, LAST_TRANSFER_AMOUNT)] = amount
            prefs[stringKey(userId, LAST_TRANSFER_REFERENCE)] = reference
            prefs[stringKey(userId, RECENT_TRANSFERS)] = encodeTransfers(updatedTransfers.take(MAX_RECENT_TRANSFERS))
        }
    }

    suspend fun saveRates(userId: String, date: String, usdRate: Double, eurRate: Double) {
        dataStore.edit { prefs ->
            prefs[stringKey(userId, LAST_RATE_DATE)] = date
            prefs[doubleKey(userId, USD_RATE)] = usdRate
            prefs[doubleKey(userId, EUR_RATE)] = eurRate
        }
    }

    suspend fun setBiometricEnabled(userId: String, enabled: Boolean) {
        dataStore.edit { prefs ->
            prefs[booleanKey(userId, BIOMETRIC_ENABLED)] = enabled
        }
    }

    private fun encodeTransfers(transfers: List<RecentTransfer>): String {
        val json = JSONArray()
        transfers.forEach { transfer ->
            json.put(
                JSONObject()
                    .put("payee", transfer.payee)
                    .put("amount", transfer.amount)
                    .put("reference", transfer.reference)
            )
        }
        return json.toString()
    }

    private fun decodeTransfers(serialized: String?): List<RecentTransfer> {
        if (serialized.isNullOrBlank()) return emptyList()
        return runCatching {
            val json = JSONArray(serialized)
            buildList {
                for (index in 0 until json.length()) {
                    val item = json.getJSONObject(index)
                    add(
                        RecentTransfer(
                            payee = item.optString("payee"),
                            amount = item.optDouble("amount"),
                            reference = item.optString("reference")
                        )
                    )
                }
            }
        }.getOrDefault(emptyList())
    }

    private fun stringKey(userId: String, suffix: String) = stringPreferencesKey("${userId}_$suffix")
    private fun doubleKey(userId: String, suffix: String) = doublePreferencesKey("${userId}_$suffix")
    private fun booleanKey(userId: String, suffix: String) = booleanPreferencesKey("${userId}_$suffix")

    private fun buildAccountNumber(userId: String): String {
        val digits = userId.filter(Char::isDigit).takeLast(4).padStart(4, '0')
        return "**** $digits"
    }

    companion object {
        private const val USER_NAME = "user_name"
        private const val FIRST_NAME = "first_name"
        private const val LAST_NAME = "last_name"
        private const val EMAIL = "email"
        private const val ACCOUNT_NUMBER = "account_number"
        private const val BALANCE = "balance"
        private const val LAST_TRANSFER_PAYEE = "last_transfer_payee"
        private const val LAST_TRANSFER_AMOUNT = "last_transfer_amount"
        private const val LAST_TRANSFER_REFERENCE = "last_transfer_reference"
        private const val RECENT_TRANSFERS = "recent_transfers"
        private const val LAST_RATE_DATE = "last_rate_date"
        private const val USD_RATE = "usd_rate"
        private const val EUR_RATE = "eur_rate"
        private const val BIOMETRIC_ENABLED = "biometric_enabled"
        private const val MAX_RECENT_TRANSFERS = 5
    }
}
