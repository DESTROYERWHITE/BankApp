package uk.ac.tees.mad.F5250116

import android.os.Bundle
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.fragment.app.FragmentActivity
import androidx.lifecycle.viewmodel.compose.viewModel
import uk.ac.tees.mad.F5250116.data.BankPreferencesRepository
import uk.ac.tees.mad.F5250116.data.BiometricCredentialsRepository
import uk.ac.tees.mad.F5250116.data.FirebaseAuthRepository
import uk.ac.tees.mad.F5250116.ui.theme.BankAppTheme

class MainActivity : FragmentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        val preferencesRepository = BankPreferencesRepository(applicationContext)
        val firebaseAuthRepository = FirebaseAuthRepository()
        val biometricCredentialsRepository = BiometricCredentialsRepository(applicationContext)

        setContent {
            BankAppTheme {
                val bankViewModel: BankViewModel = viewModel(
                    factory = BankViewModel.Factory(
                        preferencesRepository,
                        firebaseAuthRepository,
                        biometricCredentialsRepository
                    )
                )
                BankAppRoot(viewModel = bankViewModel)
            }
        }
    }
}
