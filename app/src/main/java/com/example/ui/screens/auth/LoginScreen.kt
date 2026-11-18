package com.example.ui.screens.auth

import android.content.Context
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.School
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.BuildConfig
import com.example.ui.theme.AppPalette
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.android.gms.common.api.ApiException

@Composable
fun LoginScreen(
    onSignedIn: (String) -> Unit,
    onGuestContinue: () -> Unit,
    onSignInFailed: (String) -> Unit = {},
    isLoading: Boolean = false,
    errorMessage: String? = null
) {
    val context = LocalContext.current
    val gso = remember {
        GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
            .requestIdToken(WEB_CLIENT_ID)
            .requestEmail()
            .build()
    }
    val googleSignInClient = remember { GoogleSignIn.getClient(context, gso) }

    val launcher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.StartActivityForResult()
    ) { result ->
        try {
            val task = GoogleSignIn.getSignedInAccountFromIntent(result.data)
            val account = task.getResult(ApiException::class.java)
            val idToken = account.idToken
            if (idToken != null) {
                onSignedIn(idToken)
            } else {
                onSignInFailed("Google didn't return a sign-in token. Please try again, or continue as guest.")
            }
        } catch (e: ApiException) {
            onSignInFailed("Google sign-in failed (code ${e.statusCode}). You can still continue as guest.")
        } catch (e: Exception) {
            onSignInFailed("Sign-in failed: ${e.localizedMessage ?: "unknown error"}. You can still continue as guest.")
        }
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Brush.verticalGradient(listOf(AppPalette.Brand, AppPalette.BrandDeep))),
        contentAlignment = Alignment.Center
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(32.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Box(
                modifier = Modifier
                    .size(96.dp)
                    .clip(RoundedCornerShape(28.dp))
                    .background(Color.White.copy(alpha = 0.18f)),
                contentAlignment = Alignment.Center
            ) {
                Icon(Icons.Default.School, contentDescription = null, tint = Color.White, modifier = Modifier.size(52.dp))
            }
            Spacer(modifier = Modifier.height(24.dp))
            Text(
                text = "Revision Space",
                color = Color.White,
                fontWeight = FontWeight.Black,
                fontSize = 30.sp,
                letterSpacing = 1.sp
            )
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = "Revise Smarter. Achieve Faster.",
                color = Color.White.copy(alpha = 0.85f),
                fontSize = 15.sp,
                textAlign = TextAlign.Center
            )
            Spacer(modifier = Modifier.height(48.dp))

            if (errorMessage != null) {
                Text(
                    text = errorMessage,
                    color = Color(0xFFFFDAD6),
                    fontSize = 13.sp,
                    textAlign = TextAlign.Center,
                    modifier = Modifier.padding(bottom = 16.dp)
                )
            }

            Button(
                onClick = {
                    try {
                        launcher.launch(googleSignInClient.signInIntent)
                    } catch (e: Exception) {
                        onSignInFailed("Could not start Google sign-in. Please continue as guest.")
                    }
                },
                enabled = !isLoading,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(54.dp),
                shape = RoundedCornerShape(16.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = Color.White,
                    contentColor = AppPalette.Brand
                )
            ) {
                if (isLoading) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(22.dp),
                        color = AppPalette.Brand,
                        strokeWidth = 2.dp
                    )
                } else {
                    Text("Continue with Google", fontWeight = FontWeight.Bold, fontSize = 15.sp)
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            TextButton(
                onClick = onGuestContinue,
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("Continue as Guest", color = Color.White, fontWeight = FontWeight.SemiBold, fontSize = 14.sp)
            }

            Spacer(modifier = Modifier.height(16.dp))
            Text(
                text = "By continuing you agree to our Terms & Privacy Policy.",
                color = Color.White.copy(alpha = 0.6f),
                fontSize = 11.sp,
                textAlign = TextAlign.Center
            )
        }
    }
}

// Real OAuth Web Client ID from Firebase Console, injected via .env / .env.example.
// The old placeholder value made every Google sign-in attempt fail silently.
private val WEB_CLIENT_ID: String = BuildConfig.FIREBASE_WEB_CLIENT_ID