package com.rptsd.app.ui.screens.profile

import android.content.pm.PackageManager
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.rptsd.app.data.repository.AuthRepository
import com.rptsd.app.data.repository.SubscriptionRepository
import com.rptsd.app.data.remote.dto.subscription.SubscriptionStatusDto
import com.rptsd.app.domain.model.Result
import com.rptsd.app.domain.model.User
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

import android.content.Context
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.BarChart
import androidx.compose.material.icons.filled.ChatBubble
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import com.rptsd.app.ui.navigation.Screen
import dagger.hilt.android.qualifiers.ApplicationContext

// ─── ViewModel ───────────────────────────────────────────────────────────────

data class ProfileUiState(
    val user: User? = null,
    val subscription: SubscriptionStatusDto? = null,
    val isLoading: Boolean = false,
    val appVersion: String = "",
)

@HiltViewModel
class ProfileViewModel @Inject constructor(
    @ApplicationContext private val context: Context,
    private val authRepository: AuthRepository,
    private val subscriptionRepository: SubscriptionRepository,
) : ViewModel() {

    private val _uiState = MutableStateFlow(ProfileUiState())
    val uiState: StateFlow<ProfileUiState> = _uiState

    init {
        val version = try {
            context.packageManager.getPackageInfo(context.packageName, 0).versionName ?: "1.0"
        } catch (_: PackageManager.NameNotFoundException) { "1.0" }
        _uiState.value = _uiState.value.copy(appVersion = version)
        load()
    }

    fun load() {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true)
            val user = authRepository.getCurrentUser()
            val sub = when (val r = subscriptionRepository.getStatus()) {
                is Result.Success -> r.data
                is Result.Error   -> null
            }
            _uiState.value = _uiState.value.copy(isLoading = false, user = user, subscription = sub)
        }
    }

    fun logout(onDone: () -> Unit) {
        viewModelScope.launch {
            authRepository.logout()
            onDone()
        }
    }
}

// ─── Screen ──────────────────────────────────────────────────────────────────

@Composable
fun ProfileScreen(
    navController: NavController,
    viewModel: ProfileViewModel = hiltViewModel(),
) {
    val uiState by viewModel.uiState.collectAsState()
    val user = uiState.user
    val sub = uiState.subscription

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Text("Profile", style = MaterialTheme.typography.headlineMedium)
            IconButton(onClick = { viewModel.load() }, enabled = !uiState.isLoading) {
                if (uiState.isLoading) CircularProgressIndicator(modifier = Modifier.size(20.dp), strokeWidth = 2.dp)
                else Icon(Icons.Default.Refresh, contentDescription = "Refresh")
            }
        }

        // ── User info ─────────────────────────────────────────────────────────
        Card(modifier = Modifier.fillMaxWidth()) {
            Row(
                modifier = Modifier.padding(16.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(16.dp),
            ) {
                Icon(
                    Icons.Default.Person, contentDescription = null,
                    modifier = Modifier.size(48.dp),
                    tint = MaterialTheme.colorScheme.primary,
                )
                Column {
                    Text(user?.name ?: "—", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.SemiBold)
                    Text(user?.email ?: "—", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    Text(user?.phone ?: "—", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
            }
        }

        // ── Subscription ──────────────────────────────────────────────────────
        Card(modifier = Modifier.fillMaxWidth()) {
            Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Text("Subscription", style = MaterialTheme.typography.titleMedium)
                    val isActive = sub?.isActive == true
                    Surface(
                        color = if (isActive) Color(0xFFE8F5E9) else Color(0xFFFFEBEE),
                        shape = MaterialTheme.shapes.small,
                    ) {
                        Text(
                            text = sub?.status ?: "—",
                            modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp),
                            style = MaterialTheme.typography.labelMedium,
                            color = if (isActive) Color(0xFF2E7D32) else MaterialTheme.colorScheme.error,
                            fontWeight = FontWeight.Bold,
                        )
                    }
                }
                if (sub?.endDate != null) {
                    Text(
                        text = "Expires: ${sub.endDate.take(10)}",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                }
                if ((sub?.daysRemaining ?: 0) >= 0) {
                    Text(
                        text = "${sub?.daysRemaining ?: 0} days remaining",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                }
                if (sub?.isActive != true) {
                    Spacer(modifier = Modifier.height(4.dp))
                    OutlinedButton(
                        onClick = { navController.navigate(Screen.Subscription.route) },
                        modifier = Modifier.fillMaxWidth(),
                    ) { Text("Renew Now") }
                }
            }
        }

        // ── Quick links ───────────────────────────────────────────────────────
        Card(modifier = Modifier.fillMaxWidth()) {
            Column {
                ProfileLink("View My Stats", Icons.Default.BarChart) {
                    navController.navigate(Screen.Stats.route)
                }
                HorizontalDivider(modifier = Modifier.padding(horizontal = 16.dp))
                ProfileLink("Send Feedback", Icons.Default.ChatBubble) {
                    navController.navigate(Screen.Feedback.route)
                }
            }
        }

        Spacer(modifier = Modifier.weight(1f))

        // ── Logout ────────────────────────────────────────────────────────────
        OutlinedButton(
            onClick = {
                viewModel.logout {
                    navController.navigate(Screen.Login.route) { popUpTo(0) { inclusive = true } }
                }
            },
            modifier = Modifier.fillMaxWidth(),
            colors = ButtonDefaults.outlinedButtonColors(contentColor = MaterialTheme.colorScheme.error),
        ) { Text("Logout") }

        Text(
            text = "v${uiState.appVersion}",
            style = MaterialTheme.typography.labelSmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.fillMaxWidth().wrapContentWidth(Alignment.CenterHorizontally),
        )
    }
}

@Composable
private fun ProfileLink(label: String, icon: ImageVector, onClick: () -> Unit) {
    TextButton(
        onClick = onClick,
        modifier = Modifier.fillMaxWidth(),
        contentPadding = PaddingValues(horizontal = 16.dp, vertical = 12.dp),
    ) {
        Icon(icon, contentDescription = null, modifier = Modifier.size(20.dp))
        Spacer(modifier = Modifier.width(12.dp))
        Text(label, style = MaterialTheme.typography.bodyMedium, modifier = Modifier.weight(1f))
        Icon(Icons.Default.ChevronRight, contentDescription = null, tint = MaterialTheme.colorScheme.onSurfaceVariant)
    }
}
