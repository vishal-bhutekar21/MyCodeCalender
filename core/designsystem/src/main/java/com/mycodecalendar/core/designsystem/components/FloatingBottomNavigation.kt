package com.mycodecalendar.core.designsystem.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.MenuBook
import androidx.compose.material.icons.rounded.EmojiEvents
import androidx.compose.material.icons.rounded.Home
import androidx.compose.material.icons.rounded.Settings
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.luminance
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.mycodecalendar.core.designsystem.BrandPrimaryOrange
import com.mycodecalendar.core.designsystem.Typography

enum class NavTab(val route: String, val label: String, val icon: ImageVector) {
    HOME("home", "Home", Icons.Rounded.Home),
    CONTESTS("contests", "Contests", Icons.Rounded.EmojiEvents),
    RESOURCES("resources", "Resources", Icons.AutoMirrored.Rounded.MenuBook),
    SETTINGS("settings", "Settings", Icons.Rounded.Settings)
}

/**
 * FloatingBottomNavigation — Ultra-Minimalist Modern Floating Glass Dock.
 *
 * Design Features:
 * - Deep Obsidian Frosted Glass in Dark Mode (#0D111A) with soft 1px border (#1E2536).
 * - Clean Pure White Frosted Glass in Light Mode.
 * - Reactive luminance-based color adapting.
 * - Ergonomic 60.dp floating pill container with ambient bottom margin.
 */
@Composable
fun FloatingBottomNavigation(
    currentRoute: String,
    onTabSelected: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    val isDark = MaterialTheme.colorScheme.background.luminance() < 0.5f

    val baseBg    = if (isDark) Color(0xFF0D111A) else Color(0xFFFFFFFF)
    val glassFill = if (isDark) Color(0xFF151B28) else Color(0xFFF8FAFC)

    Box(
        modifier = modifier
            .fillMaxWidth()
            .navigationBarsPadding()
            .padding(horizontal = 18.dp, vertical = 14.dp),
        contentAlignment = Alignment.Center
    ) {
        // Floating pill glass dock (72dp height)
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(72.dp)
                .shadow(
                    elevation = 28.dp,
                    shape = CircleShape,
                    spotColor = if (isDark) BrandPrimaryOrange.copy(alpha = 0.18f) else Color(0x14000000),
                    ambientColor = Color.Black.copy(alpha = if (isDark) 0.38f else 0.06f)
                )
                .clip(CircleShape)
                .background(baseBg.copy(alpha = if (isDark) 0.90f else 0.94f))
                .background(
                    brush = Brush.verticalGradient(
                        colors = listOf(
                            glassFill.copy(alpha = if (isDark) 0.38f else 0.72f),
                            glassFill.copy(alpha = if (isDark) 0.14f else 0.40f)
                        )
                    )
                )
        ) {
            Row(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(horizontal = 10.dp),
                horizontalArrangement = Arrangement.SpaceEvenly,
                verticalAlignment = Alignment.CenterVertically
            ) {
                NavTab.values().forEach { tab ->
                    val isSelected = currentRoute == tab.route
                    NavTabItem(
                        tab = tab,
                        isSelected = isSelected,
                        isDark = isDark,
                        onClick = { onTabSelected(tab.route) }
                    )
                }
            }
        }
    }
}

@Composable
private fun NavTabItem(
    tab: NavTab,
    isSelected: Boolean,
    isDark: Boolean,
    onClick: () -> Unit
) {
    val itemColor by animateColorAsState(
        targetValue = if (isSelected) BrandPrimaryOrange
        else if (isDark) Color(0xFF64748B) else Color(0xFF94A3B8),
        animationSpec = spring(stiffness = Spring.StiffnessMedium),
        label = "navColor_${tab.name}"
    )

    val iconScale by animateFloatAsState(
        targetValue = if (isSelected) 1.12f else 1.0f,
        animationSpec = spring(
            dampingRatio = Spring.DampingRatioMediumBouncy,
            stiffness = Spring.StiffnessMedium
        ),
        label = "iconScale_${tab.name}"
    )

    val horizontalPadding by animateDpAsState(
        targetValue = if (isSelected) 18.dp else 12.dp,
        animationSpec = spring(stiffness = Spring.StiffnessMediumLow),
        label = "hPad_${tab.name}"
    )

    Column(
        modifier = Modifier
            .clip(RoundedCornerShape(16.dp))
            .clickable(
                interactionSource = remember { MutableInteractionSource() },
                indication = null,
                onClick = onClick
            )
            .padding(horizontal = horizontalPadding, vertical = 6.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(4.dp)
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.Center
        ) {
            Icon(
                imageVector = tab.icon,
                contentDescription = tab.label,
                tint = itemColor,
                modifier = Modifier
                    .size(20.dp)
                    .scale(iconScale)
            )

            AnimatedVisibility(
                visible = isSelected,
                enter = fadeIn(spring(stiffness = Spring.StiffnessMedium)),
                exit = fadeOut(spring(stiffness = Spring.StiffnessMedium))
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = tab.label,
                        color = itemColor,
                        fontSize = 12.5.sp,
                        fontWeight = FontWeight.Bold,
                        style = Typography.labelMedium.copy(fontWeight = FontWeight.Bold)
                    )
                }
            }
        }

        // Active dot indicator — a clean minimal dot below the icon/label
        Box(
            modifier = Modifier
                .size(width = if (isSelected) 16.dp else 4.dp, height = 3.dp)
                .clip(CircleShape)
                .background(
                    if (isSelected) BrandPrimaryOrange
                    else Color.Transparent
                )
        )
    }
}
