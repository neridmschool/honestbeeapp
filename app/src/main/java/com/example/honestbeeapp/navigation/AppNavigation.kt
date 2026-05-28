package com.example.honestbeeapp.navigation

import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import com.example.honestbeeapp.data.model.AppUser
import com.example.honestbeeapp.data.model.SessionProfile
import com.example.honestbeeapp.data.model.UserRole
import com.example.honestbeeapp.data.repository.AccountRepository
import com.example.honestbeeapp.ui.admin.AdminMainScreen
import com.example.honestbeeapp.ui.auth.CustomerRegisterScreen
import com.example.honestbeeapp.ui.auth.LoginScreen
import com.example.honestbeeapp.ui.auth.MerchantRegisterScreen
import com.example.honestbeeapp.ui.auth.PendingApprovalScreen
import com.example.honestbeeapp.ui.auth.RejectedScreen
import com.example.honestbeeapp.ui.auth.RiderRegisterScreen
import com.example.honestbeeapp.ui.auth.WelcomeScreen
import com.example.honestbeeapp.ui.components.LoadingScreen
import com.example.honestbeeapp.ui.components.ProfileErrorScreen
import com.example.honestbeeapp.ui.customer.CustomerMainScreen
import com.example.honestbeeapp.ui.merchant.MerchantMainScreen
import com.example.honestbeeapp.ui.rider.RiderMainScreen
import com.example.honestbeeapp.util.FirebaseConstants
import com.example.honestbeeapp.util.shortUid
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.TimeoutCancellationException
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.withTimeout

@Composable
fun AppNavigation(
    auth: FirebaseAuth = remember { FirebaseAuth.getInstance() },
    firestore: FirebaseFirestore = remember { FirebaseFirestore.getInstance() }
) {
    val accountRepository = remember(auth, firestore) {
        AccountRepository(auth = auth, firestore = firestore)
    }
    var currentUser by remember { mutableStateOf(auth.currentUser) }
    var reloadKey by remember { mutableStateOf(0) }
    var loggedOutMessage by rememberSaveable { mutableStateOf<String?>(null) }
    var routeState by remember {
        mutableStateOf<AppRouteState>(
            if (auth.currentUser == null) AppRouteState.Welcome else AppRouteState.Loading
        )
    }

    DisposableEffect(auth) {
        val listener = FirebaseAuth.AuthStateListener { firebaseAuth ->
            currentUser = firebaseAuth.currentUser
        }
        auth.addAuthStateListener(listener)
        onDispose { auth.removeAuthStateListener(listener) }
    }

    LaunchedEffect(currentUser?.uid, reloadKey) {
        val firebaseUser = currentUser
        if (firebaseUser == null) {
            routeState = AppRouteState.Welcome
            return@LaunchedEffect
        }

        routeState = AppRouteState.Loading
        routeState = loadRouteForUser(
            firestore = firestore,
            firebaseUser = firebaseUser
        )
    }

    when (val state = routeState) {
        AppRouteState.Loading -> LoadingScreen()

        AppRouteState.Welcome -> WelcomeScreen(
            onLoginClick = { routeState = AppRouteState.Login },
            onCustomerSignUpClick = { routeState = AppRouteState.CustomerRegister },
            onMerchantSignUpClick = { routeState = AppRouteState.MerchantRegister },
            onRiderSignUpClick = { routeState = AppRouteState.RiderRegister }
        )

        AppRouteState.Login -> LoginScreen(
            initialMessage = loggedOutMessage,
            onSignedIn = {
                loggedOutMessage = null
                currentUser = auth.currentUser
                routeState = AppRouteState.Loading
                reloadKey++
            },
            onBackToWelcome = { routeState = AppRouteState.Welcome }
        )

        AppRouteState.CustomerRegister -> CustomerRegisterScreen(
            onRegistered = {
                loggedOutMessage = null
                currentUser = auth.currentUser
                routeState = AppRouteState.Loading
                reloadKey++
            },
            onBackToWelcome = { routeState = AppRouteState.Welcome }
        )

        AppRouteState.MerchantRegister -> MerchantRegisterScreen(
            onRegistered = {
                loggedOutMessage = null
                currentUser = auth.currentUser
                routeState = AppRouteState.Loading
                reloadKey++
            },
            onBackToWelcome = { routeState = AppRouteState.Welcome }
        )

        AppRouteState.RiderRegister -> RiderRegisterScreen(
            onRegistered = {
                loggedOutMessage = null
                currentUser = auth.currentUser
                routeState = AppRouteState.Loading
                reloadKey++
            },
            onBackToWelcome = { routeState = AppRouteState.Welcome }
        )

        is AppRouteState.CustomerDashboard -> CustomerMainScreen(
            profile = state.profile,
            onLogout = accountRepository::logout
        )

        is AppRouteState.MerchantDashboard -> MerchantMainScreen(
            profile = state.profile,
            onLogout = accountRepository::logout
        )

        is AppRouteState.RiderDashboard -> RiderMainScreen(
            profile = state.profile,
            onLogout = accountRepository::logout
        )

        is AppRouteState.AdminDashboard -> AdminMainScreen(
            profile = state.profile,
            onLogout = accountRepository::logout
        )

        is AppRouteState.PendingApproval -> PendingApprovalScreen(
            profile = state.profile,
            onLogout = accountRepository::logout
        )

        is AppRouteState.Rejected -> RejectedScreen(
            profile = state.profile,
            onLogout = accountRepository::logout
        )

        is AppRouteState.DeletedAccount -> {
            LaunchedEffect(state.message) {
                loggedOutMessage = state.message
                auth.signOut()
                currentUser = null
                routeState = AppRouteState.Welcome
            }
            LoadingScreen()
        }

        is AppRouteState.Error -> ProfileErrorScreen(
            message = state.message,
            onLogout = accountRepository::logout
        )
    }
}

