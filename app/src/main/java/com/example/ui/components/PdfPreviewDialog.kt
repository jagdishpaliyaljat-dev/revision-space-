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

import android.graphics.pdf.PdfRenderer
import android.os.ParcelFileDescriptor
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.File

@Composable
fun PdfPreviewDialog(
    pdfFile: File,
    onDismiss: () -> Unit,
    onConfirm: () -> Unit
) {
    var bitmaps by remember { mutableStateOf<List<android.graphics.Bitmap>>(emptyList()) }
    var isLoading by remember { mutableStateOf(true) }
    
    LaunchedEffect(pdfFile) {
        withContext(Dispatchers.IO) {
            try {
                val fd = ParcelFileDescriptor.open(pdfFile, ParcelFileDescriptor.MODE_READ_ONLY)
                val renderer = PdfRenderer(fd)
                val result = mutableListOf<android.graphics.Bitmap>()
                for (i in 0 until renderer.pageCount) {
                    val page = renderer.openPage(i)
                    // Increase resolution by 2x for better preview quality
                    val bitmap = android.graphics.Bitmap.createBitmap(
                        page.width * 2, page.height * 2, android.graphics.Bitmap.Config.ARGB_8888
                    )
                    bitmap.eraseColor(android.graphics.Color.WHITE)
                    page.render(bitmap, null, null, PdfRenderer.Page.RENDER_MODE_FOR_DISPLAY)
                    result.add(bitmap)
                    page.close()
                }
                renderer.close()
                fd.close()
                bitmaps = result
            } catch (e: Exception) {
                e.printStackTrace()
            } finally {
                isLoading = false
            }
        }
    }
    
    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Surface(
            modifier = Modifier.fillMaxSize().padding(16.dp),
            shape = RoundedCornerShape(16.dp),
            color = MaterialTheme.colorScheme.surface
        ) {
            Column(modifier = Modifier.fillMaxSize()) {
                Text(
                    "Report Preview", 
                    style = MaterialTheme.typography.titleLarge,
                    modifier = Modifier.padding(16.dp)
                )
                
                if (isLoading) {
                    Box(modifier = Modifier.weight(1f).fillMaxWidth(), contentAlignment = Alignment.Center) {
                        CircularProgressIndicator()
                    }
                } else if (bitmaps.isEmpty()) {
                    Box(modifier = Modifier.weight(1f).fillMaxWidth(), contentAlignment = Alignment.Center) {
                        Text("Failed to load PDF preview")
                    }
                } else {
                    LazyColumn(
                        modifier = Modifier.weight(1f).fillMaxWidth().padding(horizontal = 16.dp),
                        verticalArrangement = Arrangement.spacedBy(16.dp)
                    ) {
                        items(bitmaps) { bmp ->
                            Image(
                                bitmap = bmp.asImageBitmap(),
                                contentDescription = "PDF Page",
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .border(1.dp, Color.LightGray)
                            )
                        }
                    }
                }
                
                Row(
                    modifier = Modifier.fillMaxWidth().padding(16.dp),
                    horizontalArrangement = Arrangement.End
                ) {
                    TextButton(onClick = onDismiss) {
                        Text("Cancel")
                    }
                    Spacer(modifier = Modifier.width(8.dp))
                    Button(onClick = onConfirm) {
                        Text("Share Report")
                    }
                }
            }
        }
    }
}
