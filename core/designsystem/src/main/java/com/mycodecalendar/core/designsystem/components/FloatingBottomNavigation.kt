package com.mycodecalendar.core.designsystem.components

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.isSystemInDarkTheme
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
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.draw.scale
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.mycodecalendar.core.designsystem.GlassBorderDark
import com.mycodecalendar.core.designsystem.GlassBorderLight
import com.mycodecalendar.core.designsystem.GlassSurfaceDark
import com.mycodecalendar.core.designsystem.GlassSurfaceLight
import com.mycodecalendar.core.designsystem.Typography

enum class NavTab(val route: String, val label: String, val icon: ImageVector) {
    HOME("home", "Home", Icons.Rounded.Home),
    CONTESTS("contests", "Contests", Icons.Rounded.EmojiEvents),
    RESOURCES("resources", "Resources", Icons.AutoMirrored.Rounded.MenuBook),
    SETTINGS("settings", "Settings", Icons.Rounded.Settings)
}

/**
 * FloatingBottomNavigation — Modern, accessible, minimalist floating bottom navigation.
 *
 * Ergonomic Improvements:
 * - Shifted up higher (bottom margin: 24.dp) for comfortable one-handed thumb reach.
 * - Increased height & tap target bounds (60.dp inner container height).
 * - Ultra-clean glass pill design with soft drop shadow and crisp selected indicators.
 */
@Composable
fun FloatingBottomNavigation(
    currentRoute: String,
    onTabSelected: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    val isDark = isSystemInDarkTheme()

    val glassFill   = if (isDark) GlassSurfaceDark  else GlassSurfaceLight
    val glassBorder = if (isDark) GlassBorderDark   else GlassBorderLight
    val baseBg      = if (isDark) Color(0xFF0F172A)  else Color(0xFFFFFFFF)

    val borderBrush = Brush.linearGradient(
        colors = listOf(
            if (isDark) Color(0x35FFFFFF) else Color(0xE6FFFFFF),
            glassBorder,
            if (isDark) Color(0x10FFFFFF) else Color(0x40FFFFFF)
        )
    )

    Box(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 24.dp, vertical = 20.dp),
        contentAlignment = Alignment.Center
    ) {
        // Floating pill shadow and surface container
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(64.dp)
                .shadow(
                    elevation = 24.dp,
                    shape = CircleShape,
                    spotColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.18f),
                    ambientColor = Color.Black.copy(alpha = 0.12f)
                )
                .clip(CircleShape)
                .background(baseBg.copy(alpha = if (isDark) 0.88f else 0.92f))
                .background(
                    brush = Brush.verticalGradient(
                        colors = listOf(
                            glassFill.copy(alpha = if (isDark) 0.30f else 0.70f),
                            glassFill.copy(alpha = if (isDark) 0.15f else 0.50f)
                        )
                    )
                )
                .border(
                    width = 1.dp,
                    brush = borderBrush,
                    shape = CircleShape
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
        targetValue = if (isSelected) MaterialTheme.colorScheme.primary
        else MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.50f),
        animationSpec = spring(stiffness = Spring.StiffnessMedium),
        label = "navColor_${tab.name}"
    )

    val pillBg by animateColorAsState(
        targetValue = if (isSelected)
            MaterialTheme.colorScheme.primary.copy(alpha = if (isDark) 0.20f else 0.12f)
        else Color.Transparent,
        animationSpec = spring(stiffness = Spring.StiffnessMedium),
        label = "navBg_${tab.name}"
    )

    val pillBorder by animateColorAsState(
        targetValue = if (isSelected)
            MaterialTheme.colorScheme.primary.copy(alpha = if (isDark) 0.40f else 0.28f)
        else Color.Transparent,
        animationSpec = spring(stiffness = Spring.StiffnessMedium),
        label = "navBorder_${tab.name}"
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

    Box(
        modifier = Modifier
            .clip(CircleShape)
            .background(pillBg)
            .border(1.dp, pillBorder, CircleShape)
            .clickable(
                interactionSource = remember { MutableInteractionSource() },
                indication = null,
                onClick = onClick
            )
            .padding(horizontal = horizontalPadding, vertical = 10.dp),
        contentAlignment = Alignment.Center
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
                    .size(22.dp)
                    .scale(iconScale)
            )

            if (isSelected) {
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = tab.label,
                    color = itemColor,
                    fontSize = 13.sp,
                    fontWeight = FontWeight.Bold,
                    style = Typography.labelMedium.copy(fontWeight = FontWeight.Bold)
                )
            }
        }
    }
}
