package com.mycodecalendar.feature.onboarding

import android.app.Activity
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.*
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.scale
import androidx.compose.ui.graphics.vector.PathParser
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.android.gms.common.api.ApiException
import com.google.firebase.FirebaseNetworkException
import com.google.firebase.auth.*
import com.mycodecalendar.core.designsystem.BrandPrimaryOrange
import com.mycodecalendar.core.designsystem.GlassmorphismBackground
import com.mycodecalendar.core.designsystem.Typography
import com.mycodecalendar.core.designsystem.components.CyberLoadingSpinner
import com.mycodecalendar.core.designsystem.components.GlassCard
import com.mycodecalendar.core.designsystem.isAppInDarkTheme
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

/**
 * Authentic Google 'G' Logo rendered via vector path.
 */
@Composable
fun GoogleLogo(modifier: Modifier = Modifier) {
    Canvas(modifier = modifier) {
        val w = size.width
        val scaleRatio = w / 24f

        val bluePath = PathParser().parsePathString(
            "M22.56 12.25c0-.78-.07-1.53-.2-2.25H12v4.26h5.92c-.26 1.37-1.04 2.53-2.21 3.31v2.77h3.57c2.08-1.92 3.28-4.74 3.28-8.09z"
        ).toPath()
        val greenPath = PathParser().parsePathString(
            "M12 23c2.97 0 5.46-.98 7.28-2.66l-3.57-2.77c-.98.66-2.23 1.06-3.71 1.06-2.86 0-5.29-1.93-6.16-4.53H2.18v2.84C3.99 20.53 7.7 23 12 23z"
        ).toPath()
        val yellowPath = PathParser().parsePathString(
            "M5.84 14.09c-.22-.66-.35-1.36-.35-2.09s.13-1.43.35-2.09V7.06H2.18C1.43 8.55 1 10.22 1 12s.43 3.45 1.18 4.94l2.85-2.22.81-.63z"
        ).toPath()
        val redPath = PathParser().parsePathString(
            "M12 5.38c1.62 0 3.06.56 4.21 1.64l3.15-3.15C17.45 2.09 14.97 1 12 1 7.7 1 3.99 3.47 2.18 7.06l3.66 2.84c.87-2.6 3.3-4.52 6.16-4.52z"
        ).toPath()

        scale(scaleRatio, scaleRatio, pivot = Offset.Zero) {
            drawPath(bluePath, color = Color(0xFF4285F4))
            drawPath(greenPath, color = Color(0xFF34A853))
            drawPath(yellowPath, color = Color(0xFFFBBC05))
            drawPath(redPath, color = Color(0xFFEA4335))
        }
    }
}

/**
 * Modern, Minimal & Clean Authentication Screen with Google Sign-In & Email.
 */
