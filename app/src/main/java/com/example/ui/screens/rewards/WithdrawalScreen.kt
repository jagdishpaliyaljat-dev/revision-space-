package com.example.ui.screens.rewards
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

import android.widget.Toast
import androidx.compose.animation.*
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavController
import com.example.data.WithdrawalRequest
import com.example.viewmodel.RevisionViewModel
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun WithdrawalScreen(viewModel: RevisionViewModel, navController: NavController) {
    val context = LocalContext.current
    val userProfile by viewModel.currentUserProfile.collectAsStateWithLifecycle()
    val requestList by viewModel.withdrawalRequests.collectAsStateWithLifecycle()

    val currentCoins = userProfile?.totalCoins ?: 0
    val rupeesEquivalent = currentCoins / 100.0

    // Form states
    var selectedMethod by remember { mutableStateOf("UPI") } // "UPI" or "BANK"
    var upiId by remember { mutableStateOf("") }
    var bankAccountName by remember { mutableStateOf("") }
    var bankAccountNumber by remember { mutableStateOf("") }
    var bankIfscCode by remember { mutableStateOf("") }
    var coinAmountText by remember { mutableStateOf("") }

    // Submission states
    var submitLoading by remember { mutableStateOf(false) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Withdraw Rewards", fontWeight = FontWeight.Bold, color = Color(0xFF1E1B4B)) },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Back", tint = Color(0xFF1E1B4B))
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = Color(0xFFF6F7FB))
            )
        },
        containerColor = Color(0xFFF6F7FB)
    ) { padding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Balance Widget
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(24.dp),
                    colors = CardDefaults.cardColors(containerColor = Color(0xFF6C4DFF))
                ) {
                    Column(
                        modifier = Modifier.padding(24.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text("AVAILABLE BALANCE", color = Color.White.copy(alpha = 0.7f), fontSize = 11.sp, fontWeight = FontWeight.Bold, letterSpacing = 1.sp)
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = String.format("%,d Coins", currentCoins),
                            color = Color.White,
                            fontSize = 32.sp,
                            fontWeight = FontWeight.Black
                        )
                        Spacer(modifier = Modifier.height(6.dp))
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(8.dp))
                                .background(Color.White.copy(alpha = 0.15f))
                                .padding(horizontal = 12.dp, vertical = 6.dp)
                        ) {
                            Text(
                                text = String.format("INR Equivalent: ₹%,.2f", rupeesEquivalent),
                                color = Color.White,
                                fontSize = 14.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                }
            }

            // Withdrawal Method and details Form
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(20.dp),
                    colors = CardDefaults.cardColors(containerColor = Color.White),
                    border = BorderStroke(1.dp, Color(0xFFE7EAF3))
                ) {
                    Column(modifier = Modifier.padding(20.dp)) {
                        Text("Payout Setup", fontWeight = FontWeight.Bold, fontSize = 16.sp, color = Color(0xFF1E1B4B))
                        Spacer(modifier = Modifier.height(14.dp))

                        // Method Tabs
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(44.dp)
                                .clip(RoundedCornerShape(12.dp))
                                .background(Color(0xFFF1F3F9)),
                            horizontalArrangement = Arrangement.SpaceEvenly,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Box(
                                modifier = Modifier
                                    .weight(1f)
                                    .fillMaxHeight()
                                    .clickable { selectedMethod = "UPI" }
                                    .background(if (selectedMethod == "UPI") Color(0xFF6C4DFF) else Color.Transparent),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    "UPI (GPay/PhonePe)",
                                    color = if (selectedMethod == "UPI") Color.White else Color(0xFF5B6478),
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 13.sp
                                )
                            }
                            Box(
                                modifier = Modifier
                                    .weight(1f)
                                    .fillMaxHeight()
                                    .clickable { selectedMethod = "BANK" }
                                    .background(if (selectedMethod == "BANK") Color(0xFF6C4DFF) else Color.Transparent),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    "Bank Transfer",
                                    color = if (selectedMethod == "BANK") Color.White else Color(0xFF5B6478),
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 13.sp
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(16.dp))

                        // Render Fields
                        if (selectedMethod == "UPI") {
                            Text("UPI ID Address", fontSize = 12.sp, color = Color(0xFF5B6478), fontWeight = FontWeight.Bold)
                            Spacer(modifier = Modifier.height(6.dp))
                            OutlinedTextField(
                                value = upiId,
                                onValueChange = { upiId = it.trim() },
                                placeholder = { Text("e.g. mobile@ybl") },
                                modifier = Modifier.fillMaxWidth(),
                                singleLine = true,
                                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email),
                                colors = OutlinedTextFieldDefaults.colors(
                                    focusedBorderColor = Color(0xFF6C4DFF),
                                    unfocusedBorderColor = Color(0xFFE7EAF3)
                                ),
                                shape = RoundedCornerShape(12.dp)
                            )
                        } else {
                            Text("Account Holder Name", fontSize = 12.sp, color = Color(0xFF5B6478), fontWeight = FontWeight.Bold)
                            Spacer(modifier = Modifier.height(4.dp))
                            OutlinedTextField(
                                value = bankAccountName,
                                onValueChange = { bankAccountName = it },
                                placeholder = { Text("John Doe") },
                                modifier = Modifier.fillMaxWidth(),
                                singleLine = true,
                                colors = OutlinedTextFieldDefaults.colors(
                                    focusedBorderColor = Color(0xFF6C4DFF),
                                    unfocusedBorderColor = Color(0xFFE7EAF3)
                                ),
                                shape = RoundedCornerShape(12.dp)
                            )

                            Spacer(modifier = Modifier.height(10.dp))

                            Text("Bank Account Number", fontSize = 12.sp, color = Color(0xFF5B6478), fontWeight = FontWeight.Bold)
                            Spacer(modifier = Modifier.height(4.dp))
                            OutlinedTextField(
                                value = bankAccountNumber,
                                onValueChange = { bankAccountNumber = it.trim() },
                                placeholder = { Text("123456789012") },
                                modifier = Modifier.fillMaxWidth(),
                                singleLine = true,
                                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                                colors = OutlinedTextFieldDefaults.colors(
                                    focusedBorderColor = Color(0xFF6C4DFF),
                                    unfocusedBorderColor = Color(0xFFE7EAF3)
                                ),
                                shape = RoundedCornerShape(12.dp)
                            )

                            Spacer(modifier = Modifier.height(10.dp))

                            Text("IFSC Code", fontSize = 12.sp, color = Color(0xFF5B6478), fontWeight = FontWeight.Bold)
                            Spacer(modifier = Modifier.height(4.dp))
                            OutlinedTextField(
                                value = bankIfscCode,
                                onValueChange = { bankIfscCode = it.uppercase().trim() },
                                placeholder = { Text("SBIN0001234") },
                                modifier = Modifier.fillMaxWidth(),
                                singleLine = true,
                                colors = OutlinedTextFieldDefaults.colors(
                                    focusedBorderColor = Color(0xFF6C4DFF),
                                    unfocusedBorderColor = Color(0xFFE7EAF3)
                                ),
                                shape = RoundedCornerShape(12.dp)
                            )
                        }

                        Spacer(modifier = Modifier.height(16.dp))

                        // Amount to withdraw conversion
                        Text("Withdrawal Coins Amount", fontSize = 12.sp, color = Color(0xFF5B6478), fontWeight = FontWeight.Bold)
                        Spacer(modifier = Modifier.height(6.dp))
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            OutlinedTextField(
                                value = coinAmountText,
                                onValueChange = { coinAmountText = it.filter { char -> char.isDigit() } },
                                placeholder = { Text("e.g. 100000") },
                                modifier = Modifier.weight(1f),
                                singleLine = true,
                                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                                colors = OutlinedTextFieldDefaults.colors(
                                    focusedBorderColor = Color(0xFF6C4DFF),
                                    unfocusedBorderColor = Color(0xFFE7EAF3)
                                ),
                                shape = RoundedCornerShape(12.dp)
                            )
                        }

                        // Conversion Indicator Text
                        val coinsValue = coinAmountText.toIntOrNull() ?: 0
                        val computedInr = coinsValue / 100.0
                        if (coinsValue > 0) {
                            Spacer(modifier = Modifier.height(6.dp))
                            Text(
                                "Estimated payout value: ₹${String.format("%,.2f", computedInr)} INR",
                                color = Color(0xFF10B981),
                                fontWeight = FontWeight.Bold,
                                fontSize = 12.sp
                            )
                        }

                        Spacer(modifier = Modifier.height(18.dp))

                        // Primary Submit Button 
                        Button(
                            onClick = {
                                if (coinsValue < 100000) {
                                    Toast.makeText(context, "Minimum withdrawal is 100,000 Coins (₹1,000)", Toast.LENGTH_LONG).show()
                                    return@Button
                                }
                                if (coinsValue > currentCoins) {
                                    Toast.makeText(context, "Insufficient coin balance", Toast.LENGTH_LONG).show()
                                    return@Button
                                }
                                if (selectedMethod == "UPI" && upiId.isBlank()) {
                                    Toast.makeText(context, "Please enter a valid UPI ID Address", Toast.LENGTH_SHORT).show()
                                    return@Button
                                }
                                if (selectedMethod == "BANK" && (bankAccountName.isBlank() || bankAccountNumber.isBlank() || bankIfscCode.isBlank())) {
                                    Toast.makeText(context, "Please complete all Bank Account detail fields", Toast.LENGTH_SHORT).show()
                                    return@Button
                                }

                                submitLoading = true
                                val req = WithdrawalRequest(
                                    id = "",
                                    userId = "",
                                    userName = userProfile?.name ?: "User",
                                    userEmail = userProfile?.email ?: "",
                                    amountInInr = computedInr.toInt(),
                                    coinsDeducted = coinsValue,
                                    method = selectedMethod,
                                    upiId = if (selectedMethod == "UPI") upiId else "",
                                    bankAccountName = if (selectedMethod == "BANK") bankAccountName else "",
                                    bankAccountNumber = if (selectedMethod == "BANK") bankAccountNumber else "",
                                    bankIfscCode = if (selectedMethod == "BANK") bankIfscCode else "",
                                    status = "PENDING"
                                )

                                viewModel.submitWithdrawalRequest(req) { success ->
                                    submitLoading = false
                                    if (success) {
                                        Toast.makeText(context, "Withdrawal request submitted successfully!", Toast.LENGTH_LONG).show()
                                        coinAmountText = ""
                                        upiId = ""
                                        bankAccountName = ""
                                        bankAccountNumber = ""
                                        bankIfscCode = ""
                                    } else {
                                        Toast.makeText(context, "Transaction execution failure. Please try again.", Toast.LENGTH_SHORT).show()
                                    }
                                }
                            },
                            modifier = Modifier.fillMaxWidth(),
                            enabled = !submitLoading,
                            colors = ButtonDefaults.buttonColors(
                                containerColor = Color(0xFF6C4DFF),
                                disabledContainerColor = Color(0xFFE7EAF3)
                            ),
                            shape = RoundedCornerShape(12.dp)
                        ) {
                            if (submitLoading) {
                                CircularProgressIndicator(color = Color.White, modifier = Modifier.size(20.dp))
                            } else {
                                Text("Submit Withdrawal Request", fontWeight = FontWeight.Bold, fontSize = 14.sp)
                            }
                        }

                        // Conditions help text
                        Spacer(modifier = Modifier.height(10.dp))
                        Text(
                            text = "Note: Min 100,000 Coins (₹1,000). Manual payout audits require up to 48 hours for processing.",
                            fontSize = 11.sp,
                            color = Color(0xFF5B6478),
                            textAlign = TextAlign.Center,
                            modifier = Modifier.fillMaxWidth()
                        )
                    }
                }
            }

            // Withdrawal Requests List
            item {
                Text(
                    text = "Request History & Status",
                    modifier = Modifier.padding(top = 10.dp),
                    fontWeight = FontWeight.Bold,
                    fontSize = 16.sp,
                    color = Color(0xFF1E1B4B)
                )
            }

            if (requestList.isEmpty()) {
                item {
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(16.dp),
                        colors = CardDefaults.cardColors(containerColor = Color.White),
                        border = BorderStroke(1.dp, Color(0xFFF1F3F9))
                    ) {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(36.dp),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Icon(Icons.Outlined.ReceiptLong, contentDescription = null, tint = Color(0xFF8A94A6), modifier = Modifier.size(44.dp))
                            Spacer(modifier = Modifier.height(12.dp))
                            Text("No withdrawals requested yet", fontWeight = FontWeight.SemiBold, color = Color(0xFF5B6478), fontSize = 14.sp)
                            Spacer(modifier = Modifier.height(4.dp))
                            Text("Earn coins by studying and refer friends to cash out!", color = Color(0xFF8A94A6), textAlign = TextAlign.Center, fontSize = 12.sp)
                        }
                    }
                }
            } else {
                items(requestList) { item ->
                    WithdrawalRequestRow(item)
                }
            }
        }
    }
}


