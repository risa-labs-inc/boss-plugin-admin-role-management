package ai.rever.boss.plugin.dynamic.adminrolemanagement

import ai.rever.boss.plugin.api.Panel.Companion.right
import ai.rever.boss.plugin.api.Panel.Companion.top
import ai.rever.boss.plugin.api.Panel.Companion.bottom
import ai.rever.boss.plugin.api.PanelId
import ai.rever.boss.plugin.api.PanelInfo
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Security

object AdminRoleManagementInfo : PanelInfo {
    override val id = PanelId("admin-role-management", 22)
    override val displayName = "Admin Role Management"
    override val icon = Icons.Outlined.Security
    override val defaultSlotPosition = right.top.bottom
}
