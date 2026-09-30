package com.example.ui.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.ResearchStep
import com.example.ui.theme.BrandCyanPrimary
import com.example.ui.theme.BrandEmeraldAccent

@Composable
fun ResearchProgressCard(steps: List<ResearchStep>) {
    if (steps.isEmpty()) return

    Surface(
        shape = RoundedCornerShape(14.dp),
        color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.6f),
        border = androidx.compose.foundation.BorderStroke(1.dp, BrandCyanPrimary.copy(alpha = 0.3f)),
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 8.dp)
    ) {
        Column(modifier = Modifier.padding(14.dp)) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween,
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text("🔎", fontSize = 16.sp)
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = "ডিপ রিসার্চ অগ্রগতি (Deep Research Workflow)",
                        style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold),
                        color = BrandCyanPrimary
                    )
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            steps.forEachIndexed { index, step ->
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 4.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Box(
                        modifier = Modifier
                            .size(24.dp)
                            .clip(CircleShape)
                            .background(
                                when {
                                    step.isDone -> BrandEmeraldAccent.copy(alpha = 0.2f)
                                    step.isActive -> BrandCyanPrimary.copy(alpha = 0.2f)
                                    else -> MaterialTheme.colorScheme.surface
                                }
                            )
                            .border(
                                1.dp,
                                when {
                                    step.isDone -> BrandEmeraldAccent
                                    step.isActive -> BrandCyanPrimary
                                    else -> MaterialTheme.colorScheme.outline.copy(alpha = 0.4f)
                                },
                                CircleShape
                            ),
                        contentAlignment = Alignment.Center
                    ) {
                        if (step.isDone) {
                            Icon(
                                imageVector = Icons.Default.Check,
                                contentDescription = "Done",
                                tint = BrandEmeraldAccent,
                                modifier = Modifier.size(14.dp)
                            )
                        } else if (step.isActive) {
                            CircularProgressIndicator(
                                modifier = Modifier.size(12.dp),
                                color = BrandCyanPrimary,
                                strokeWidth = 1.8.dp
                            )
                        } else {
                            Text(
                                text = "${step.stepIndex}",
                                fontSize = 11.sp,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }

                    Spacer(modifier = Modifier.width(10.dp))

                    Column {
                        Text(
                            text = step.title,
                            style = MaterialTheme.typography.labelMedium.copy(
                                fontWeight = if (step.isActive || step.isDone) FontWeight.Bold else FontWeight.Normal
                            ),
                            color = when {
                                step.isDone -> BrandEmeraldAccent
                                step.isActive -> BrandCyanPrimary
                                else -> MaterialTheme.colorScheme.onSurfaceVariant
                            }
                        )
                        if (step.isActive && step.detail.isNotBlank()) {
                            Text(
                                text = step.detail,
                                style = MaterialTheme.typography.bodySmall.copy(fontSize = 11.sp),
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                }
            }
        }
    }
}