@Composable
fun WithdrawalRequestRow(req: WithdrawalRequest) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        border = BorderStroke(1.dp, Color(0xFFF1F3F9))
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Column {
                    Text("Requested Amount", color = Color(0xFF5B6478), fontSize = 11.sp)
                    Text("₹${String.format("%,d", req.amountInInr)} INR", fontWeight = FontWeight.Black, fontSize = 18.sp, color = Color(0xFF0F172A))
                }

                val (badgeBg, badgeText, statusLabel) = when (req.status) {
                    "APPROVED" -> Triple(Color(0xFF10B981).copy(alpha = 0.12f), Color(0xFF10B981), "APPROVED")
                    "REJECTED" -> Triple(Color(0xFFEF4444).copy(alpha = 0.1f), Color(0xFFEF4444), "REJECTED")
                    else -> Triple(Color(0xFFF59E0B).copy(alpha = 0.15f), Color(0xFFF59E0B), "PENDING")
                }
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(8.dp))
                        .background(badgeBg)
                        .padding(horizontal = 8.dp, vertical = 4.dp)
                ) {
                    Text(statusLabel, fontWeight = FontWeight.Black, color = badgeText, fontSize = 10.sp)
                }
            }

            Spacer(modifier = Modifier.height(12.dp))
            HorizontalDivider(color = Color(0xFFF1F3F9))
            Spacer(modifier = Modifier.height(10.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        text = "Transfer Method: ${req.method}",
                        fontSize = 12.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = Color(0xFF5B6478)
                    )
                    Text(
                        text = if (req.method == "UPI") "UPI ID: ${req.upiId}" else "Acc: ${req.bankAccountNumber}",
                        fontSize = 11.sp,
                        color = Color(0xFF8A94A6)
                    )
                }

                Text(
                    text = SimpleDateFormat("dd MMM, hh:mm a", Locale.getDefault()).format(Date(req.createdAtMs)),
                    fontSize = 11.sp,
                    color = Color(0xFF8A94A6)
                )
            }

            if (req.status == "REJECTED" && req.rejectReason.isNotEmpty()) {
                Spacer(modifier = Modifier.height(10.dp))
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(8.dp))
                        .background(Color(0xFFEF4444).copy(alpha = 0.05f))
                        .padding(8.dp)
                ) {
                    Text(
                        "Reject Reason: ${req.rejectReason}",
                        color = Color(0xFFEF4444),
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        }
    }
}
