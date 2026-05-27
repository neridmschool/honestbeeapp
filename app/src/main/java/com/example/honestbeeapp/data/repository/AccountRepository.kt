package com.example.honestbeeapp.data.repository

import com.example.honestbeeapp.data.model.SessionProfile
import com.example.honestbeeapp.data.model.SessionState
import com.example.honestbeeapp.data.model.UserRole
import com.example.honestbeeapp.util.FirebaseConstants
import com.example.honestbeeapp.util.readableMessage
import com.example.honestbeeapp.util.shortUid
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.SetOptions

class AccountRepository(
    private val auth: FirebaseAuth = FirebaseAuth.getInstance(),
    private val firestore: FirebaseFirestore = FirebaseFirestore.getInstance()
) {
    fun observeSession(onState: (SessionState) -> Unit): () -> Unit {
        val listener = FirebaseAuth.AuthStateListener { firebaseAuth ->
            val user = firebaseAuth.currentUser
            if (user == null) {
                onState(SessionState.SignedOut)
                return@AuthStateListener
            }

            onState(SessionState.LoadingProfile)
            loadSharedUserProfile(
                user = user,
                onProfile = { profile -> onState(profile.toSessionState()) },
                onError = { message -> onState(SessionState.Error(message)) }
            )
        }

        auth.addAuthStateListener(listener)
        return { auth.removeAuthStateListener(listener) }
    }

    fun signIn(
        email: String,
        password: String,
        onError: (String) -> Unit
    ) {
        auth.signInWithEmailAndPassword(email, password)
            .addOnFailureListener { onError(it.readableMessage("Could not sign in.")) }
    }

    fun register(
        email: String,
        password: String,
        displayName: String,
        role: UserRole,
        onProfileReady: (SessionProfile) -> Unit,
        onError: (String) -> Unit
    ) {
        auth.createUserWithEmailAndPassword(email, password)
            .addOnSuccessListener { result ->
                val user = result.user
                if (user == null) {
                    onError("Account was created, but Firebase did not return a user.")
                    return@addOnSuccessListener
                }

                createSharedAccountDocuments(
                    user = user,
                    displayName = displayName,
                    role = role,
                    onProfileReady = onProfileReady,
                    onError = onError
                )
            }
            .addOnFailureListener { onError(it.readableMessage("Could not register.")) }
    }

    fun logout() {
        auth.signOut()
    }

    private fun createSharedAccountDocuments(
        user: FirebaseUser,
        displayName: String,
        role: UserRole,
        onProfileReady: (SessionProfile) -> Unit,
        onError: (String) -> Unit
    ) {
        val cleanName = displayName.ifBlank { "Honestbee user" }
        val email = user.email.orEmpty()
        val status = if (role == UserRole.Customer) {
            FirebaseConstants.STATUS_ACTIVE
        } else {
            FirebaseConstants.STATUS_PENDING
        }
        val batch = firestore.batch()
        val userRef = firestore.collection(FirebaseConstants.USERS).document(user.uid)
        val userData = hashMapOf<String, Any>(
            "uid" to user.uid,
            "email" to email,
            "username" to cleanName,
            "role" to role.id,
            "status" to status,
            "updatedAt" to FieldValue.serverTimestamp(),
            "createdAt" to FieldValue.serverTimestamp()
        )

        batch.set(userRef, userData, SetOptions.merge())

        role.profileCollection?.let { collection ->
            val profileRef = firestore.collection(collection).document(user.uid)
            val profileData = hashMapOf<String, Any>(
                "uid" to user.uid,
                "email" to email,
                "status" to status,
                "updatedAt" to FieldValue.serverTimestamp(),
                "createdAt" to FieldValue.serverTimestamp()
            )
            batch.set(profileRef, profileData, SetOptions.merge())
        }

        batch.commit()
            .addOnSuccessListener {
                onProfileReady(
                    SessionProfile(
                        uid = user.uid,
                        email = email,
                        displayName = cleanName,
                        role = role,
                        status = status
                    )
                )
            }
            .addOnFailureListener { onError(it.readableMessage("Could not save account profile.")) }
    }

    private fun loadSharedUserProfile(
        user: FirebaseUser,
        onProfile: (SessionProfile) -> Unit,
        onError: (String) -> Unit
    ) {
        firestore.collection(FirebaseConstants.USERS).document(user.uid)
            .get()
            .addOnSuccessListener { document ->
                if (!document.exists()) {
                    onError("Signed in, but users/${user.uid.shortUid()} does not exist.")
                    return@addOnSuccessListener
                }

                val role = UserRole.from(document.getString("role"))
                if (role == null) {
                    onError("users/${user.uid.shortUid()} has an unsupported role.")
                    return@addOnSuccessListener
                }

                val status = document.getString("status")
                    ?.trim()
                    ?.lowercase()
                    .orEmpty()
                    .ifBlank { "pending" }

                onProfile(
                    SessionProfile(
                        uid = user.uid,
                        email = document.getString("email") ?: user.email.orEmpty(),
                        displayName = document.getString("username")
                            ?: document.getString("displayName")
                            ?: document.getString("name")
                            ?: "",
                        role = role,
                        status = status
                    )
                )
            }
            .addOnFailureListener {
                onError(it.readableMessage("Could not load users/${user.uid.shortUid()}."))
            }
    }
}
