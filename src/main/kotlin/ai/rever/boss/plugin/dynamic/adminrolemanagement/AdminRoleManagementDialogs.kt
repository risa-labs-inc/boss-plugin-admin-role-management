package ai.rever.boss.plugin.dynamic.adminrolemanagement

import ai.rever.boss.plugin.api.UserWithRolesData
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material.icons.filled.Delete
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog

@Composable
fun AssignRoleDialog(
    user: UserWithRolesData,
    availableRoles: List<String>,
    selectedRole: String?,
    onRoleSelected: (String) -> Unit,
    onConfirm: () -> Unit,
    onDismiss: () -> Unit,
    isLoading: Boolean
) {
    Dialog(onDismissRequest = { if (!isLoading) onDismiss() }) {
        Surface(
            shape = RoundedCornerShape(8.dp),
            color = Color(0xFF3C3F41),
            modifier = Modifier.width(400.dp)
        ) {
            Column(modifier = Modifier.padding(24.dp)) {
                Text(
                    "Assign Role",
                    color = Color.White,
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold
                )

                Spacer(modifier = Modifier.height(8.dp))

                Text(
                    "User: ${user.email}",
                    color = Color.Gray,
                    fontSize = 14.sp
                )

                Spacer(modifier = Modifier.height(16.dp))

                if (availableRoles.isEmpty()) {
                    Text(
                        "This user already has all available roles.",
                        color = Color.Gray,
                        fontSize = 14.sp
                    )
                    Spacer(modifier = Modifier.height(16.dp))
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.End
                    ) {
                        Button(
                            onClick = onDismiss,
                            colors = ButtonDefaults.buttonColors(
                                backgroundColor = Color(0xFF4A90E2)
                            )
                        ) {
                            Text("Close", color = Color.White)
                        }
                    }
                } else {
                    Text(
                        "Select Role:",
                        color = Color.White,
                        fontSize = 14.sp,
                        modifier = Modifier.padding(bottom = 8.dp)
                    )

                    RoleDropdown(
                        roles = availableRoles,
                        selectedRole = selectedRole,
                        onRoleSelected = onRoleSelected
                    )

                    Spacer(modifier = Modifier.height(24.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp, Alignment.End)
                    ) {
                        TextButton(
                            onClick = onDismiss,
                            enabled = !isLoading
                        ) {
                            Text("Cancel", color = Color.Gray)
                        }

                        Button(
                            onClick = onConfirm,
                            enabled = !isLoading && selectedRole != null,
                            colors = ButtonDefaults.buttonColors(
                                backgroundColor = Color(0xFF4A90E2),
                                disabledBackgroundColor = Color.Gray
                            )
                        ) {
                            if (isLoading) {
                                CircularProgressIndicator(
                                    modifier = Modifier.size(16.dp),
                                    color = Color.White,
                                    strokeWidth = 2.dp
                                )
                            } else {
                                Text("Assign", color = Color.White)
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun RoleDropdown(
    roles: List<String>,
    selectedRole: String?,
    onRoleSelected: (String) -> Unit
) {
    var expanded by remember { mutableStateOf(false) }

    Box {
        Surface(
            modifier = Modifier
                .fillMaxWidth()
                .clickable { expanded = true },
            shape = RoundedCornerShape(4.dp),
            color = Color(0xFF2B2D30)
        ) {
            Row(
                modifier = Modifier.padding(12.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    selectedRole ?: "Select a role...",
                    color = if (selectedRole != null) Color.White else Color.Gray,
                    fontSize = 14.sp
                )
                Icon(
                    Icons.Default.KeyboardArrowDown,
                    contentDescription = "Expand",
                    tint = Color.Gray,
                    modifier = Modifier.size(16.dp)
                )
            }
        }

        DropdownMenu(
            expanded = expanded,
            onDismissRequest = { expanded = false },
            modifier = Modifier.background(Color(0xFF3C3F41))
        ) {
            roles.forEach { roleName ->
                DropdownMenuItem(
                    onClick = {
                        onRoleSelected(roleName)
                        expanded = false
                    }
                ) {
                    Text(roleName, color = Color.White, fontSize = 14.sp)
                }
            }
        }
    }
}

@Composable
fun RemoveRoleConfirmationDialog(
    user: UserWithRolesData,
    roleName: String,
    onConfirm: () -> Unit,
    onDismiss: () -> Unit,
    isLoading: Boolean
) {
    Dialog(onDismissRequest = { if (!isLoading) onDismiss() }) {
        Surface(
            shape = RoundedCornerShape(8.dp),
            color = Color(0xFF3C3F41),
            modifier = Modifier.width(400.dp)
        ) {
            Column(modifier = Modifier.padding(24.dp)) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.padding(bottom = 16.dp)
                ) {
                    Icon(
                        Icons.Default.Warning,
                        contentDescription = "Warning",
                        tint = Color(0xFFFF9800),
                        modifier = Modifier.size(24.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        "Remove Role",
                        color = Color.White,
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold
                    )
                }

                Text(
                    "Are you sure you want to remove the \"$roleName\" role from this user?",
                    color = Color.White,
                    fontSize = 14.sp
                )

                Spacer(modifier = Modifier.height(8.dp))

                Text(
                    "User: ${user.email}",
                    color = Color.Gray,
                    fontSize = 12.sp
                )

                Spacer(modifier = Modifier.height(24.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp, Alignment.End)
                ) {
                    TextButton(
                        onClick = onDismiss,
                        enabled = !isLoading
                    ) {
                        Text("Cancel", color = Color.Gray)
                    }

                    Button(
                        onClick = onConfirm,
                        enabled = !isLoading,
                        colors = ButtonDefaults.buttonColors(
                            backgroundColor = Color(0xFFE91E63),
                            disabledBackgroundColor = Color.Gray
                        )
                    ) {
                        if (isLoading) {
                            CircularProgressIndicator(
                                modifier = Modifier.size(16.dp),
                                color = Color.White,
                                strokeWidth = 2.dp
                            )
                        } else {
                            Text("Remove", color = Color.White)
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun DeleteUserConfirmationDialog(
    user: UserWithRolesData,
    onConfirm: () -> Unit,
    onDismiss: () -> Unit,
    isLoading: Boolean
) {
    Dialog(onDismissRequest = { if (!isLoading) onDismiss() }) {
        Surface(
            shape = RoundedCornerShape(16.dp),
            color = Color(0xFF2B2D30)
        ) {
            Column(
                modifier = Modifier
                    .padding(24.dp)
                    .width(400.dp)
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Icon(
                        Icons.Default.Warning,
                        contentDescription = "Warning",
                        tint = Color(0xFFE91E63),
                        modifier = Modifier.size(24.dp)
                    )
                    Text(
                        "Delete User",
                        color = Color.White,
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold
                    )
                }

                Spacer(modifier = Modifier.height(16.dp))

                Text(
                    "Are you sure you want to delete this user? This action cannot be undone.",
                    color = Color.White,
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Medium
                )

                Spacer(modifier = Modifier.height(8.dp))

                Text(
                    "User: ${user.email}",
                    color = Color.Gray,
                    fontSize = 12.sp
                )

                Spacer(modifier = Modifier.height(8.dp))

                Text(
                    "This will delete:",
                    color = Color(0xFFE91E63),
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Medium
                )
                Text(
                    "• User account\n• All role assignments\n• User data",
                    color = Color.Gray,
                    fontSize = 12.sp,
                    modifier = Modifier.padding(start = 8.dp, top = 4.dp)
                )

                Spacer(modifier = Modifier.height(24.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp, Alignment.End)
                ) {
                    TextButton(
                        onClick = onDismiss,
                        enabled = !isLoading
                    ) {
                        Text("Cancel", color = Color.Gray)
                    }

                    Button(
                        onClick = onConfirm,
                        enabled = !isLoading,
                        colors = ButtonDefaults.buttonColors(
                            backgroundColor = Color(0xFFE91E63),
                            disabledBackgroundColor = Color.Gray
                        )
                    ) {
                        if (isLoading) {
                            CircularProgressIndicator(
                                modifier = Modifier.size(16.dp),
                                color = Color.White,
                                strokeWidth = 2.dp
                            )
                        } else {
                            Row(
                                horizontalArrangement = Arrangement.spacedBy(4.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Icon(
                                    Icons.Default.Delete,
                                    contentDescription = "Delete",
                                    modifier = Modifier.size(16.dp),
                                    tint = Color.White
                                )
                                Text("Delete", color = Color.White)
                            }
                        }
                    }
                }
            }
        }
    }
}
