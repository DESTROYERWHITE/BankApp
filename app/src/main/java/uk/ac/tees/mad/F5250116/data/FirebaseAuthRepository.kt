package uk.ac.tees.mad.F5250116.data

import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.FirebaseFirestoreException

data class FirebaseUserProfile(
    val uid: String,
    val email: String,
    val firstName: String,
    val lastName: String
)

class FirebaseAuthRepository(
    private val auth: FirebaseAuth = FirebaseAuth.getInstance(),
    private val firestore: FirebaseFirestore = FirebaseFirestore.getInstance()
) {

    fun hasAuthenticatedSession(): Boolean = auth.currentUser != null

    fun currentUserId(): String? = auth.currentUser?.uid

    fun registerUser(
        email: String,
        firstName: String,
        lastName: String,
        pin: String,
        onSuccess: (FirebaseUserProfile) -> Unit,
        onError: (String) -> Unit
    ) {
        auth.createUserWithEmailAndPassword(email, pin)
            .addOnSuccessListener { result ->
                val firebaseUser = result.user
                if (firebaseUser == null) {
                    onError("Registration did not complete. Please try again.")
                    return@addOnSuccessListener
                }

                val profile = hashMapOf(
                    "uid" to firebaseUser.uid,
                    "email" to email,
                    "firstName" to firstName,
                    "lastName" to lastName
                )

                firestore.collection(USERS_COLLECTION)
                    .document(firebaseUser.uid)
                    .set(profile)
                    .addOnSuccessListener {
                        onSuccess(
                            FirebaseUserProfile(
                                uid = firebaseUser.uid,
                                email = email,
                                firstName = firstName,
                                lastName = lastName
                            )
                        )
                    }
                    .addOnFailureListener { error ->
                        if (error is FirebaseFirestoreException &&
                            error.code == FirebaseFirestoreException.Code.PERMISSION_DENIED
                        ) {
                            // Allow registration to complete even if Firestore rules are still locked down.
                            onSuccess(
                                FirebaseUserProfile(
                                    uid = firebaseUser.uid,
                                    email = email,
                                    firstName = firstName,
                                    lastName = lastName
                                )
                            )
                        } else {
                            onError(error.message ?: "Profile setup failed.")
                        }
                    }
            }
            .addOnFailureListener { error ->
                onError(error.message ?: "Unable to create your account.")
            }
    }

    fun signIn(
        email: String,
        pin: String,
        onSuccess: (FirebaseUserProfile) -> Unit,
        onError: (String) -> Unit
    ) {
        auth.signInWithEmailAndPassword(email, pin)
            .addOnSuccessListener { result ->
                val firebaseUser = result.user
                if (firebaseUser == null) {
                    onError("Sign in failed. Please try again.")
                    return@addOnSuccessListener
                }

                loadProfile(
                    uid = firebaseUser.uid,
                    fallbackEmail = firebaseUser.email.orEmpty(),
                    onSuccess = onSuccess,
                    onError = onError
                )
            }
            .addOnFailureListener { error ->
                onError(error.message ?: "Unable to sign in with Firebase.")
            }
    }

    fun restoreSession(
        onSuccess: (FirebaseUserProfile) -> Unit,
        onError: (String) -> Unit
    ) {
        val firebaseUser = auth.currentUser
        if (firebaseUser == null) {
            onError("No active Firebase session.")
            return
        }

        loadProfile(
            uid = firebaseUser.uid,
            fallbackEmail = firebaseUser.email.orEmpty(),
            onSuccess = onSuccess,
            onError = onError
        )
    }

    fun signOut() {
        auth.signOut()
    }

    private fun loadProfile(
        uid: String,
        fallbackEmail: String,
        onSuccess: (FirebaseUserProfile) -> Unit,
        onError: (String) -> Unit
    ) {
        firestore.collection(USERS_COLLECTION)
            .document(uid)
            .get()
            .addOnSuccessListener { snapshot ->
                val firstName = snapshot.getString("firstName").orEmpty()
                val lastName = snapshot.getString("lastName").orEmpty()
                val email = snapshot.getString("email") ?: fallbackEmail

                onSuccess(
                    FirebaseUserProfile(
                        uid = uid,
                        email = email,
                        firstName = firstName,
                        lastName = lastName
                    )
                )
            }
            .addOnFailureListener { error ->
                if (error is FirebaseFirestoreException &&
                    error.code == FirebaseFirestoreException.Code.PERMISSION_DENIED
                ) {
                    // Fall back to the Firebase Auth account so login still works.
                    onSuccess(
                        FirebaseUserProfile(
                            uid = uid,
                            email = fallbackEmail,
                            firstName = fallbackEmail.substringBefore("@").replaceFirstChar {
                                if (it.isLowerCase()) it.titlecase() else it.toString()
                            },
                            lastName = ""
                        )
                    )
                } else {
                    onError(error.message ?: "Unable to load your profile.")
                }
            }
    }

    companion object {
        private const val USERS_COLLECTION = "users"
    }
}
