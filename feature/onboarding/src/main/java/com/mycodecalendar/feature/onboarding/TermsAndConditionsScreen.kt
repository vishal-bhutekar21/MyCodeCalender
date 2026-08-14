package com.mycodecalendar.feature.onboarding

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.OpenInNew
import androidx.compose.material.icons.rounded.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.mycodecalendar.core.designsystem.BrandPrimaryOrange
import com.mycodecalendar.core.designsystem.GlassmorphismBackground
import com.mycodecalendar.core.designsystem.Typography
import com.mycodecalendar.core.designsystem.components.GlassCard

/**
 * TermsAndConditionsScreen — Clean, minimal Terms & Privacy Policy screen with proper spacing.
 */
@Composable
fun TermsAndConditionsScreen(
    onAgreeClick: () -> Unit,
    onOpenUrl: (String) -> Unit
) {
    var isAgreed by remember { mutableStateOf(false) }
    val privacyUrl = "https://vishalbhutekar.netlify.app/myapps/codecalendar/privacy"
    val brandOrange = BrandPrimaryOrange

    GlassmorphismBackground {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .statusBarsPadding()
                .navigationBarsPadding()
                .padding(horizontal = 24.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Spacer(modifier = Modifier.height(28.dp))

            // ── ICON & MINIMAL HEADER ────────────────────────────────────────
            Box(
                modifier = Modifier
                    .size(56.dp)
                    .background(
                        Brush.linearGradient(listOf(Color(0xFF1E2235), Color(0xFF121624))),
                        RoundedCornerShape(16.dp)
                    )
                    .border(1.dp, brandOrange.copy(alpha = 0.5f), RoundedCornerShape(16.dp)),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Rounded.Security,
                    contentDescription = null,
                    modifier = Modifier.size(26.dp),
                    tint = brandOrange
                )
            }

            Spacer(modifier = Modifier.height(18.dp))

            Text(
                text = "Terms & Privacy",
                style = Typography.headlineSmall.copy(fontWeight = FontWeight.Bold),
                color = MaterialTheme.colorScheme.onBackground
            )

            Spacer(modifier = Modifier.height(6.dp))

            Text(
                text = "Quick summary of how your data is handled",
                style = Typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.65f),
                textAlign = TextAlign.Center
            )

            Spacer(modifier = Modifier.height(28.dp))

            // ── MINIMAL CONTENT CARD WITH PROPER SPACING ─────────────────────
            GlassCard(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth(),
                cornerRadius = 20.dp,
                accentColor = brandOrange
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .verticalScroll(rememberScrollState())
                        .padding(horizontal = 20.dp, vertical = 22.dp),
                    verticalArrangement = Arrangement.spacedBy(20.dp)
                ) {
                    TermsMinimalRow(
                        icon = Icons.Rounded.Lock,
                        title = "Data Protection",
                        description = "No ad trackers or selling of personal data. Your information remains private."
                    )

                    TermsMinimalRow(
                        icon = Icons.Rounded.Code,
                        title = "Platform Sync",
                        description = "Public handle stats (LeetCode, Codeforces, etc.) are fetched in read-only mode."
                    )

                    TermsMinimalRow(
                        icon = Icons.Rounded.EventAvailable,
                        title = "Contest Reminders",
                        description = "Calendar permissions are strictly used when exporting contest alerts."
                    )

                    TermsMinimalRow(
                        icon = Icons.Rounded.Copyright,
                        title = "Educational Fair Use & Copyright",
                        description = "All platform logos, contest schedules, and problem sets belong to their respective copyright owners. Provided strictly for non-commercial educational learning under fair use. For content removal inquiries: vishal.bhutekar1@gmail.com"
                    )

                    Spacer(modifier = Modifier.weight(1f, fill = false))

                    // ── UNHIGHLIGHTED MINIMAL LINK ───────────────────────────
                    Surface(
                        onClick = { onOpenUrl(privacyUrl) },
                        shape = RoundedCornerShape(12.dp),
                        color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.30f),
                        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.12f)),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 14.dp, vertical = 12.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text(
                                text = "Full Terms & Privacy Policy",
                                style = Typography.bodySmall.copy(fontWeight = FontWeight.Medium),
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )

                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(4.dp)
                            ) {
                                Text(
                                    text = "Click to open",
                                    style = Typography.labelSmall.copy(fontWeight = FontWeight.SemiBold),
                                    color = brandOrange
                                )
                                Icon(
                                    imageVector = Icons.AutoMirrored.Rounded.OpenInNew,
                                    contentDescription = "Open Link",
                                    modifier = Modifier.size(13.dp),
                                    tint = brandOrange
                                )
                            }
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(20.dp))

            // ── AGREEMENT CHECKBOX ───────────────────────────────────────────
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable { isAgreed = !isAgreed }
                    .padding(vertical = 2.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Checkbox(
                    checked = isAgreed,
                    onCheckedChange = { isAgreed = it },
                    colors = CheckboxDefaults.colors(
                        checkedColor = brandOrange,
                        uncheckedColor = MaterialTheme.colorScheme.outline.copy(alpha = 0.5f)
                    )
                )
                Spacer(modifier = Modifier.width(6.dp))
                Text(
                    text = "I agree to the Terms of Service & Privacy Policy",
                    style = Typography.bodySmall.copy(fontWeight = FontWeight.Medium),
                    color = MaterialTheme.colorScheme.onSurface
                )
            }

            Spacer(modifier = Modifier.height(14.dp))

            // ── AGREE & CONTINUE BUTTON ──────────────────────────────────────
            Button(
                onClick = onAgreeClick,
                enabled = isAgreed,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(50.dp)
                    .shadow(
                        elevation = if (isAgreed) 8.dp else 0.dp,
                        shape = RoundedCornerShape(14.dp),
                        spotColor = brandOrange.copy(alpha = 0.4f)
                    ),
                shape = RoundedCornerShape(14.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = brandOrange,
                    disabledContainerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f)
                )
            ) {
                Text(
                    text = "Agree & Continue",
                    style = Typography.labelLarge.copy(fontWeight = FontWeight.Bold),
                    color = if (isAgreed) Color.White else MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.35f)
                )
            }

            Spacer(modifier = Modifier.height(24.dp))
        }
    }
}

@Composable
private fun TermsMinimalRow(
    icon: ImageVector,
    title: String,
    description: String
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(14.dp),
        verticalAlignment = Alignment.Top
    ) {
        Box(
            modifier = Modifier
                .size(34.dp)
                .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.45f), CircleShape)
                .border(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.15f), CircleShape),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                modifier = Modifier.size(16.dp),
                tint = BrandPrimaryOrange
            )
        }

        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = title,
                style = Typography.titleSmall.copy(fontWeight = FontWeight.SemiBold),
                color = MaterialTheme.colorScheme.onSurface
            )
            Spacer(modifier = Modifier.height(3.dp))
            Text(
                text = description,
                style = Typography.bodySmall.copy(lineHeight = 16.sp),
                color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.70f)
            )
        }
    }
}
