package ai.rever.boss.plugin.dynamic.adminrolemanagement

import ai.rever.boss.plugin.api.PanelComponentWithUI
import ai.rever.boss.plugin.api.PanelInfo
import androidx.compose.runtime.Composable
import com.arkivanov.decompose.ComponentContext

/**
 * Admin Role Management panel component (Dynamic Plugin)
 *
 * This is a stub implementation. Full functionality requires
 * host services not yet exposed through PluginContext.
 */
class AdminRoleManagementComponent(
    ctx: ComponentContext,
    override val panelInfo: PanelInfo
) : PanelComponentWithUI, ComponentContext by ctx {

    @Composable
    override fun Content() {
        AdminRoleManagementContent()
    }
}
