package com.mycodecalendar.core.designsystem.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Lock
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import com.mycodecalendar.core.designsystem.BrandPrimaryOrange
import com.mycodecalendar.core.designsystem.Typography

/**
 * AuthRequiredModal — Minimalist, modern, cyber frosted glass prompt
 * when a guest tries to connect platforms.
 */
@Composable
fun AuthRequiredModal(
    onDismiss: () -> Unit,
    onSignInClick: () -> Unit,
    title: String = "Sign In Required",
    description: String = "Sign in to connect and sync your coding platforms."
) {
    Dialog(onDismissRequest = onDismiss) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 20.dp)
                .clip(RoundedCornerShape(22.dp))
                .border(0.3.dp, BrandPrimaryOrange.copy(alpha = 0.40f), RoundedCornerShape(22.dp))
        ) {
            GlassCard(
                modifier = Modifier.fillMaxWidth(),
                cornerRadius = 22.dp,
                accentColor = BrandPrimaryOrange
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(20.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Box(
                        modifier = Modifier
                            .size(44.dp)
                            .background(BrandPrimaryOrange.copy(alpha = 0.14f), CircleShape)
                            .border(0.3.dp, BrandPrimaryOrange.copy(alpha = 0.45f), CircleShape),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Rounded.Lock,
                            contentDescription = null,
                            modifier = Modifier.size(20.dp),
                            tint = BrandPrimaryOrange
                        )
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    Text(
                        text = title,
                        style = Typography.titleMedium.copy(fontWeight = FontWeight.Bold, fontSize = 17.sp),
                        color = MaterialTheme.colorScheme.onSurface,
                        textAlign = TextAlign.Center
                    )

                    Spacer(modifier = Modifier.height(6.dp))

                    Text(
                        text = description,
                        style = Typography.bodySmall.copy(fontSize = 12.sp, lineHeight = 17.sp),
                        color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.70f),
                        textAlign = TextAlign.Center
                    )

                    Spacer(modifier = Modifier.height(18.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(10.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        OutlinedButton(
                            onClick = onDismiss,
                            modifier = Modifier
                                .weight(1f)
                                .height(42.dp),
                            shape = RoundedCornerShape(12.dp),
                            border = BorderStroke(0.3.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.30f)),
                            colors = ButtonDefaults.outlinedButtonColors(
                                contentColor = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        ) {
                            Text(
                                text = "Cancel",
                                style = Typography.labelMedium.copy(fontWeight = FontWeight.SemiBold, fontSize = 12.5.sp)
                            )
                        }

                        Button(
                            onClick = {
                                onDismiss()
                                onSignInClick()
                            },
                            modifier = Modifier
                                .weight(1.2f)
                                .height(42.dp)
                                .shadow(6.dp, RoundedCornerShape(12.dp), spotColor = BrandPrimaryOrange.copy(alpha = 0.45f)),
                            shape = RoundedCornerShape(12.dp),
                            colors = ButtonDefaults.buttonColors(containerColor = BrandPrimaryOrange)
                        ) {
                            Text(
                                text = "Sign In",
                                style = Typography.labelMedium.copy(fontWeight = FontWeight.Bold, fontSize = 12.5.sp),
                                color = Color.White
                            )
                        }
                    }
                }
            }
        }
    }
}
