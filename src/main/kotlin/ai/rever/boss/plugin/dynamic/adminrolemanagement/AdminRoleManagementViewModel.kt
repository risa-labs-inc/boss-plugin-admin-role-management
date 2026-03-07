package ai.rever.boss.plugin.dynamic.adminrolemanagement

import ai.rever.boss.plugin.api.FilterOperator
import ai.rever.boss.plugin.api.QueryFilter
import ai.rever.boss.plugin.api.QueryRange
import ai.rever.boss.plugin.api.SupabaseDataProvider
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json

private val json = Json { ignoreUnknownKeys = true }

@Serializable
data class UserWithRolesRow(
    val id: String,
    val email: String,
    val roles: List<String> = emptyList()
)

@Serializable
data class RoleRow(
    val id: String = "",
    val name: String,
    val description: String? = null
)

/**
 * ViewModel for Admin Role Management
 *
 * Uses SupabaseDataProvider for data operations via generic select/rpc calls.
 */
class AdminRoleManagementViewModel(
    private val dataProvider: SupabaseDataProvider
) {
    private val scope = CoroutineScope(Dispatchers.Main + SupervisorJob())
    private var searchJob: Job? = null

    var state by mutableStateOf(AdminRoleState())
        private set

    init {
        loadAllUsers()
        loadAvailableRoles()
    }

    fun dispose() {
        scope.cancel()
    }

    private fun refreshCurrentView() {
        if (state.searchQuery.isBlank()) {
            loadAllUsers()
        } else {
            searchUsers(state.searchQuery)
        }
    }

    private suspend fun fetchUsersWithRoles(
        filters: List<QueryFilter> = emptyList(),
        offset: Int,
        limit: Int
    ): Result<Pair<List<UserWithRoles>, Boolean>> {
        // Fetch limit + 1 to detect hasMore
        val fetchLimit = limit + 1
        val result = dataProvider.select(
            table = "users_with_roles",
            columns = "*",
            filters = filters,
            range = QueryRange(offset.toLong(), (offset + fetchLimit - 1).toLong())
        )

        return result.mapCatching { jsonStr ->
            val allRows = json.decodeFromString<List<UserWithRolesRow>>(jsonStr)
            val hasMore = allRows.size > limit
            val rows = if (hasMore) allRows.take(limit) else allRows

            val users = rows.map { row ->
                UserWithRoles(id = row.id, email = row.email, roles = row.roles)
            }
            Pair(users, hasMore)
        }
    }

    fun loadAllUsers() {
        searchJob?.cancel()
        state = state.copy(
            isLoading = true,
            errorMessage = null,
            searchQuery = "",
            currentOffset = 0,
            hasMore = true
        )

        scope.launch {
            val result = fetchUsersWithRoles(offset = 0, limit = state.pageSize)

            result.onSuccess { (users, hasMore) ->
                state = state.copy(
                    allUsers = users,
                    filteredUsers = users,
                    isLoading = false,
                    currentOffset = users.size,
                    hasMore = hasMore
                )
            }.onFailure { exception ->
                state = state.copy(
                    isLoading = false,
                    errorMessage = exception.message ?: "Unknown error"
                )
            }
        }
    }

    fun loadAvailableRoles() {
        scope.launch {
            val result = dataProvider.select(table = "roles", columns = "*")

            result.onSuccess { rolesJson ->
                val roles = json.decodeFromString<List<RoleRow>>(rolesJson)
                state = state.copy(availableRoles = roles.map { RoleInfo(it.id, it.name, it.description) })
            }.onFailure {
                state = state.copy(availableRoles = emptyList())
            }
        }
    }

    fun loadMoreUsers() {
        if (state.searchQuery.isNotBlank()) {
            loadMoreSearchResults()
            return
        }

        if (state.isLoadingMore || !state.hasMore || state.isLoading) {
            return
        }

        state = state.copy(isLoadingMore = true, errorMessage = null)

        scope.launch {
            val result = fetchUsersWithRoles(offset = state.currentOffset, limit = state.pageSize)

            result.onSuccess { (newUsers, hasMore) ->
                val allUsers = state.allUsers + newUsers

                state = state.copy(
                    allUsers = allUsers,
                    filteredUsers = allUsers,
                    isLoadingMore = false,
                    currentOffset = state.currentOffset + newUsers.size,
                    hasMore = hasMore
                )
            }.onFailure { exception ->
                state = state.copy(
                    isLoadingMore = false,
                    errorMessage = exception.message ?: "Unknown error"
                )
            }
        }
    }

    fun searchUsers(query: String) {
        searchJob?.cancel()

        state = state.copy(
            searchQuery = query,
            errorMessage = null
        )

        if (query.isBlank()) {
            loadAllUsers()
            return
        }

        state = state.copy(
            isLoading = true,
            currentOffset = 0,
            hasMore = false
        )

        searchJob = scope.launch {
            delay(300)

            val filters = listOf(QueryFilter("email", FilterOperator.ILIKE, "%$query%"))
            val result = fetchUsersWithRoles(filters = filters, offset = 0, limit = state.pageSize)

            result.onSuccess { (users, hasMore) ->
                state = state.copy(
                    allUsers = users,
                    filteredUsers = users,
                    isLoading = false,
                    currentOffset = users.size,
                    hasMore = hasMore
                )
            }.onFailure { exception ->
                state = state.copy(
                    isLoading = false,
                    errorMessage = exception.message ?: "Unknown error"
                )
            }
        }
    }

    private fun loadMoreSearchResults() {
        if (state.isLoadingMore || !state.hasMore || state.isLoading) {
            return
        }

        state = state.copy(isLoadingMore = true, errorMessage = null)

        scope.launch {
            val filters = listOf(QueryFilter("email", FilterOperator.ILIKE, "%${state.searchQuery}%"))
            val result = fetchUsersWithRoles(
                filters = filters,
                offset = state.currentOffset,
                limit = state.pageSize
            )

            result.onSuccess { (newUsers, hasMore) ->
                val allUsers = state.allUsers + newUsers

                state = state.copy(
                    allUsers = allUsers,
                    filteredUsers = allUsers,
                    isLoadingMore = false,
                    currentOffset = state.currentOffset + newUsers.size,
                    hasMore = hasMore
                )
            }.onFailure { exception ->
                state = state.copy(
                    isLoadingMore = false,
                    errorMessage = exception.message ?: "Unknown error"
                )
            }
        }
    }

    fun assignRole(userId: String, roleName: String) {
        state = state.copy(isOperationInProgress = true, errorMessage = null)

        scope.launch {
            val result = dataProvider.rpc(
                function = "assign_role_to_user",
                parameters = """{"target_user_id":"$userId","role_name":"$roleName"}"""
            )

            if (result.isSuccess) {
                state = state.copy(
                    isOperationInProgress = false,
                    successMessage = "Role $roleName assigned successfully"
                )
                refreshCurrentView()
            } else {
                val error = result.exceptionOrNull()?.message ?: "Unknown error"
                state = state.copy(
                    isOperationInProgress = false,
                    errorMessage = "Failed to assign role: $error"
                )
            }
        }
    }

    fun removeRole(userId: String, roleName: String) {
        state = state.copy(isOperationInProgress = true, errorMessage = null)

        scope.launch {
            val result = dataProvider.rpc(
                function = "remove_role_from_user",
                parameters = """{"target_user_id":"$userId","role_name":"$roleName"}"""
            )

            if (result.isSuccess) {
                state = state.copy(
                    isOperationInProgress = false,
                    successMessage = "Role $roleName removed successfully"
                )
                refreshCurrentView()
            } else {
                val error = result.exceptionOrNull()?.message ?: "Unknown error"
                state = state.copy(
                    isOperationInProgress = false,
                    errorMessage = "Failed to remove role: $error"
                )
            }
        }
    }

    fun deleteUser(userId: String) {
        state = state.copy(isOperationInProgress = true, errorMessage = null)

        scope.launch {
            val result = dataProvider.rpc(
                function = "delete_user",
                parameters = """{"target_user_id":"$userId"}"""
            )

            if (result.isSuccess) {
                state = state.copy(
                    isOperationInProgress = false,
                    successMessage = "User deleted successfully"
                )
                refreshCurrentView()
            } else {
                val error = result.exceptionOrNull()?.message ?: "Unknown error"
                state = state.copy(
                    isOperationInProgress = false,
                    errorMessage = "Failed to delete user: $error"
                )
            }
        }
    }

    fun selectUser(user: UserWithRoles) {
        state = state.copy(selectedUser = user)
    }

    fun clearSelectedUser() {
        state = state.copy(selectedUser = null)
    }

    fun showAssignRoleDialog(user: UserWithRoles) {
        state = state.copy(
            selectedUser = user,
            showAssignRoleDialog = true
        )
    }

    fun hideAssignRoleDialog() {
        state = state.copy(
            showAssignRoleDialog = false,
            selectedRoleToAssign = null
        )
    }

    fun showRemoveRoleDialog(user: UserWithRoles, roleName: String) {
        state = state.copy(
            selectedUser = user,
            selectedRoleToRemove = roleName,
            showRemoveRoleDialog = true
        )
    }

    fun hideRemoveRoleDialog() {
        state = state.copy(
            showRemoveRoleDialog = false,
            selectedRoleToRemove = null
        )
    }

    fun showDeleteUserDialog(user: UserWithRoles) {
        state = state.copy(
            selectedUser = user,
            showDeleteUserDialog = true
        )
    }

    fun hideDeleteUserDialog() {
        state = state.copy(showDeleteUserDialog = false)
    }

    fun setRoleToAssign(roleName: String) {
        state = state.copy(selectedRoleToAssign = roleName)
    }

    fun getAvailableRolesForUser(user: UserWithRoles): List<String> {
        val userRoleNames = user.roles
        return state.availableRoles
            .map { it.name }
            .filter { roleName -> !userRoleNames.contains(roleName) }
    }

    fun clearSuccessMessage() {
        state = state.copy(successMessage = null)
    }

    fun clearErrorMessage() {
        state = state.copy(errorMessage = null)
    }
}

data class UserWithRoles(
    val id: String,
    val email: String,
    val roles: List<String>
)

data class RoleInfo(
    val id: String,
    val name: String,
    val description: String?
)

data class AdminRoleState(
    val allUsers: List<UserWithRoles> = emptyList(),
    val filteredUsers: List<UserWithRoles> = emptyList(),
    val searchQuery: String = "",
    val isLoading: Boolean = false,
    val isLoadingMore: Boolean = false,
    val isOperationInProgress: Boolean = false,
    val errorMessage: String? = null,
    val successMessage: String? = null,
    val selectedUser: UserWithRoles? = null,
    val showAssignRoleDialog: Boolean = false,
    val showRemoveRoleDialog: Boolean = false,
    val showDeleteUserDialog: Boolean = false,
    val selectedRoleToAssign: String? = null,
    val selectedRoleToRemove: String? = null,
    val availableRoles: List<RoleInfo> = emptyList(),
    val currentOffset: Int = 0,
    val pageSize: Int = 20,
    val hasMore: Boolean = true
)
