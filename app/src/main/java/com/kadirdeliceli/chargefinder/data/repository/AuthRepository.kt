package com.kadirdeliceli.chargefinder.data.repository

import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.GoogleAuthProvider
import com.google.firebase.firestore.FirebaseFirestore
import com.kadirdeliceli.chargefinder.domain.model.User
import kotlinx.coroutines.tasks.await
import com.google.firebase.Timestamp

class AuthRepository(
    private val auth: FirebaseAuth = FirebaseAuth.getInstance(),
    private val firestore: FirebaseFirestore = FirebaseFirestore.getInstance()
) {

    fun getCurrentUserId(): String? = auth.currentUser?.uid

    fun isUserLoggedIn(): Boolean = auth.currentUser != null

    // email ve password kayıt
    suspend fun registerWithEmail(
        email: String,
        password: String,
        displayName: String
    ): Result<User> {
        return try {
            val authResult = auth.createUserWithEmailAndPassword(email, password).await()
            val firebaseUser = authResult.user
                ?: return Result.failure(Exception("Kullanıcı oluşturulamadı"))

            val user = User(
                uid = firebaseUser.uid,
                email = email,
                displayName = displayName,
                createdAt = Timestamp.now()
            )

            saveUserToFirestore(user)

            Result.success(user)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    // email ve password giriş
    suspend fun loginWithEmail(email: String, password: String): Result<User> {
        return try {
            val authResult = auth.signInWithEmailAndPassword(email, password).await()
            val firebaseUser = authResult.user
                ?: return Result.failure(Exception("Giriş başarısız"))

            val user = getUserFromFirestore(firebaseUser.uid)
                ?: User(
                    uid = firebaseUser.uid,
                    email = firebaseUser.email ?: email,
                    displayName = firebaseUser.displayName ?: ""
                )

            Result.success(user)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    // google ile giriş
    suspend fun loginWithGoogle(idToken: String): Result<User> {
        return try {
            val credential = GoogleAuthProvider.getCredential(idToken, null)
            val authResult = auth.signInWithCredential(credential).await()
            val firebaseUser = authResult.user
                ?: return Result.failure(Exception("Google ile giriş başarısız"))

            val existingUser = getUserFromFirestore(firebaseUser.uid)
            val user = existingUser ?: User(
                uid = firebaseUser.uid,
                email = firebaseUser.email ?: "",
                displayName = firebaseUser.displayName ?: "",
                createdAt = Timestamp.now()
            ).also { saveUserToFirestore(it) }

            Result.success(user)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    fun logout() {
        auth.signOut()
    }

    // mevcut kullnıcıyı çek
    suspend fun getCurrentUser(): User? {
        val uid = auth.currentUser?.uid ?: return null
        return getUserFromFirestore(uid)
    }

    // diğer fonksiyonlar

    private suspend fun saveUserToFirestore(user: User) {
        firestore.collection("users")
            .document(user.uid)
            .set(user)
            .await()
    }

    private suspend fun getUserFromFirestore(uid: String): User? {
        return try {
            val document = firestore.collection("users")
                .document(uid)
                .get()
                .await()

            document.toObject(User::class.java)
        } catch (e: Exception) {
            null
        }
    }
}