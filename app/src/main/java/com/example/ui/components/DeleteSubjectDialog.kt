package com.example.ui.components
import com.example.ui.*

import com.example.ui.screens.auth.*
import com.example.ui.screens.home.*
import com.example.ui.screens.revision.*
import com.example.ui.screens.subject.*
import com.example.ui.screens.profile.*
import com.example.ui.screens.rewards.*
import com.example.ui.screens.settings.*
import com.example.ui.screens.admin.*
import com.example.ui.components.*
import com.example.ui.utils.*

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.size
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp

@Composable
fun DeleteSubjectDialog(
    onDismissRequest: () -> Unit,
    onConfirm: () -> Unit,
    isDeleting: Boolean = false
) {
    AlertDialog(
        onDismissRequest = {
            if (!isDeleting) onDismissRequest()
        },
        title = { Text("Delete Subject?", color = MaterialTheme.colorScheme.error, style = MaterialTheme.typography.titleLarge) },
        text = { 
             Text(
                "This action cannot be undone.\n\nDeleting this subject will permanently remove\n• Subject\n• Chapters\n• Topics\n• Revision History\n• Pending Revisions\n• Completed Revisions\n• Study Time\n• Progress\n• Statistics\n• Notifications\n• To-do entries",
                style = MaterialTheme.typography.bodyMedium
            ) 
         },
        confirmButton = {
            Button(
                onClick = onConfirm,
                enabled = !isDeleting,
                colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error)
            ) { 
                if (isDeleting) {
                    Box(modifier = Modifier.size(24.dp)) {
                        CircularProgressIndicator(
                            color = MaterialTheme.colorScheme.onError,
                            strokeWidth = 2.dp
                        )
                    }
                } else {
                    Text("Delete") 
                }
            }
        },
        dismissButton = { 
            TextButton(onClick = onDismissRequest, enabled = !isDeleting) { Text("Cancel") } 
        }
    )
}
