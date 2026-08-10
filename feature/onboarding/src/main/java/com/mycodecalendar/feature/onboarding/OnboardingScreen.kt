package com.mycodecalendar.feature.onboarding

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.mycodecalendar.core.designsystem.Typography

@Composable
fun OnboardingScreen(onComplete: () -> Unit) {
    var step by remember { mutableStateOf(0) }

    val titles = listOf(
        "Never Miss a Coding Contest",
        "Track Ratings Across Platforms",
        "Sync Directly to Android Calendar"
    )

    val descriptions = listOf(
        "Real-time countdowns and live contest aggregation from Codeforces, LeetCode, CodeChef, AtCoder, and GeeksforGeeks.",
        "Connect your handles to see rating graphs, global rankings, and problem-solving stats all in one dashboard.",
        "Set custom reminders and add upcoming contests to your device calendar with zero duplicate events."
    )

    val icons = listOf("🏆", "📊", "📅")

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.SpaceBetween
    ) {
        // Skip Button
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.End
        ) {
            TextButton(onClick = onComplete) {
                Text("Skip", style = Typography.labelLarge)
            }
        }

        // Card Content
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier.padding(horizontal = 16.dp)
        ) {
            Surface(
                shape = CircleShape,
                color = MaterialTheme.colorScheme.primaryContainer,
                modifier = Modifier.size(100.dp)
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Text(text = icons[step], fontSize = 48.sp)
                }
            }

            Spacer(modifier = Modifier.height(32.dp))

            Text(
                text = titles[step],
                style = Typography.headlineMedium.copy(fontWeight = FontWeight.Bold),
                textAlign = TextAlign.Center,
                color = MaterialTheme.colorScheme.onBackground
            )

            Spacer(modifier = Modifier.height(16.dp))

            Text(
                text = descriptions[step],
                style = Typography.bodyLarge,
                textAlign = TextAlign.Center,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }

        // Indicators & Navigation Buttons
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier.fillMaxWidth()
        ) {
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                for (i in 0..2) {
                    Box(
                        modifier = Modifier
                            .size(width = if (i == step) 24.dp else 8.dp, height = 8.dp)
                            .background(
                                color = if (i == step) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.outlineVariant,
                                shape = CircleShape
                            )
                    )
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            Button(
                onClick = {
                    if (step < 2) {
                        step++
                    } else {
                        onComplete()
                    }
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(50.dp),
                shape = RoundedCornerShape(14.dp)
            ) {
                Text(
                    text = if (step == 2) "Get Started" else "Continue",
                    style = Typography.labelLarge.copy(fontWeight = FontWeight.Bold)
                )
            }

            Spacer(modifier = Modifier.height(16.dp))
        }
    }
}
