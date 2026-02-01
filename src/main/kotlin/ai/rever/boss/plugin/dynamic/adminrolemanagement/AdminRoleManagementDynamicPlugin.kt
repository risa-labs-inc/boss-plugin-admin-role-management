package ai.rever.boss.plugin.dynamic.adminrolemanagement

import ai.rever.boss.plugin.api.DynamicPlugin
import ai.rever.boss.plugin.api.PluginContext

/**
 * Admin Role Management dynamic plugin - Loaded from external JAR.
 *
 * Manage user roles and permissions
 */
class AdminRoleManagementDynamicPlugin : DynamicPlugin {
    override val pluginId: String = "ai.rever.boss.plugin.dynamic.adminrolemanagement"
    override val displayName: String = "Admin Role Management (Dynamic)"
    override val version: String = "1.0.3"
    override val description: String = "Manage user roles and permissions"
    override val author: String = "Risa Labs"
    override val url: String = "https://github.com/risa-labs-inc/boss-plugin-admin-role-management"

    override fun register(context: PluginContext) {
        context.panelRegistry.registerPanel(AdminRoleManagementInfo) { ctx, panelInfo ->
            AdminRoleManagementComponent(ctx, panelInfo)
        }
    }
}
