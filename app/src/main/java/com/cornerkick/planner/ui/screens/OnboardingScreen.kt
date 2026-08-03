package com.cornerkick.planner.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.cornerkick.planner.ui.AppStrings
import com.cornerkick.planner.ui.components.DisclaimerCard
import com.cornerkick.planner.ui.theme.DarkSectionBackground
import com.cornerkick.planner.ui.theme.StrongOrange
import com.cornerkick.planner.ui.theme.WhiteText

@Composable
fun OnboardingScreen(onFinish: () -> Unit) {
    val bullets = listOf(
        "Plan your corner-kick routines." to "Build and save set-piece ideas for every match.",
        "Create near post, far post, and short corner schemes." to "Pick a corner type for each routine.",
        "Place players and draw movement arrows." to "Tap to add markers and simple runs, passes, and crosses.",
        "Save set-piece ideas and review them later." to "Browse, edit, duplicate, and delete your scheme history.",
        "View football matches as an extra reference." to "A secondary Match Schedule screen powered by football-data.org.",
        "No account. No ads. No betting. No official logos." to "Your data stays on this device.",
    )

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .verticalScroll(rememberScrollState()),
    ) {
        // Orange header block.
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .background(StrongOrange)
                .padding(24.dp),
        ) {
            Box(
                modifier = Modifier
                    .size(56.dp)
                    .clip(RoundedCornerShape(14.dp))
                    .background(DarkSectionBackground),
                contentAlignment = Alignment.Center,
            ) {
                Text("⚑", color = StrongOrange, style = MaterialTheme.typography.headlineMedium)
            }
            Spacer(Modifier.height(14.dp))
            Text(
                "CornerKick Planner",
                style = MaterialTheme.typography.headlineLarge,
                color = WhiteText,
                fontWeight = FontWeight.ExtraBold,
            )
            Text(
                "Corner-kick tactics planner",
                style = MaterialTheme.typography.titleMedium,
                color = WhiteText,
            )
        }

        Column(
            modifier = Modifier.padding(20.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp),
        ) {
            bullets.forEach { (title, sub) ->
                Row(verticalAlignment = Alignment.Top) {
                    Box(
                        modifier = Modifier
                            .padding(top = 6.dp, end = 12.dp)
                            .size(10.dp)
                            .clip(RoundedCornerShape(3.dp))
                            .background(StrongOrange),
                    )
                    Column {
                        Text(
                            title,
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onBackground,
                        )
                        Text(
                            sub,
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                        )
                    }
                }
            }

            DisclaimerCard(text = AppStrings.TACTICS_DISCLAIMER)
            DisclaimerCard(text = AppStrings.MATCH_DISCLAIMER)

            Spacer(Modifier.height(4.dp))

            Button(
                onClick = onFinish,
                modifier = Modifier.fillMaxWidth().height(52.dp),
                shape = RoundedCornerShape(14.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = StrongOrange,
                    contentColor = WhiteText,
                ),
            ) {
                Text("Start Planning", fontWeight = FontWeight.Bold)
            }
            TextButton(
                onClick = onFinish,
                modifier = Modifier.fillMaxWidth(),
            ) {
                Text("Skip", color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
            Spacer(Modifier.height(12.dp))
        }
    }
}
