package ai.rever.boss.plugin.dynamic.adminrolemanagement

import ai.rever.boss.plugin.api.DynamicPlugin
import ai.rever.boss.plugin.api.PluginContext

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

    private var userManagementProvider: ai.rever.boss.plugin.api.UserManagementProvider? = null
    private var authDataProvider: ai.rever.boss.plugin.api.AuthDataProvider? = null
    private var pluginScope: kotlinx.coroutines.CoroutineScope? = null

    override fun register(context: PluginContext) {
        userManagementProvider = context.userManagementProvider
        authDataProvider = context.authDataProvider
        pluginScope = context.pluginScope

        context.panelRegistry.registerPanel(AdminRoleManagementInfo) { ctx, panelInfo ->
            AdminRoleManagementComponent(
                ctx = ctx,
                panelInfo = panelInfo,
                userManagementProvider = userManagementProvider,
                authDataProvider = authDataProvider,
                scope = pluginScope ?: kotlinx.coroutines.CoroutineScope(kotlinx.coroutines.Dispatchers.Main)
            )
        }
    }
}
