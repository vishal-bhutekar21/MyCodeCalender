package com.mycodecalendar.feature.onboarding

import androidx.compose.animation.*
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
import androidx.compose.material.icons.automirrored.rounded.ArrowForward
import androidx.compose.material.icons.rounded.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.mycodecalendar.core.designsystem.GlassmorphismBackground
import com.mycodecalendar.core.designsystem.Typography
import com.mycodecalendar.core.designsystem.components.GlassCard
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

/**
 * Modern, Majestic Minimal Login & Sign-Up Screen.
 *
 * Visual Styling:
 * - Brand MyCodeCalendar styling with Vivid Orange (#FF6B00) highlights.
 * - Supports GitHub Token / Handle Authentication, Email & Password, and Guest Instant Access.
 */
@Composable
fun AuthScreen(
    onAuthSuccess: (username: String, method: String) -> Unit,
    onGuestBypass: () -> Unit
) {
    val scope = rememberCoroutineScope()

    var selectedTab by remember { mutableStateOf(0) } // 0 = Sign In, 1 = Create Account

    // Form inputs
    var name by remember { mutableStateOf("") }
    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var githubHandleOrToken by remember { mutableStateOf("") }
    var isPasswordVisible by remember { mutableStateOf(false) }
    var showGithubTokenInput by remember { mutableStateOf(false) }

    var isLoading by remember { mutableStateOf(false) }
    var errorMessage by remember { mutableStateOf<String?>(null) }

    val brandOrange = Color(0xFFFF6B00)
    val brandOrangeGrad = listOf(Color(0xFFFF7A00), Color(0xFFFF5200))
    val brandPurple = Color(0xFF6C5CE7)
    val brandGreen = Color(0xFF10B981)

    GlassmorphismBackground {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .statusBarsPadding()
                .navigationBarsPadding()
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 20.dp, vertical = 20.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Spacer(modifier = Modifier.height(12.dp))

            // ── APP LOGO & HEADER ───────────────────────────────────────────
            Box(
                modifier = Modifier
                    .size(72.dp)
                    .background(
                        Brush.linearGradient(listOf(Color(0xFF1E2235), Color(0xFF121624))),
                        RoundedCornerShape(20.dp)
                    )
                    .border(1.5.dp, brandOrange.copy(alpha = 0.6f), RoundedCornerShape(20.dp))
                    .shadow(16.dp, RoundedCornerShape(20.dp), spotColor = brandOrange.copy(alpha = 0.4f)),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    Icons.Rounded.Code,
                    contentDescription = null,
                    modifier = Modifier.size(36.dp),
                    tint = brandOrange
                )
            }

            Spacer(modifier = Modifier.height(16.dp))

            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(
                    text = "MyCode",
                    style = Typography.displaySmall.copy(fontWeight = FontWeight.Black),
                    color = MaterialTheme.colorScheme.onBackground
                )
                Text(
                    text = "Calendar",
                    style = Typography.displaySmall.copy(fontWeight = FontWeight.Black),
                    color = brandOrange
                )
            }

            Spacer(modifier = Modifier.height(4.dp))

            Text(
                text = "Plan. Code. Achieve.",
                style = Typography.labelMedium.copy(
                    fontWeight = FontWeight.Bold,
                    letterSpacing = 1.2.sp
                ),
                color = brandOrange
            )

            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = if (selectedTab == 0) "Welcome back! Sign in to sync your ratings & calendar."
                else "Create an account to track contests & manage developer handles.",
                style = Typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                textAlign = TextAlign.Center,
                modifier = Modifier.padding(horizontal = 16.dp)
            )

            Spacer(modifier = Modifier.height(24.dp))

            // ── TAB SELECTOR (Sign In vs Create Account) ────────────────────
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(48.dp)
                    .clip(RoundedCornerShape(14.dp))
                    .background(MaterialTheme.colorScheme.surface.copy(alpha = 0.6f))
                    .border(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.2f), RoundedCornerShape(14.dp))
                    .padding(4.dp)
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
                        style = Typography.labelLarge.copy(fontWeight = FontWeight.Bold),
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
                        style = Typography.labelLarge.copy(fontWeight = FontWeight.Bold),
                        color = if (selectedTab == 1) Color.White else MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }

            Spacer(modifier = Modifier.height(20.dp))

            // ── AUTH CARD CONTAINER ─────────────────────────────────────────
            GlassCard(
                modifier = Modifier.fillMaxWidth(),
                cornerRadius = 24.dp,
                accentColor = brandOrange
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(20.dp)
                ) {
                    // ── GITHUB FAST AUTH BUTTON ─────────────────────────────
                    Button(
                        onClick = {
                            if (showGithubTokenInput) {
                                if (githubHandleOrToken.isBlank()) {
                                    errorMessage = "Please enter your GitHub handle or token"
                                    return@Button
                                }
                                scope.launch {
                                    isLoading = true
                                    errorMessage = null
                                    delay(600)
                                    val user = githubHandleOrToken.trim().removePrefix("@")
                                    isLoading = false
                                    onAuthSuccess(user, "GitHub")
                                }
                            } else {
                                showGithubTokenInput = true
                            }
                        },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(50.dp),
                        shape = RoundedCornerShape(14.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = Color(0xFF24292F)
                        ),
                        border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFF444C56))
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.Center
                        ) {
                            Icon(
                                Icons.Rounded.Code,
                                contentDescription = null,
                                modifier = Modifier.size(20.dp),
                                tint = Color.White
                            )
                            Spacer(modifier = Modifier.width(10.dp))
                            Text(
                                text = if (showGithubTokenInput) "Verify & Connect GitHub"
                                else "Continue with GitHub",
                                style = Typography.labelLarge.copy(fontWeight = FontWeight.Bold),
                                color = Color.White
                            )
                        }
                    }

                    // GitHub Handle / Token Input Field (revealed on click)
                    AnimatedVisibility(
                        visible = showGithubTokenInput,
                        enter = expandVertically() + fadeIn(),
                        exit = shrinkVertically() + fadeOut()
                    ) {
                        Column(modifier = Modifier.padding(top = 12.dp)) {
                            OutlinedTextField(
                                value = githubHandleOrToken,
                                onValueChange = {
                                    githubHandleOrToken = it
                                    errorMessage = null
                                },
                                placeholder = {
                                    Text(
                                        "Enter GitHub handle (e.g. torvalds)",
                                        style = Typography.bodyMedium,
                                        color = MaterialTheme.colorScheme.outline.copy(alpha = 0.5f)
                                    )
                                },
                                leadingIcon = {
                                    Icon(
                                        Icons.Rounded.AlternateEmail,
                                        contentDescription = null,
                                        modifier = Modifier.size(18.dp),
                                        tint = brandGreen
                                    )
                                },
                                modifier = Modifier.fillMaxWidth(),
                                shape = RoundedCornerShape(12.dp),
                                singleLine = true,
                                colors = OutlinedTextFieldDefaults.colors(
                                    focusedContainerColor = MaterialTheme.colorScheme.surface,
                                    unfocusedContainerColor = MaterialTheme.colorScheme.surface,
                                    focusedBorderColor = brandGreen,
                                    unfocusedBorderColor = MaterialTheme.colorScheme.outline.copy(alpha = 0.25f)
                                )
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(18.dp))

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
                            text = "OR USE EMAIL",
                            modifier = Modifier.padding(horizontal = 10.dp),
                            style = Typography.labelSmall.copy(fontSize = 10.sp, fontWeight = FontWeight.Bold),
                            color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f)
                        )
                        HorizontalDivider(
                            modifier = Modifier.weight(1f),
                            color = MaterialTheme.colorScheme.outline.copy(alpha = 0.15f)
                        )
                    }

                    Spacer(modifier = Modifier.height(18.dp))

                    // ── NAME INPUT (Sign Up only) ───────────────────────────
                    AnimatedVisibility(
                        visible = selectedTab == 1,
                        enter = expandVertically() + fadeIn(),
                        exit = shrinkVertically() + fadeOut()
                    ) {
                        Column(modifier = Modifier.padding(bottom = 12.dp)) {
                            Text(
                                text = "Your Name",
                                style = Typography.labelMedium.copy(fontWeight = FontWeight.SemiBold),
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                modifier = Modifier.padding(bottom = 6.dp, start = 2.dp)
                            )
                            OutlinedTextField(
                                value = name,
                                onValueChange = { name = it; errorMessage = null },
                                placeholder = {
                                    Text("e.g. Alex Turing", color = MaterialTheme.colorScheme.outline.copy(alpha = 0.4f))
                                },
                                leadingIcon = {
                                    Icon(Icons.Rounded.Person, null, modifier = Modifier.size(18.dp), tint = brandOrange)
                                },
                                modifier = Modifier.fillMaxWidth(),
                                shape = RoundedCornerShape(12.dp),
                                singleLine = true
                            )
                        }
                    }

                    // ── EMAIL INPUT ─────────────────────────────────────────
                    Text(
                        text = "Email Address",
                        style = Typography.labelMedium.copy(fontWeight = FontWeight.SemiBold),
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.padding(bottom = 6.dp, start = 2.dp)
                    )
                    OutlinedTextField(
                        value = email,
                        onValueChange = { email = it; errorMessage = null },
                        placeholder = {
                            Text("developer@domain.com", color = MaterialTheme.colorScheme.outline.copy(alpha = 0.4f))
                        },
                        leadingIcon = {
                            Icon(Icons.Rounded.Email, null, modifier = Modifier.size(18.dp), tint = brandOrange)
                        },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email),
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(12.dp),
                        singleLine = true
                    )

                    Spacer(modifier = Modifier.height(14.dp))

                    // ── PASSWORD INPUT ──────────────────────────────────────
                    Text(
                        text = "Password",
                        style = Typography.labelMedium.copy(fontWeight = FontWeight.SemiBold),
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.padding(bottom = 6.dp, start = 2.dp)
                    )
                    OutlinedTextField(
                        value = password,
                        onValueChange = { password = it; errorMessage = null },
                        placeholder = {
                            Text("••••••••", color = MaterialTheme.colorScheme.outline.copy(alpha = 0.4f))
                        },
                        leadingIcon = {
                            Icon(Icons.Rounded.Lock, null, modifier = Modifier.size(18.dp), tint = brandOrange)
                        },
                        trailingIcon = {
                            IconButton(onClick = { isPasswordVisible = !isPasswordVisible }) {
                                Icon(
                                    if (isPasswordVisible) Icons.Rounded.VisibilityOff else Icons.Rounded.Visibility,
                                    contentDescription = "Toggle password visibility",
                                    modifier = Modifier.size(18.dp),
                                    tint = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        },
                        visualTransformation = if (isPasswordVisible) VisualTransformation.None else PasswordVisualTransformation(),
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(12.dp),
                        singleLine = true
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

                    Spacer(modifier = Modifier.height(20.dp))

                    // ── PRIMARY SUBMIT BUTTON ───────────────────────────────
                    Button(
                        onClick = {
                            if (email.isBlank() || !email.contains("@")) {
                                errorMessage = "Please enter a valid email address."
                                return@Button
                            }
                            if (password.length < 4) {
                                errorMessage = "Password must be at least 4 characters."
                                return@Button
                            }

                            scope.launch {
                                isLoading = true
                                errorMessage = null
                                delay(600)
                                isLoading = false
                                val user = if (selectedTab == 1 && name.isNotBlank()) name else email.substringBefore("@")
                                onAuthSuccess(user, "Email")
                            }
                        },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(52.dp)
                            .shadow(12.dp, RoundedCornerShape(14.dp), spotColor = brandOrange.copy(alpha = 0.5f)),
                        enabled = !isLoading,
                        shape = RoundedCornerShape(14.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = brandOrange)
                    ) {
                        if (isLoading) {
                            CircularProgressIndicator(modifier = Modifier.size(18.dp), color = Color.White, strokeWidth = 2.dp)
                            Spacer(modifier = Modifier.width(10.dp))
                            Text("Authenticating…", style = Typography.labelLarge.copy(fontWeight = FontWeight.Bold), color = Color.White)
                        } else {
                            Text(
                                text = if (selectedTab == 0) "Sign In →" else "Create Developer Account →",
                                style = Typography.labelLarge.copy(fontWeight = FontWeight.Black),
                                color = Color.White
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(20.dp))

            // ── GUEST / INSTANT ACCESS BYPASS ───────────────────────────────
            GlassCard(
                modifier = Modifier.fillMaxWidth(),
                cornerRadius = 18.dp,
                accentColor = brandOrange,
                onClick = onGuestBypass
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 14.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        Box(
                            modifier = Modifier
                                .size(36.dp)
                                .background(brandOrange.copy(alpha = 0.15f), CircleShape),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                Icons.Rounded.Bolt,
                                contentDescription = null,
                                modifier = Modifier.size(20.dp),
                                tint = brandOrange
                            )
                        }

                        Column {
                            Text(
                                text = "Instant Access as Guest",
                                style = Typography.titleSmall.copy(fontWeight = FontWeight.Bold),
                                color = MaterialTheme.colorScheme.onSurface
                            )
                            Text(
                                text = "Explore contests & features without signing in",
                                style = Typography.labelSmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f)
                            )
                        }
                    }

                    Icon(
                        Icons.AutoMirrored.Rounded.ArrowForward,
                        contentDescription = "Bypass",
                        modifier = Modifier.size(18.dp),
                        tint = brandOrange
                    )
                }
            }

            Spacer(modifier = Modifier.height(32.dp))
        }
    }
}
