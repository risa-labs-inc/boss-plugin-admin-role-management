package ai.rever.boss.plugin.dynamic.adminrolemanagement

import ai.rever.boss.plugin.api.AuthDataProvider
import ai.rever.boss.plugin.api.PanelComponentWithUI
import ai.rever.boss.plugin.api.PanelInfo
import ai.rever.boss.plugin.api.UserManagementProvider
import ai.rever.boss.plugin.ui.BossTheme
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Security
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.arkivanov.decompose.ComponentContext
import com.arkivanov.essenty.lifecycle.doOnDestroy
import kotlinx.coroutines.CoroutineScope

/**
 * Admin Role Management panel component (Dynamic Plugin)
 *
 * Uses userManagementProvider and authDataProvider from PluginContext
 * for user and role operations.
 */
class AdminRoleManagementComponent(
    ctx: ComponentContext,
    override val panelInfo: PanelInfo,
    private val userManagementProvider: UserManagementProvider?,
    private val authDataProvider: AuthDataProvider?,
    private val scope: CoroutineScope
) : PanelComponentWithUI, ComponentContext by ctx {

    private val viewModel: AdminRoleManagementViewModel? = userManagementProvider?.let {
        AdminRoleManagementViewModel(it)
    }

    init {
        lifecycle.doOnDestroy {
            viewModel?.dispose()
        }
    }

    @Composable
    override fun Content() {
        BossTheme {
            if (viewModel != null && authDataProvider != null) {
                val currentUser by authDataProvider.currentUser.collectAsState()
                AdminRoleManagementContent(
                    viewModel = viewModel,
                    currentUserId = currentUser?.id
                )
            } else {
                // Provider not available - show stub UI
                ProvidersNotAvailableContent()
            }
        }
    }
}

@Composable
private fun ProvidersNotAvailableContent() {
    Surface(modifier = Modifier.fillMaxSize(), color = MaterialTheme.colors.background) {
        Column(
            modifier = Modifier.fillMaxSize().padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Icon(
                imageVector = Icons.Outlined.Security,
                contentDescription = "Admin Role Management",
                modifier = Modifier.size(48.dp),
                tint = MaterialTheme.colors.primary
            )
            Spacer(modifier = Modifier.height(16.dp))
            Text(
                text = "Admin Role Management",
                fontSize = 20.sp,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colors.onBackground
            )
            Spacer(modifier = Modifier.height(8.dp))
            Card(
                modifier = Modifier.padding(16.dp),
                shape = RoundedCornerShape(8.dp),
                backgroundColor = MaterialTheme.colors.surface,
                elevation = 4.dp
            ) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        text = "Providers Not Available",
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Medium,
                        color = MaterialTheme.colors.primary
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = "User management and auth providers are required.\nPlease update to plugin-api 1.0.4 or later.",
                        fontSize = 12.sp,
                        color = MaterialTheme.colors.onSurface.copy(alpha = 0.7f)
                    )
                }
            }
        }
    }
}
