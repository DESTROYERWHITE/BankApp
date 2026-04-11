package uk.ac.tees.mad.F5250116

import androidx.biometric.BiometricManager
import androidx.biometric.BiometricPrompt
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Logout
import androidx.compose.material.icons.filled.AccountBalance
import androidx.compose.material.icons.filled.CreditCard
import androidx.compose.material.icons.filled.Email
import androidx.compose.material.icons.filled.ExpandLess
import androidx.compose.material.icons.filled.ExpandMore
import androidx.compose.material.icons.filled.Fingerprint
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Payments
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Shield
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.fragment.app.FragmentActivity
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import kotlinx.coroutines.delay
import uk.ac.tees.mad.F5250116.data.RecentTransfer
import java.util.concurrent.Executor

private object Destinations {
    const val Splash = "splash"
    const val Auth = "auth"
    const val Dashboard = "dashboard"
    const val Transfer = "transfer"
}

@Composable
fun BankAppRoot(viewModel: BankViewModel) {
    val navController = rememberNavController()
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val snackbarHostState = remember { SnackbarHostState() }
    var snackbarMessage by remember { mutableStateOf<String?>(null) }

    snackbarMessage?.let { message ->
        LaunchedEffect(message) {
            snackbarHostState.showSnackbar(message)
            snackbarMessage = null
        }
    }

    NavHost(navController = navController, startDestination = Destinations.Splash) {
        composable(Destinations.Splash) {
            SplashScreen(uiState.preferences.fullName, uiState.isAuthLoading) {
                navController.navigate(if (uiState.isLoggedIn) Destinations.Dashboard else Destinations.Auth) {
                    popUpTo(Destinations.Splash) { inclusive = true }
                }
            }
        }

        composable(Destinations.Auth) {
            AuthScreen(
                biometricEnabled = uiState.preferences.biometricEnabled,
                snackbarHostState = snackbarHostState,
                onLogin = { email, pin ->
                    viewModel.login(
                        email = email,
                        pin = pin,
                        onSuccess = {
                            navController.navigate(Destinations.Dashboard) {
                                popUpTo(Destinations.Auth) { inclusive = true }
                            }
                        },
                        onError = { snackbarMessage = it }
                    )
                },
                onRegister = { email, firstName, lastName, pin, confirmPin ->
                    viewModel.register(
                        email = email,
                        firstName = firstName,
                        lastName = lastName,
                        pin = pin,
                        confirmPin = confirmPin,
                        onSuccess = {
                            navController.navigate(Destinations.Dashboard) {
                                popUpTo(Destinations.Auth) { inclusive = true }
                            }
                        },
                        onError = { snackbarMessage = it }
                    )
                },
                onBiometricLogin = {
                    viewModel.loginWithBiometrics(
                        onSuccess = {
                            navController.navigate(Destinations.Dashboard) {
                                popUpTo(Destinations.Auth) { inclusive = true }
                            }
                        },
                        onError = { snackbarMessage = it }
                    )
                },
                onToggleBiometric = viewModel::toggleBiometrics
            )
        }

        composable(Destinations.Dashboard) {
            DashboardScreen(
                uiState = uiState,
                formatCurrency = viewModel::formatCurrency,
                onRefreshRates = viewModel::refreshRates,
                onTransferClick = { navController.navigate(Destinations.Transfer) },
                onLogout = {
                    viewModel.logout()
                    navController.navigate(Destinations.Auth) {
                        popUpTo(Destinations.Dashboard) { inclusive = true }
                    }
                }
            )
        }

        composable(Destinations.Transfer) {
            TransferScreen(
                balance = viewModel.formatCurrency(uiState.preferences.balance),
                formatCurrency = viewModel::formatCurrency,
                recentTransfers = uiState.preferences.recentTransfers,
                onBack = { navController.popBackStack() },
                onSubmit = { payee, amount, reference, onSuccess ->
                    viewModel.submitTransfer(
                        payee = payee,
                        amountText = amount,
                        reference = reference,
                        onSuccess = onSuccess,
                        onError = { snackbarMessage = it }
                    )
                },
                snackbarHostState = snackbarHostState
            )
        }
    }
}

