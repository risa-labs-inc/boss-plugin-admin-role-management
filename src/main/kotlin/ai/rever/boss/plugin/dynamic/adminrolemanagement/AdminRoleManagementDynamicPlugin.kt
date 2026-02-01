package ai.rever.boss.plugin.dynamic.adminrolemanagement

import ai.rever.boss.plugin.api.AuthDataProvider
import ai.rever.boss.plugin.api.DynamicPlugin
import ai.rever.boss.plugin.api.PluginContext
import ai.rever.boss.plugin.api.UserManagementProvider
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers

/**
 * Admin Role Management dynamic plugin - Loaded from external JAR.
 *
 * Manage user roles and permissions.
 * Uses userManagementProvider and authDataProvider from PluginContext.
 */
class AdminRoleManagementDynamicPlugin : DynamicPlugin {
    override val pluginId: String = "ai.rever.boss.plugin.dynamic.adminrolemanagement"
    override val displayName: String = "Admin Role Management (Dynamic)"
    override val version: String = "1.0.4"
    override val description: String = "Manage user roles and permissions"
    override val author: String = "Risa Labs"
    override val url: String = "https://github.com/risa-labs-inc/boss-plugin-admin-role-management"

    override fun register(context: PluginContext) {
        // Try to get providers via reflection for backwards compatibility with 1.0.3
        val userManagementProvider = try {
            val method = context.javaClass.getMethod("getUserManagementProvider")
            method.invoke(context) as? UserManagementProvider
        } catch (_: Exception) {
            null
        }

        val authDataProvider = try {
            val method = context.javaClass.getMethod("getAuthDataProvider")
            method.invoke(context) as? AuthDataProvider
        } catch (_: Exception) {
            null
        }

        val pluginScope = try {
            val method = context.javaClass.getMethod("getPluginScope")
            method.invoke(context) as? CoroutineScope
        } catch (_: Exception) {
            null
        }

        context.panelRegistry.registerPanel(AdminRoleManagementInfo) { ctx, panelInfo ->
            AdminRoleManagementComponent(
                ctx = ctx,
                panelInfo = panelInfo,
                userManagementProvider = userManagementProvider,
                authDataProvider = authDataProvider,
                scope = pluginScope ?: CoroutineScope(Dispatchers.Main)
            )
        }
    }
}
