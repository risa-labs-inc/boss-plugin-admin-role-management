# BOSS Admin Roles

Assign roles to users, in the right sidebar as **Admin: Roles**.

The people half of BOSS's RBAC. This plugin puts existing roles onto users; creating those
roles and permissions in the first place is the job of its sibling, [Role
Creation](https://github.com/risa-labs-inc/boss-plugin-role-creation), which sits directly
below it in the same slot.

## What it does

- **Lists users with their roles** from the `users_with_roles` view, paginated with a "load
  more" control.
- **Search by email**, debounced so typing does not fire a query per keystroke.
- **Assign a role** to a user, or **remove one**, each behind a confirmation dialog.
- **Delete a user**. The control only appears for an admin session, and never against another
  admin.
- **Admin badges** mark privileged users in the list, and every operation reports success or
  failure as a toast.

The role dropdown is populated from the `get_grantable_roles()` RPC rather than the full role
list, so it reflects what the server will actually let you grant.

## MCP tools

| Tool | Purpose |
|---|---|
| `users_list` | List users with their id, email and roles |
| `user_search` | Search users by email |
| `roles_list` | All roles with permission counts and a system flag |
| `user_role_assign` | Assign a role to a user |
| `user_role_remove` | Remove a role from a user |

## Permissions

Manifest `requiredPermissions` is `["role.read", "role.assign"]`.

Per-tool gates match: the three read tools need `role.read`, assign and remove need
`role.assign`. These are aligned with the server's RLS policies rather than with a plausible
looking `users.read`, which exists but is not what the server checks.

## Requirements

- BOSS >= 9.2.20, boss-plugin-api >= 1.0.20
- `supabaseDataProvider` and `authDataProvider`. If either is missing the plugin registers a
  degraded panel rather than crashing.
- `userManagementProvider` for the MCP tools.
- No external binaries.

## Build

```bash
./gradlew buildPluginJar
cp build/libs/boss-plugin-admin-role-management-*.jar ~/.boss/plugins/
```

See [AGENTS.md](AGENTS.md) for architecture and conventions.

## License

Proprietary - Risa Labs Inc.