@Composable
fun AuthScreen(
    onAuthSuccess: (username: String, method: String, email: String?, photoUrl: String?) -> Unit,
    onGuestBypass: () -> Unit
) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    val auth = remember { FirebaseAuth.getInstance() }
    val isDark = isAppInDarkTheme

    var selectedTab by remember { mutableStateOf(0) } // 0 = Sign In, 1 = Create Account

    // Form inputs
    var name by remember { mutableStateOf("") }
    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var isPasswordVisible by remember { mutableStateOf(false) }

    var showForgotPasswordDialog by remember { mutableStateOf(false) }
    var showNoInternetDialog by remember { mutableStateOf(false) }
    var resetEmail by remember { mutableStateOf("") }
    var resetLoading by remember { mutableStateOf(false) }
    var resetSuccessMessage by remember { mutableStateOf<String?>(null) }
    var resetErrorMessage by remember { mutableStateOf<String?>(null) }

    var isLoading by remember { mutableStateOf(false) }
    var loadingMessage by remember { mutableStateOf("Connecting…") }
    var errorMessage by remember { mutableStateOf<String?>(null) }

    var showSuccessModal by remember { mutableStateOf(false) }
    var successName by remember { mutableStateOf("") }

    val brandOrange = BrandPrimaryOrange
    val brandOrangeGrad = listOf(Color(0xFFFF7A00), Color(0xFFFF5200))

    // ── GOOGLE SIGN-IN CLIENT SETUP ─────────────────────────────────────────
    val gso = remember {
        GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
            .requestEmail()
            .requestProfile()
            .build()
    }
    val googleSignInClient = remember { GoogleSignIn.getClient(context, gso) }

    val googleLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.StartActivityForResult()
    ) { result ->
        if (result.resultCode == Activity.RESULT_OK) {
            val task = GoogleSignIn.getSignedInAccountFromIntent(result.data)
            try {
                val account = task.getResult(ApiException::class.java)
                val displayName = account?.displayName?.ifBlank { null }
                    ?: account?.email?.substringBefore("@")?.replaceFirstChar { it.uppercase() }
                    ?: "Developer"
                val userEmail = account?.email
                val userPhoto = account?.photoUrl?.toString()

                loadingMessage = "Welcome, $displayName…"
                auth.signInAnonymously().addOnCompleteListener { authTask ->
                    val user = auth.currentUser
                    user?.updateProfile(
                        UserProfileChangeRequest.Builder()
                            .setDisplayName(displayName)
                            .build()
                    )
                    isLoading = false
                    successName = displayName
                    showSuccessModal = true
                    scope.launch {
                        delay(650)
                        showSuccessModal = false
                        onAuthSuccess(displayName, "Google", userEmail, userPhoto)
                    }
                }
            } catch (e: Exception) {
                isLoading = false
                errorMessage = "Google Sign-In was not completed. Please try again."
            }
        } else {
            isLoading = false
            // User dismissed or closed account chooser cleanly
        }
    }

    GlassmorphismBackground {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .statusBarsPadding()
                .navigationBarsPadding()
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 22.dp, vertical = 18.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Spacer(modifier = Modifier.height(10.dp))

            // ── APP LOGO & HEADER ───────────────────────────────────────────
            Box(
                modifier = Modifier
                    .size(64.dp)
                    .background(
                        Brush.linearGradient(listOf(Color(0xFF1E2235), Color(0xFF121624))),
                        RoundedCornerShape(18.dp)
                    )
                    .border(1.2.dp, brandOrange.copy(alpha = 0.5f), RoundedCornerShape(18.dp))
                    .shadow(12.dp, RoundedCornerShape(18.dp), spotColor = brandOrange.copy(alpha = 0.35f)),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    Icons.Rounded.Code,
                    contentDescription = null,
                    modifier = Modifier.size(32.dp),
                    tint = brandOrange
                )
            }

            Spacer(modifier = Modifier.height(14.dp))

            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(
                    text = "MyCode",
                    style = Typography.headlineMedium.copy(fontWeight = FontWeight.Black),
                    color = MaterialTheme.colorScheme.onBackground
                )
                Text(
                    text = "Calendar",
                    style = Typography.headlineMedium.copy(fontWeight = FontWeight.Black),
                    color = brandOrange
                )
            }

            Spacer(modifier = Modifier.height(4.dp))

            Text(
                text = "Plan. Code. Achieve.",
                style = Typography.labelMedium.copy(
                    fontWeight = FontWeight.Bold,
                    letterSpacing = 1.1.sp
                ),
                color = brandOrange
            )

            Spacer(modifier = Modifier.height(20.dp))

            // ── TAB SELECTOR (Sign In vs Create Account) ────────────────────
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(44.dp)
                    .clip(RoundedCornerShape(12.dp))
                    .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.40f))
                    .border(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.15f), RoundedCornerShape(12.dp))
                    .padding(3.dp)
            ) {
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxHeight()
                        .clip(RoundedCornerShape(10.dp))
                        .then(
                            if (selectedTab == 0) Modifier.background(
                                Brush.horizontalGradient(brandOrangeGrad)
                            )
                            else Modifier.clickable { selectedTab = 0; errorMessage = null }
                        ),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "Sign In",
                        style = Typography.labelMedium.copy(fontWeight = FontWeight.Bold),
                        color = if (selectedTab == 0) Color.White else MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }

                Box(
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxHeight()
                        .clip(RoundedCornerShape(10.dp))
                        .then(
                            if (selectedTab == 1) Modifier.background(
                                Brush.horizontalGradient(brandOrangeGrad)
                            )
                            else Modifier.clickable { selectedTab = 1; errorMessage = null }
                        ),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "Create Account",
                        style = Typography.labelMedium.copy(fontWeight = FontWeight.Bold),
                        color = if (selectedTab == 1) Color.White else MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }

            Spacer(modifier = Modifier.height(18.dp))

            // ── CLEAN AUTH CARD CONTAINER ───────────────────────────────────
            GlassCard(
                modifier = Modifier.fillMaxWidth(),
                cornerRadius = 20.dp,
                accentColor = brandOrange
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(18.dp)
                ) {
                    // ── GOOGLE QUICK SIGN-IN BUTTON (FULL-WIDTH) ─────────────
                    Surface(
                        onClick = {
                            if (!isNetworkConnected(context)) {
                                showNoInternetDialog = true
                                return@Surface
                            }
                            errorMessage = null
                            isLoading = true
                            loadingMessage = "Connecting with Google…"
                            // Always sign out first to force account chooser
                            googleSignInClient.signOut().addOnCompleteListener {
                                try {
                                    googleLauncher.launch(googleSignInClient.signInIntent)
                                } catch (e: Exception) {
                                    isLoading = false
                                    errorMessage = "Could not launch Google Sign-In: ${e.localizedMessage}"
                                }
                            }
                        },
                        shape = RoundedCornerShape(12.dp),
                        color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = if (isDark) 0.45f else 0.85f),
                        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.18f)),
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(48.dp)
                    ) {
                        Row(
                            modifier = Modifier.fillMaxSize(),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.Center
                        ) {
                            GoogleLogo(modifier = Modifier.size(20.dp))
                            Spacer(modifier = Modifier.width(10.dp))
                            Text(
                                text = "Continue with Google",
                                style = Typography.labelLarge.copy(fontWeight = FontWeight.SemiBold),
                                color = MaterialTheme.colorScheme.onSurface
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    // ── OR DIVIDER ──────────────────────────────────────────
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        HorizontalDivider(
                            modifier = Modifier.weight(1f),
                            color = MaterialTheme.colorScheme.outline.copy(alpha = 0.15f)
                        )
                        Text(
                            text = "OR WITH EMAIL",
                            modifier = Modifier.padding(horizontal = 10.dp),
                            style = Typography.labelSmall.copy(fontSize = 10.sp, fontWeight = FontWeight.Bold),
                            color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f)
                        )
                        HorizontalDivider(
                            modifier = Modifier.weight(1f),
                            color = MaterialTheme.colorScheme.outline.copy(alpha = 0.15f)
                        )
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    // ── NAME INPUT (Create Account only) ────────────────────
                    AnimatedVisibility(
                        visible = selectedTab == 1,
                        enter = expandVertically() + fadeIn(),
                        exit = shrinkVertically() + fadeOut()
                    ) {
                        Column(modifier = Modifier.padding(bottom = 12.dp)) {
                            Text(
                                text = "Your Name",
                                style = Typography.labelMedium.copy(fontWeight = FontWeight.SemiBold),
                                color = MaterialTheme.colorScheme.onSurface,
                                modifier = Modifier.padding(bottom = 5.dp, start = 2.dp)
                            )
                            OutlinedTextField(
                                value = name,
                                onValueChange = { name = it; errorMessage = null },
                                placeholder = {
                                    Text(
                                        "e.g. Alex Turing",
                                        style = Typography.bodySmall,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.55f)
                                    )
                                },
                                leadingIcon = {
                                    Icon(Icons.Rounded.Person, null, modifier = Modifier.size(18.dp), tint = brandOrange)
                                },
                                keyboardOptions = KeyboardOptions(
                                    keyboardType = KeyboardType.Text,
                                    imeAction = androidx.compose.ui.text.input.ImeAction.Next
                                ),
                                modifier = Modifier.fillMaxWidth(),
                                shape = RoundedCornerShape(12.dp),
                                singleLine = true,
                                colors = OutlinedTextFieldDefaults.colors(
                                    focusedContainerColor = MaterialTheme.colorScheme.surface.copy(alpha = 0.7f),
                                    unfocusedContainerColor = MaterialTheme.colorScheme.surface.copy(alpha = 0.45f),
                                    focusedBorderColor = brandOrange,
                                    unfocusedBorderColor = MaterialTheme.colorScheme.outline.copy(alpha = 0.20f),
                                    focusedTextColor = MaterialTheme.colorScheme.onSurface,
                                    unfocusedTextColor = MaterialTheme.colorScheme.onSurface,
                                    cursorColor = brandOrange
                                )
                            )
                        }
                    }

                    // ── EMAIL INPUT ─────────────────────────────────────────
                    Text(
                        text = "Email Address",
                        style = Typography.labelMedium.copy(fontWeight = FontWeight.SemiBold),
                        color = MaterialTheme.colorScheme.onSurface,
                        modifier = Modifier.padding(bottom = 5.dp, start = 2.dp)
                    )
                    OutlinedTextField(
                        value = email,
                        onValueChange = { email = it; errorMessage = null },
                        placeholder = {
                            Text(
                                "developer@domain.com",
                                style = Typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.55f)
                            )
                        },
                        leadingIcon = {
                            Icon(Icons.Rounded.Email, null, modifier = Modifier.size(18.dp), tint = brandOrange)
                        },
                        keyboardOptions = KeyboardOptions(
                            keyboardType = KeyboardType.Email,
                            imeAction = androidx.compose.ui.text.input.ImeAction.Next
                        ),
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(12.dp),
                        singleLine = true,
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedContainerColor = MaterialTheme.colorScheme.surface.copy(alpha = 0.7f),
                            unfocusedContainerColor = MaterialTheme.colorScheme.surface.copy(alpha = 0.45f),
                            focusedBorderColor = brandOrange,
                            unfocusedBorderColor = MaterialTheme.colorScheme.outline.copy(alpha = 0.20f),
                            focusedTextColor = MaterialTheme.colorScheme.onSurface,
                            unfocusedTextColor = MaterialTheme.colorScheme.onSurface,
                            cursorColor = brandOrange
                        )
                    )

                    Spacer(modifier = Modifier.height(12.dp))

                    // ── PASSWORD INPUT ──────────────────────────────────────
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(bottom = 5.dp, start = 2.dp, end = 2.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "Password",
                            style = Typography.labelMedium.copy(fontWeight = FontWeight.SemiBold),
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        if (selectedTab == 0) {
                            Text(
                                text = "Forgot Password?",
                                style = Typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                                color = brandOrange,
                                modifier = Modifier.clickable {
                                    resetEmail = email.trim()
                                    resetSuccessMessage = null
                                    resetErrorMessage = null
                                    showForgotPasswordDialog = true
                                }
                            )
                        }
                    }
                    OutlinedTextField(
                        value = password,
                        onValueChange = { password = it; errorMessage = null },
                        placeholder = {
                            Text(
                                "••••••••",
                                style = Typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.55f)
                            )
                        },
                        leadingIcon = {
                            Icon(Icons.Rounded.Lock, null, modifier = Modifier.size(18.dp), tint = brandOrange)
                        },
                        trailingIcon = {
                            IconButton(onClick = { isPasswordVisible = !isPasswordVisible }) {
                                Icon(
                                    if (isPasswordVisible) Icons.Rounded.VisibilityOff else Icons.Rounded.Visibility,
                                    contentDescription = "Toggle visibility",
                                    modifier = Modifier.size(18.dp),
                                    tint = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        },
                        visualTransformation = if (isPasswordVisible) VisualTransformation.None else PasswordVisualTransformation(),
                        keyboardOptions = KeyboardOptions(
                            keyboardType = KeyboardType.Password,
                            imeAction = androidx.compose.ui.text.input.ImeAction.Done
                        ),
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(12.dp),
                        singleLine = true,
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedContainerColor = MaterialTheme.colorScheme.surface.copy(alpha = 0.7f),
                            unfocusedContainerColor = MaterialTheme.colorScheme.surface.copy(alpha = 0.45f),
                            focusedBorderColor = brandOrange,
                            unfocusedBorderColor = MaterialTheme.colorScheme.outline.copy(alpha = 0.20f),
                            focusedTextColor = MaterialTheme.colorScheme.onSurface,
                            unfocusedTextColor = MaterialTheme.colorScheme.onSurface,
                            cursorColor = brandOrange
                        )
                    )

                    // Error feedback
                    AnimatedVisibility(
                        visible = errorMessage != null,
                        enter = expandVertically() + fadeIn(),
                        exit = shrinkVertically() + fadeOut()
                    ) {
                        errorMessage?.let { err ->
                            Row(
                                modifier = Modifier.padding(top = 10.dp, start = 2.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(6.dp)
                            ) {
                                Icon(Icons.Rounded.ErrorOutline, null, modifier = Modifier.size(14.dp), tint = MaterialTheme.colorScheme.error)
                                Text(err, style = Typography.labelSmall, color = MaterialTheme.colorScheme.error)
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(18.dp))

                    // ── PRIMARY SUBMIT BUTTON ───────────────────────────────
                    Button(
                        onClick = {
                            if (!isNetworkConnected(context)) {
                                showNoInternetDialog = true
                                return@Button
                            }
                            if (email.isBlank() || !email.contains("@")) {
                                errorMessage = "Please enter a valid email address."
                                return@Button
                            }
                            if (password.length < 6) {
                                errorMessage = "Password must be at least 6 characters."
                                return@Button
                            }

                            isLoading = true
                            errorMessage = null

                            if (selectedTab == 0) {
                                loadingMessage = "Signing in…"
                                auth.signInWithEmailAndPassword(email.trim(), password)
                                    .addOnCompleteListener { task ->
                                        isLoading = false
                                        if (task.isSuccessful) {
                                            val user = task.result.user
                                            val displayName = user?.displayName?.ifBlank { null }
                                                ?: email.substringBefore("@")
                                            val userEmail = user?.email ?: email.trim()
                                            successName = displayName
                                            showSuccessModal = true
                                            scope.launch {
                                                delay(800)
                                                showSuccessModal = false
                                                onAuthSuccess(displayName, "Email", userEmail, null)
                                            }
                                        } else {
                                            errorMessage = mapFirebaseError(task.exception)
                                        }
                                    }
                            } else {
                                loadingMessage = "Creating account…"
                                auth.createUserWithEmailAndPassword(email.trim(), password)
                                    .addOnCompleteListener { task ->
                                        if (task.isSuccessful) {
                                            val user = task.result.user
                                            if (name.isNotBlank()) {
                                                val profileUpdates = UserProfileChangeRequest.Builder()
                                                    .setDisplayName(name.trim())
                                                    .build()
                                                user?.updateProfile(profileUpdates)
                                            }
                                            isLoading = false
                                            val displayName = if (name.isNotBlank()) name.trim() else email.substringBefore("@")
                                            val userEmail = user?.email ?: email.trim()
                                            successName = displayName
                                            showSuccessModal = true
                                            scope.launch {
                                                delay(800)
                                                showSuccessModal = false
                                                onAuthSuccess(displayName, "Email", userEmail, null)
                                            }
                                        } else {
                                            isLoading = false
                                            errorMessage = mapFirebaseError(task.exception)
                                        }
                                    }
                            }
                        },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(48.dp)
                            .shadow(8.dp, RoundedCornerShape(12.dp), spotColor = brandOrange.copy(alpha = 0.4f)),
                        enabled = !isLoading,
                        shape = RoundedCornerShape(12.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = brandOrange)
                    ) {
                        Text(
                            text = if (selectedTab == 0) "Sign In" else "Create Account",
                            style = Typography.labelLarge.copy(fontWeight = FontWeight.Bold),
                            color = Color.White
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // ── GUEST ACCESS BUTTON (Clean Rounded Pill) ─────────────────────
            Surface(
                onClick = {
                    isLoading = true
                    loadingMessage = "Entering Guest Mode…"
                    auth.signInAnonymously().addOnCompleteListener {
                        isLoading = false
                        onGuestBypass()
                    }
                },
                shape = RoundedCornerShape(14.dp),
                color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = if (isDark) 0.35f else 0.70f),
                border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.15f)),
                modifier = Modifier
                    .fillMaxWidth()
                    .height(48.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxSize(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.Center
                ) {
                    Icon(
                        Icons.Rounded.Bolt,
                        contentDescription = null,
                        modifier = Modifier.size(18.dp),
                        tint = brandOrange
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "Explore as Guest",
                        style = Typography.labelLarge.copy(fontWeight = FontWeight.Bold),
                        color = MaterialTheme.colorScheme.onSurface
                    )
                }
            }

            Spacer(modifier = Modifier.height(24.dp))
        }

        // ── LOADING DIALOG OVERLAY ───────────────────────────────────────────
        if (isLoading) {
            Dialog(onDismissRequest = {}) {
                GlassCard(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 24.dp),
                    cornerRadius = 22.dp,
                    accentColor = brandOrange
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(26.dp),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.Center
                    ) {
                        CyberLoadingSpinner(size = 48.dp, color = brandOrange, secondaryColor = Color(0xFF4285F4))
                        Spacer(modifier = Modifier.height(16.dp))
                        Text(
                            text = loadingMessage,
                            style = Typography.titleSmall.copy(fontWeight = FontWeight.Bold),
                            color = MaterialTheme.colorScheme.onSurface,
                            textAlign = TextAlign.Center
                        )
                    }
                }
            }
        }

        // ── SUCCESS DIALOG OVERLAY ───────────────────────────────────────────
        if (showSuccessModal) {
            Dialog(onDismissRequest = {}) {
                GlassCard(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 24.dp),
                    cornerRadius = 22.dp,
                    accentColor = Color(0xFF10B981)
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(26.dp),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.Center
                    ) {
                        Box(
                            modifier = Modifier
                                .size(56.dp)
                                .background(Color(0xFF10B981).copy(alpha = 0.18f), CircleShape)
                                .border(1.5.dp, Color(0xFF10B981).copy(alpha = 0.50f), CircleShape),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                Icons.Rounded.CheckCircle,
                                contentDescription = null,
                                modifier = Modifier.size(32.dp),
                                tint = Color(0xFF10B981)
                            )
                        }
                        Spacer(modifier = Modifier.height(14.dp))
                        Text(
                            text = "Welcome, $successName!",
                            style = Typography.titleMedium.copy(fontWeight = FontWeight.Black),
                            color = MaterialTheme.colorScheme.onSurface,
                            textAlign = TextAlign.Center
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = "Connecting your developer calendar & stats…",
                            style = Typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.70f),
                            textAlign = TextAlign.Center
                        )
                    }
                }
            }
        }

        // ── FORGOT PASSWORD DIALOG ───────────────────────────────────────────
        if (showForgotPasswordDialog) {
            Dialog(onDismissRequest = { showForgotPasswordDialog = false }) {
                GlassCard(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp),
                    cornerRadius = 22.dp,
                    accentColor = brandOrange
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(22.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Box(
                            modifier = Modifier
                                .size(48.dp)
                                .background(brandOrange.copy(alpha = 0.15f), CircleShape)
                                .border(1.dp, brandOrange.copy(alpha = 0.40f), CircleShape),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Rounded.Key,
                                contentDescription = null,
                                modifier = Modifier.size(22.dp),
                                tint = brandOrange
                            )
                        }

                        Spacer(modifier = Modifier.height(12.dp))

                        Text(
                            text = "Reset Your Password",
                            style = Typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                            color = MaterialTheme.colorScheme.onSurface,
                            textAlign = TextAlign.Center
                        )

                        Spacer(modifier = Modifier.height(4.dp))

                        Text(
                            text = "Enter your email to receive a secure password recovery link.",
                            style = Typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.70f),
                            textAlign = TextAlign.Center,
                            lineHeight = 16.sp
                        )

                        Spacer(modifier = Modifier.height(16.dp))

                        OutlinedTextField(
                            value = resetEmail,
                            onValueChange = {
                                resetEmail = it
                                resetErrorMessage = null
                                resetSuccessMessage = null
                            },
                            placeholder = {
                                Text(
                                    "developer@domain.com",
                                    style = Typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.55f)
                                )
                            },
                            leadingIcon = {
                                Icon(Icons.Rounded.Email, null, modifier = Modifier.size(18.dp), tint = brandOrange)
                            },
                            keyboardOptions = KeyboardOptions(
                                keyboardType = KeyboardType.Email,
                                imeAction = androidx.compose.ui.text.input.ImeAction.Done
                            ),
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(12.dp),
                            singleLine = true,
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedContainerColor = MaterialTheme.colorScheme.surface.copy(alpha = 0.7f),
                                unfocusedContainerColor = MaterialTheme.colorScheme.surface.copy(alpha = 0.45f),
                                focusedBorderColor = brandOrange,
                                unfocusedBorderColor = MaterialTheme.colorScheme.outline.copy(alpha = 0.20f),
                                focusedTextColor = MaterialTheme.colorScheme.onSurface,
                                unfocusedTextColor = MaterialTheme.colorScheme.onSurface,
                                cursorColor = brandOrange
                            )
                        )

                        // Reset Success feedback
                        AnimatedVisibility(
                            visible = resetSuccessMessage != null,
                            enter = expandVertically() + fadeIn(),
                            exit = shrinkVertically() + fadeOut()
                        ) {
                            resetSuccessMessage?.let { msg ->
                                Row(
                                    modifier = Modifier.padding(top = 10.dp),
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                                ) {
                                    Icon(Icons.Rounded.CheckCircle, null, modifier = Modifier.size(16.dp), tint = Color(0xFF10B981))
                                    Text(msg, style = Typography.labelSmall, color = Color(0xFF10B981))
                                }
                            }
                        }

                        // Reset Error feedback
                        AnimatedVisibility(
                            visible = resetErrorMessage != null,
                            enter = expandVertically() + fadeIn(),
                            exit = shrinkVertically() + fadeOut()
                        ) {
                            resetErrorMessage?.let { err ->
                                Row(
                                    modifier = Modifier.padding(top = 10.dp),
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                                ) {
                                    Icon(Icons.Rounded.ErrorOutline, null, modifier = Modifier.size(16.dp), tint = MaterialTheme.colorScheme.error)
                                    Text(err, style = Typography.labelSmall, color = MaterialTheme.colorScheme.error)
                                }
                            }
                        }

                        Spacer(modifier = Modifier.height(18.dp))

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            OutlinedButton(
                                onClick = { showForgotPasswordDialog = false },
                                modifier = Modifier
                                    .weight(1f)
                                    .height(44.dp),
                                shape = RoundedCornerShape(12.dp)
                            ) {
                                Text(
                                    text = "Close",
                                    style = Typography.labelMedium.copy(fontWeight = FontWeight.SemiBold)
                                )
                            }

                            Button(
                                onClick = {
                                    if (resetEmail.isBlank() || !resetEmail.contains("@")) {
                                        resetErrorMessage = "Please enter a valid email address."
                                        return@Button
                                    }
                                    resetLoading = true
                                    resetErrorMessage = null
                                    resetSuccessMessage = null
                                    auth.sendPasswordResetEmail(resetEmail.trim())
                                        .addOnCompleteListener { task ->
                                            resetLoading = false
                                            if (task.isSuccessful) {
                                                resetSuccessMessage = "Password reset link sent! Check your inbox."
                                            } else {
                                                resetErrorMessage = mapFirebaseError(task.exception)
                                            }
                                        }
                                },
                                enabled = !resetLoading,
                                modifier = Modifier
                                    .weight(1f)
                                    .height(44.dp),
                                shape = RoundedCornerShape(12.dp),
                                colors = ButtonDefaults.buttonColors(containerColor = brandOrange)
                            ) {
                                if (resetLoading) {
                                    CircularProgressIndicator(
                                        modifier = Modifier.size(16.dp),
                                        color = Color.White,
                                        strokeWidth = 2.dp
                                    )
                                } else {
                                    Text(
                                        text = "Send Link",
                                        style = Typography.labelMedium.copy(fontWeight = FontWeight.Bold),
                                        color = Color.White
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }

        // ── NO INTERNET DIALOG ───────────────────────────────────────────────
        if (showNoInternetDialog) {
            Dialog(onDismissRequest = { showNoInternetDialog = false }) {
                GlassCard(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp),
                    cornerRadius = 24.dp,
                    accentColor = Color(0xFFEF4444)
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(24.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Box(
                            modifier = Modifier
                                .size(54.dp)
                                .background(Color(0xFFEF4444).copy(alpha = 0.14f), CircleShape)
                                .border(1.2.dp, Color(0xFFEF4444).copy(alpha = 0.45f), CircleShape),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                Icons.Rounded.WifiOff,
                                contentDescription = null,
                                modifier = Modifier.size(26.dp),
                                tint = Color(0xFFEF4444)
                            )
                        }

                        Spacer(Modifier.height(16.dp))

                        Text(
                            text = "No Internet Connection",
                            style = Typography.titleLarge.copy(fontWeight = FontWeight.Black),
                            color = MaterialTheme.colorScheme.onSurface,
                            textAlign = TextAlign.Center
                        )

                        Spacer(Modifier.height(8.dp))

                        Text(
                            text = "Please check your network connection and try again to continue.",
                            style = Typography.bodySmall.copy(lineHeight = 18.sp),
                            color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.75f),
                            textAlign = TextAlign.Center
                        )

                        Spacer(Modifier.height(22.dp))

                        Button(
                            onClick = { showNoInternetDialog = false },
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(46.dp),
                            shape = RoundedCornerShape(12.dp),
                            colors = ButtonDefaults.buttonColors(
                                containerColor = brandOrange
                            )
                        ) {
                            Text(
                                "Got It",
                                style = Typography.labelMedium.copy(fontWeight = FontWeight.Bold),
                                color = Color.White
                            )
                        }
                    }
                }
            }
        }
    }
}

/**
 * Checks if device is currently connected to active internet.
 */
private fun isNetworkConnected(context: android.content.Context): Boolean {
    val cm = context.getSystemService(android.content.Context.CONNECTIVITY_SERVICE) as? android.net.ConnectivityManager
    val network = cm?.activeNetwork ?: return false
    val caps = cm.getNetworkCapabilities(network) ?: return false
    return caps.hasCapability(android.net.NetworkCapabilities.NET_CAPABILITY_INTERNET)
}

/**
 * Maps raw exceptions into clean, user-friendly error messages.
 */
private fun mapFirebaseError(exception: Exception?): String {
    return when (exception) {
        is FirebaseAuthInvalidUserException -> "No user found with this email. Please check or create an account."
        is FirebaseAuthInvalidCredentialsException -> "Incorrect email or password. Please verify and try again."
        is FirebaseAuthUserCollisionException -> "An account with this email already exists. Please switch to Sign In."
        is FirebaseAuthWeakPasswordException -> "Password is too weak. Please use at least 6 characters."
        is FirebaseNetworkException -> "Network error. Please check your internet connection and try again."
        else -> exception?.localizedMessage ?: "Authentication failed. Please try again."
    }
}