private suspend fun loadRouteForUser(
    firestore: FirebaseFirestore,
    firebaseUser: FirebaseUser
): AppRouteState {
    return try {
        val userDocument = withTimeout(10_000) {
            firestore.collection(FirebaseConstants.USERS)
                .document(firebaseUser.uid)
                .get()
                .await()
        }

        if (!userDocument.exists()) {
            return AppRouteState.Error("Signed in, but users/${firebaseUser.uid.shortUid()} does not exist.")
        }

        val appUser = userDocument.toObject(AppUser::class.java)
            ?: return AppRouteState.Error("Could not read users/${firebaseUser.uid.shortUid()}.")

        val role = UserRole.from(appUser.role)
            ?: return AppRouteState.Error("users/${firebaseUser.uid.shortUid()} has an unsupported role.")

        val status = appUser.status.trim().lowercase().ifBlank {
            FirebaseConstants.STATUS_PENDING
        }

        if (status == FirebaseConstants.STATUS_DELETED) {
            return AppRouteState.DeletedAccount("Your account has been deleted. Please contact the admin.")
        }

        val profile = appUser.toSessionProfile(
            firebaseUid = firebaseUser.uid,
            firebaseEmail = firebaseUser.email.orEmpty(),
            role = role,
            status = status
        )

        routeByRoleAndStatus(profile)
    } catch (exception: TimeoutCancellationException) {
        AppRouteState.Error("Account check timed out. Please check your internet connection, then logout and try again.")
    } catch (exception: Exception) {
        AppRouteState.Error(exception.localizedMessage ?: "Could not load account status.")
    }
}

private fun routeByRoleAndStatus(profile: SessionProfile): AppRouteState {
    return when (profile.status) {
        FirebaseConstants.STATUS_ACTIVE,
        FirebaseConstants.STATUS_APPROVED -> when (profile.role) {
            UserRole.Customer -> AppRouteState.CustomerDashboard(profile)
            UserRole.Merchant -> AppRouteState.MerchantDashboard(profile)
            UserRole.Rider -> AppRouteState.RiderDashboard(profile)
            UserRole.Admin -> AppRouteState.AdminDashboard(profile)
        }

        FirebaseConstants.STATUS_PENDING -> AppRouteState.PendingApproval(profile)
        FirebaseConstants.STATUS_REJECTED -> AppRouteState.Rejected(profile)
        else -> AppRouteState.Error("Unsupported account status: ${profile.status}")
    }
}

private fun AppUser.toSessionProfile(
    firebaseUid: String,
    firebaseEmail: String,
    role: UserRole,
    status: String
): SessionProfile {
    val fullName = "$firstName $lastName".trim()
    val displayName = username.ifBlank { fullName }

    return SessionProfile(
        uid = uid.ifBlank { firebaseUid },
        email = email.ifBlank { firebaseEmail },
        displayName = displayName,
        role = role,
        status = status
    )
}

private sealed class AppRouteState {
    object Loading : AppRouteState()
    object Welcome : AppRouteState()
    object Login : AppRouteState()
    object CustomerRegister : AppRouteState()
    object MerchantRegister : AppRouteState()
    object RiderRegister : AppRouteState()
    data class CustomerDashboard(val profile: SessionProfile) : AppRouteState()
    data class MerchantDashboard(val profile: SessionProfile) : AppRouteState()
    data class RiderDashboard(val profile: SessionProfile) : AppRouteState()
    data class AdminDashboard(val profile: SessionProfile) : AppRouteState()
    data class PendingApproval(val profile: SessionProfile) : AppRouteState()
    data class Rejected(val profile: SessionProfile) : AppRouteState()
    data class DeletedAccount(val message: String) : AppRouteState()
    data class Error(val message: String) : AppRouteState()
}
