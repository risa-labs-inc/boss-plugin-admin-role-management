package ai.rever.boss.plugin.dynamic.adminrolemanagement

import ai.rever.boss.plugin.api.McpToolDefinition
import ai.rever.boss.plugin.api.McpToolHandler
import ai.rever.boss.plugin.api.McpToolProvider
import ai.rever.boss.plugin.api.McpToolResult
import ai.rever.boss.plugin.api.UserManagementProvider

/**
 * MCP tools contributed by the Admin Role Management plugin: list users and
 * roles, and assign/remove roles. This plugin loads only for admins, so these
 * tools exist only for admin users. Registered in
 * [AdminRoleManagementDynamicPlugin.register]; removed automatically on
 * disable/unload.
 */
internal class AdminRoleManagementMcpToolProvider(
    override val providerId: String,
    private val users: UserManagementProvider?,
) : McpToolProvider {

    override fun tools(): List<McpToolDefinition> = listOf(
        McpToolDefinition(
            name = "users_list",
            description = "List users with their roles (id, email, roles).",
            inputSchema = LIMIT_SCHEMA,
            handler = McpToolHandler { args ->
                val p = users ?: return@McpToolHandler unavailable()
                val limit = (args.int("limit") ?: 50).coerceIn(1, 200)
                p.getAllUsersWithRoles(limit).fold(
                    onSuccess = { page ->
                        if (page.data.isEmpty()) McpToolResult("No users.")
                        else McpToolResult(page.data.joinToString("\n") { u ->
                            "${u.id}\t${u.email}\t[${u.roles.joinToString(", ")}]"
                        })
                    },
                    onFailure = { McpToolResult("Failed: ${it.message}", isError = true) },
                )
            },
        ),
        McpToolDefinition(
            name = "user_search",
            description = "Search users by email; returns id, email, roles.",
            inputSchema = QUERY_SCHEMA,
            handler = McpToolHandler { args ->
                val p = users ?: return@McpToolHandler unavailable()
                val query = args.string("query")
                    ?: return@McpToolHandler McpToolResult("Missing required argument: query", isError = true)
                p.searchUsersByEmail(query).fold(
                    onSuccess = { page ->
                        if (page.data.isEmpty()) McpToolResult("No matching users.")
                        else McpToolResult(page.data.joinToString("\n") { u -> "${u.id}\t${u.email}\t[${u.roles.joinToString(", ")}]" })
                    },
                    onFailure = { McpToolResult("Failed: ${it.message}", isError = true) },
                )
            },
        ),
        McpToolDefinition(
            name = "roles_list",
            description = "List all roles (id, name, permission count).",
            handler = McpToolHandler {
                val p = users ?: return@McpToolHandler unavailable()
                p.getAllRoles().fold(
                    onSuccess = { roles ->
                        if (roles.isEmpty()) McpToolResult("No roles.")
                        else McpToolResult(roles.joinToString("\n") { r -> "${r.name}\t(${r.permissions.size} perms)${if (r.isSystem) "\t[system]" else ""}" })
                    },
                    onFailure = { McpToolResult("Failed: ${it.message}", isError = true) },
                )
            },
        ),
        McpToolDefinition(
            name = "user_role_assign",
            description = "Assign a role to a user.",
            inputSchema = USER_ROLE_SCHEMA,
            readOnly = false,
            handler = McpToolHandler { args -> roleOp(args) { p, uid, role -> p.assignRole(uid, role) } },
        ),
        McpToolDefinition(
            name = "user_role_remove",
            description = "Remove a role from a user.",
            inputSchema = USER_ROLE_SCHEMA,
            readOnly = false,
            handler = McpToolHandler { args -> roleOp(args) { p, uid, role -> p.removeRole(uid, role) } },
        ),
    ).onEach { it.requiredPermissions = permissionsFor(it.name) }

    // RBAC gate (admins bypass), aligned with the server: the all-users RLS and
    // this plugin's manifest both gate on role.read, so user listing uses that
    // (users.read exists but isn't what the server checks here); role
    // assign/remove match the server's role.assign exactly.
    private fun permissionsFor(tool: String): List<String> = when (tool) {
        "users_list", "user_search", "roles_list" -> listOf("role.read")
        "user_role_assign", "user_role_remove" -> listOf("role.assign")
        else -> emptyList()
    }

    private suspend fun roleOp(
        args: ai.rever.boss.plugin.api.McpToolArgs,
        op: suspend (UserManagementProvider, String, String) -> Result<Unit>,
    ): McpToolResult {
        val p = users ?: return unavailable()
        val userId = args.string("user_id")
            ?: return McpToolResult("Missing required argument: user_id", isError = true)
        val role = args.string("role")
            ?: return McpToolResult("Missing required argument: role", isError = true)
        return op(p, userId, role).fold(
            onSuccess = { McpToolResult("OK") },
            onFailure = { McpToolResult("Failed: ${it.message}", isError = true) },
        )
    }

    private fun unavailable(): McpToolResult =
        McpToolResult("User management provider unavailable (admin only).", isError = true)

    private companion object {
        const val LIMIT_SCHEMA =
            """{"type":"object","properties":{"limit":{"type":"integer","description":"Max users (default 50)."}}}"""
        const val QUERY_SCHEMA =
            """{"type":"object","properties":{"query":{"type":"string","description":"Email search text."}},"required":["query"]}"""
        const val USER_ROLE_SCHEMA =
            """{"type":"object","properties":{"user_id":{"type":"string","description":"User id."},"role":{"type":"string","description":"Role name."}},"required":["user_id","role"]}"""
    }
}
