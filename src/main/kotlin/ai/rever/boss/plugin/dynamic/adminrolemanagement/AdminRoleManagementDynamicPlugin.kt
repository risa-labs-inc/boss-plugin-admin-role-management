package ai.rever.boss.plugin.dynamic.adminrolemanagement

import ai.rever.boss.plugin.api.DynamicPlugin
import ai.rever.boss.plugin.api.PluginContext

/**
 * Admin Role Management dynamic plugin - Loaded from external JAR.
 *
 * Manage user roles and permissions.
 * Uses SupabaseDataProvider and AuthDataProvider from PluginContext.
 */
class AdminRoleManagementDynamicPlugin : DynamicPlugin {
    override val pluginId: String = "ai.rever.boss.plugin.dynamic.adminrolemanagement"
    override val displayName: String = "Admin Role Management (Dynamic)"
    override val version: String = "1.1.0"
    override val description: String = "Manage user roles and permissions"
    override val author: String = "Risa Labs"
    override val url: String = "https://github.com/risa-labs-inc/boss-plugin-admin-role-management"

    override fun register(context: PluginContext) {
        val supabaseDataProvider = context.supabaseDataProvider
        val authDataProvider = context.authDataProvider

        if (supabaseDataProvider == null || authDataProvider == null) {
            context.panelRegistry.registerPanel(AdminRoleManagementInfo) { ctx, panelInfo ->
                AdminRoleManagementComponent(ctx, panelInfo, null, null)
            }
            return
        }

        context.panelRegistry.registerPanel(AdminRoleManagementInfo) { ctx, panelInfo ->
            AdminRoleManagementComponent(
                ctx = ctx,
                panelInfo = panelInfo,
                supabaseDataProvider = supabaseDataProvider,
                authDataProvider = authDataProvider
            )
        }
    }
}