@Composable
private fun SplashScreen(userName: String, isAuthLoading: Boolean, onFinished: () -> Unit) {
    LaunchedEffect(isAuthLoading) {
        if (!isAuthLoading) {
            delay(1200)
            onFinished()
        }
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Brush.verticalGradient(listOf(Color(0xFF042A2B), Color(0xFF0B6E4F)))),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Icon(Icons.Default.AccountBalance, null, tint = Color.White, modifier = Modifier.height(64.dp))
            Text("Campus Bank", style = MaterialTheme.typography.headlineLarge, color = Color.White, fontWeight = FontWeight.Bold)
            Text("Secure student banking for $userName", color = Color(0xFFDCEFE8))
            if (isAuthLoading) CircularProgressIndicator(color = Color.White)
        }
    }
}

@Composable
private fun AuthScreen(
    biometricEnabled: Boolean,
    snackbarHostState: SnackbarHostState,
    onLogin: (String, String) -> Unit,
    onRegister: (String, String, String, String, String) -> Unit,
    onBiometricLogin: () -> Unit,
    onToggleBiometric: (Boolean) -> Unit
) {
    var loginEmail by rememberSaveable { mutableStateOf("") }
    var loginPin by rememberSaveable { mutableStateOf("") }
    var registerExpanded by rememberSaveable { mutableStateOf(false) }
    var registerEmail by rememberSaveable { mutableStateOf("") }
    var firstName by rememberSaveable { mutableStateOf("") }
    var lastName by rememberSaveable { mutableStateOf("") }
    var registerPin by rememberSaveable { mutableStateOf("") }
    var confirmPin by rememberSaveable { mutableStateOf("") }
    val context = LocalContext.current
    val activity = context as? FragmentActivity
    val biometricReady = remember {
        activity != null && BiometricManager.from(context).canAuthenticate(BiometricManager.Authenticators.BIOMETRIC_WEAK) ==
            BiometricManager.BIOMETRIC_SUCCESS
    }

    Scaffold(snackbarHost = { SnackbarHost(snackbarHostState) }) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(24.dp)
                .verticalScroll(rememberScrollState()),
            verticalArrangement = Arrangement.Center
        ) {
            AuthHeader("Authentication", "Sign in here, or expand the registration section to create a new account.")
            Spacer(Modifier.height(24.dp))
            AppField(loginEmail, { loginEmail = it }, "Email", Icons.Default.Email)
            Spacer(Modifier.height(16.dp))
            PinField(loginPin, { loginPin = it.take(6) }, "PIN")
            Spacer(Modifier.height(20.dp))
            Button(onClick = { onLogin(loginEmail, loginPin) }, modifier = Modifier.fillMaxWidth()) {
                Text("Sign in")
            }
            Spacer(Modifier.height(10.dp))
            Row(Modifier.fillMaxWidth(), Arrangement.SpaceBetween, Alignment.CenterVertically) {
                Text("Enable biometric login")
                Switch(checked = biometricEnabled, onCheckedChange = onToggleBiometric)
            }
            if (biometricEnabled && biometricReady) {
                TextButton(
                    onClick = { if (activity != null) showBiometricPrompt(activity, onBiometricLogin) },
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Icon(Icons.Default.Fingerprint, null)
                    Spacer(Modifier.width(8.dp))
                    Text("Use fingerprint")
                }
            }

            Spacer(Modifier.height(24.dp))
            Card(shape = RoundedCornerShape(24.dp)) {
                Column(Modifier.padding(20.dp)) {
                    TextButton(
                        onClick = { registerExpanded = !registerExpanded },
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text("Create a new account", modifier = Modifier.weight(1f), textAlign = TextAlign.Start)
                        Icon(
                            imageVector = if (registerExpanded) Icons.Default.ExpandLess else Icons.Default.ExpandMore,
                            contentDescription = null
                        )
                    }
                    AnimatedVisibility(visible = registerExpanded) {
                        Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
                            Text(
                                "Registration stays on this screen so your coursework auth flow is one screen.",
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                            AppField(registerEmail, { registerEmail = it }, "Email", Icons.Default.Email)
                            AppField(firstName, { firstName = it }, "First name", Icons.Default.Person)
                            AppField(lastName, { lastName = it }, "Last name", Icons.Default.Person)
                            PinField(registerPin, { registerPin = it.take(6) }, "6-digit PIN")
                            PinField(confirmPin, { confirmPin = it.take(6) }, "Confirm PIN")
                            Button(
                                onClick = { onRegister(registerEmail, firstName, lastName, registerPin, confirmPin) },
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Text("Register")
                            }
                        }
                    }
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun DashboardScreen(
    uiState: BankUiState,
    formatCurrency: (Double) -> String,
    onRefreshRates: () -> Unit,
    onTransferClick: () -> Unit,
    onLogout: () -> Unit
) {
    val prefs = uiState.preferences
    val exchangeRates = uiState.exchangeRates

    LaunchedEffect(Unit) {
        if (exchangeRates.usdRate == null || exchangeRates.eurRate == null) onRefreshRates()
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Campus Bank") },
                actions = { IconButton(onClick = onLogout) { Icon(Icons.AutoMirrored.Filled.Logout, "Log out") } }
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(16.dp)
                .verticalScroll(rememberScrollState()),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Card(colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primary), shape = RoundedCornerShape(24.dp)) {
                Column(Modifier.padding(20.dp)) {
                    Text("Current account", color = MaterialTheme.colorScheme.onPrimary.copy(alpha = 0.84f))
                    Spacer(Modifier.height(12.dp))
                    Text(formatCurrency(prefs.balance), style = MaterialTheme.typography.headlineLarge, color = MaterialTheme.colorScheme.onPrimary, fontWeight = FontWeight.Bold)
                    Spacer(Modifier.height(8.dp))
                    Text("${prefs.fullName} | ${prefs.accountNumber}", color = MaterialTheme.colorScheme.onPrimary)
                    if (prefs.email.isNotBlank()) Text(prefs.email, color = MaterialTheme.colorScheme.onPrimary.copy(alpha = 0.9f))
                }
            }

            QuickActionCard("Make a transfer", "Send funds and show the receipt on the same screen.", Icons.Default.Payments, "Transfer now", onTransferClick)

            Card(shape = RoundedCornerShape(24.dp)) {
                Column(Modifier.padding(20.dp)) {
                    Row(Modifier.fillMaxWidth(), Arrangement.SpaceBetween, Alignment.CenterVertically) {
                        Column {
                            Text("Exchange rates", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.SemiBold)
                            Text("Live HTTPS data, cached locally", color = MaterialTheme.colorScheme.onSurfaceVariant)
                        }
                        IconButton(onClick = onRefreshRates) { Icon(Icons.Default.Refresh, "Refresh rates") }
                    }
                    Spacer(Modifier.height(12.dp))
                    if (exchangeRates.isLoading) {
                        CircularProgressIndicator()
                    } else {
                        ExchangeRateRow("GBP to USD", exchangeRates.usdRate)
                        HorizontalDivider(Modifier.padding(vertical = 10.dp))
                        ExchangeRateRow("GBP to EUR", exchangeRates.eurRate)
                        Spacer(Modifier.height(8.dp))
                        Text("Last updated: ${exchangeRates.lastUpdated}", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        exchangeRates.errorMessage?.let { Text(it, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.error) }
                    }
                }
            }

            Card(shape = RoundedCornerShape(24.dp)) {
                RecentTransfersCard(
                    transfers = prefs.recentTransfers,
                    formatCurrency = formatCurrency
                )
            }

            Card(shape = RoundedCornerShape(24.dp)) {
                Column(Modifier.padding(20.dp)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Default.Shield, null)
                        Spacer(Modifier.width(8.dp))
                        Text("Security notes", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.SemiBold)
                    }
                    Spacer(Modifier.height(12.dp))
                    Text("Firebase Authentication protects email sign-in, and Cloud Firestore stores the registration profile.")
                    Spacer(Modifier.height(8.dp))
                    Text("The 6-digit PIN is used for prototype authentication and is not duplicated into Firestore profile data.")
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun TransferScreen(
    balance: String,
    formatCurrency: (Double) -> String,
    recentTransfers: List<RecentTransfer>,
    onBack: () -> Unit,
    onSubmit: (String, String, String, (TransferReceipt) -> Unit) -> Unit,
    snackbarHostState: SnackbarHostState
) {
    var payee by rememberSaveable { mutableStateOf("") }
    var amount by rememberSaveable { mutableStateOf("") }
    var reference by rememberSaveable { mutableStateOf("") }
    var localReceiptHistory by remember(recentTransfers) { mutableStateOf(recentTransfers.take(5)) }

    Scaffold(
        topBar = { SimpleTopBar("Transfer", onBack) },
        snackbarHost = { SnackbarHost(snackbarHostState) }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(16.dp)
                .verticalScroll(rememberScrollState()),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Text("Available balance: $balance", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.SemiBold)
            AppField(payee, { payee = it }, "Payee name", Icons.Default.CreditCard)
            OutlinedTextField(amount, { amount = it }, label = { Text("Amount (GBP)") }, modifier = Modifier.fillMaxWidth(), singleLine = true)
            OutlinedTextField(reference, { reference = it }, label = { Text("Payment reference") }, modifier = Modifier.fillMaxWidth(), singleLine = true)
            Button(
                onClick = {
                    onSubmit(payee, amount, reference) {
                        localReceiptHistory = (listOf(
                            RecentTransfer(
                                payee = it.payee,
                                amount = it.amount,
                                reference = it.reference
                            )
                        ) + localReceiptHistory).distinctBy { receipt ->
                            "${receipt.payee}|${receipt.amount}|${receipt.reference}"
                        }.take(5)
                        payee = ""
                        amount = ""
                        reference = ""
                    }
                },
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("Complete transfer")
            }

            if (localReceiptHistory.isNotEmpty()) {
                ReceiptHistoryCard(
                    receipts = localReceiptHistory,
                    formatCurrency = formatCurrency
                )
            }
        }
    }
}

@Composable
private fun RecentTransfersCard(
    transfers: List<RecentTransfer>,
    formatCurrency: (Double) -> String
) {
    var expanded by rememberSaveable { mutableStateOf(false) }
    Card(shape = RoundedCornerShape(24.dp)) {
        Column(Modifier.padding(20.dp)) {
            TextButton(
                onClick = { expanded = !expanded },
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("Recent transfers", modifier = Modifier.weight(1f), textAlign = TextAlign.Start)
                Icon(
                    imageVector = if (expanded) Icons.Default.ExpandLess else Icons.Default.ExpandMore,
                    contentDescription = null
                )
            }
            AnimatedVisibility(visible = expanded) {
                Column {
                    if (transfers.isEmpty()) {
                        Text("No transfers yet.", color = MaterialTheme.colorScheme.onSurfaceVariant)
                    } else {
                        transfers.take(5).forEachIndexed { index, transfer ->
                            TransferSummaryItem(
                                transfer = transfer,
                                formatCurrency = formatCurrency
                            )
                            if (index < transfers.take(5).lastIndex) {
                                HorizontalDivider(Modifier.padding(vertical = 8.dp))
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun ReceiptHistoryCard(
    receipts: List<RecentTransfer>,
    formatCurrency: (Double) -> String
) {
    Card(
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.secondaryContainer)
    ) {
        Column(Modifier.padding(20.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(Icons.Default.Shield, null, tint = MaterialTheme.colorScheme.primary)
                Spacer(Modifier.width(8.dp))
                Text("Transfer complete", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
            }
            Spacer(Modifier.height(16.dp))
            Text("Recent receipts", style = MaterialTheme.typography.titleSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
            Spacer(Modifier.height(12.dp))
            receipts.take(5).forEachIndexed { index, receipt ->
                TransferSummaryItem(
                    transfer = receipt,
                    formatCurrency = formatCurrency
                )
                if (index < receipts.take(5).lastIndex) {
                    HorizontalDivider(Modifier.padding(vertical = 8.dp))
                }
            }
        }
    }
}

@Composable
private fun TransferSummaryItem(
    transfer: RecentTransfer,
    formatCurrency: (Double) -> String
) {
    SummaryRow("Payee", transfer.payee)
    SummaryRow("Amount", formatCurrency(transfer.amount))
    SummaryRow("Reference", transfer.reference)
}

@Composable
private fun AuthHeader(title: String, subtitle: String) {
    Text(title, style = MaterialTheme.typography.headlineMedium, fontWeight = FontWeight.Bold)
    Spacer(Modifier.height(8.dp))
    Text(subtitle, style = MaterialTheme.typography.bodyLarge, color = MaterialTheme.colorScheme.onSurfaceVariant)
}

@Composable
private fun AppField(value: String, onValueChange: (String) -> Unit, label: String, icon: ImageVector) {
    OutlinedTextField(
        value = value,
        onValueChange = onValueChange,
        label = { Text(label) },
        modifier = Modifier.fillMaxWidth(),
        singleLine = true,
        trailingIcon = { Icon(icon, null) }
    )
}

@Composable
private fun PinField(value: String, onValueChange: (String) -> Unit, label: String) {
    OutlinedTextField(
        value = value,
        onValueChange = { onValueChange(it.filter(Char::isDigit)) },
        label = { Text(label) },
        modifier = Modifier.fillMaxWidth(),
        singleLine = true,
        visualTransformation = PasswordVisualTransformation(),
        trailingIcon = { Icon(Icons.Default.Lock, null) }
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun SimpleTopBar(title: String, onBack: () -> Unit) {
    TopAppBar(
        title = { Text(title) },
        navigationIcon = { TextButton(onClick = onBack) { Text("Back") } }
    )
}

@Composable
private fun QuickActionCard(title: String, subtitle: String, icon: ImageVector, buttonText: String, onClick: () -> Unit) {
    Card(shape = RoundedCornerShape(24.dp)) {
        Row(Modifier.fillMaxWidth().padding(20.dp), Arrangement.SpaceBetween, Alignment.CenterVertically) {
            Column(Modifier.weight(1f)) {
                Text(title, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.SemiBold)
                Spacer(Modifier.height(8.dp))
                Text(subtitle, color = MaterialTheme.colorScheme.onSurfaceVariant)
                Spacer(Modifier.height(12.dp))
                Button(onClick = onClick) { Text(buttonText) }
            }
            Spacer(Modifier.width(8.dp))
            Icon(icon, null, tint = MaterialTheme.colorScheme.primary)
        }
    }
}

@Composable
private fun ExchangeRateRow(label: String, rate: Double?) {
    Row(Modifier.fillMaxWidth(), Arrangement.SpaceBetween) {
        Text(label)
        Text(rate?.let { String.format("%.3f", it) } ?: "Unavailable")
    }
}

@Composable
private fun SummaryRow(label: String, value: String) {
    Row(Modifier.fillMaxWidth(), Arrangement.SpaceBetween) {
        Text(label, color = MaterialTheme.colorScheme.onSurfaceVariant)
        Text(value, fontWeight = FontWeight.Medium)
    }
    Spacer(Modifier.height(10.dp))
}

private fun showBiometricPrompt(activity: FragmentActivity, onSuccess: () -> Unit) {
    val prompt = BiometricPrompt(
        activity,
        activity.mainExecutor as Executor,
        object : BiometricPrompt.AuthenticationCallback() {
            override fun onAuthenticationSucceeded(result: BiometricPrompt.AuthenticationResult) {
                onSuccess()
            }
        }
    )

    val promptInfo = BiometricPrompt.PromptInfo.Builder()
        .setTitle("Biometric sign in")
        .setSubtitle("Use fingerprint or face unlock to access Campus Bank")
        .setNegativeButtonText("Cancel")
        .build()

    prompt.authenticate(promptInfo)
}
