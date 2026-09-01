package com.sager.jtm.ui

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.outlined.RadioButtonUnchecked
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.sager.jtm.R
import com.sager.jtm.core.Journey
import com.sager.jtm.core.JourneyStatus

/** A reusable, fully stateless journey summary card. */
@Composable
fun JourneyCard(
  journey: Journey,
  onClick: () -> Unit,
  modifier: Modifier = Modifier,
  selected: Boolean = false,
  onToggleCompleted: (() -> Unit)? = null,
) {
  val containerColor =
    if (selected) MaterialTheme.colorScheme.secondaryContainer
    else MaterialTheme.colorScheme.surfaceContainerLow
  val contentColor =
    if (selected) MaterialTheme.colorScheme.onSecondaryContainer
    else MaterialTheme.colorScheme.onSurface

  Card(
    onClick = onClick,
    modifier = modifier.fillMaxWidth(),
    colors = CardDefaults.cardColors(containerColor = containerColor, contentColor = contentColor),
  ) {
    Column(
      modifier = Modifier.padding(16.dp),
      verticalArrangement = Arrangement.spacedBy(12.dp),
    ) {
      Row(verticalAlignment = Alignment.CenterVertically) {
        Icon(
          imageVector =
            if (journey.status == JourneyStatus.COMPLETED) Icons.Filled.CheckCircle
            else Icons.Outlined.RadioButtonUnchecked,
          contentDescription =
            stringResource(
              if (journey.status == JourneyStatus.COMPLETED) R.string.status_completed
              else R.string.status_upcoming
            ),
          tint =
            if (journey.status == JourneyStatus.COMPLETED) MaterialTheme.colorScheme.primary
            else MaterialTheme.colorScheme.onSurfaceVariant,
        )
        Spacer(Modifier.width(12.dp))
        Column(modifier = Modifier.weight(1f)) {
          Text(
            text = journey.title,
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.SemiBold,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
          )
          Text(
            text = "${journey.lineName} · ${journey.region.displayName}",
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
          )
        }
        Text(
          text = journey.dateLabel,
          style = MaterialTheme.typography.labelMedium,
          color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
      }

      Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically,
      ) {
        Text(
          text = "${journey.origin}  →  ${journey.destination}",
          style = MaterialTheme.typography.bodyLarge,
          modifier = Modifier.weight(1f),
          maxLines = 2,
        )
        if (onToggleCompleted != null) {
          FilledTonalButton(onClick = onToggleCompleted) {
            Text(
              stringResource(
                if (journey.status == JourneyStatus.COMPLETED) R.string.action_mark_upcoming
                else R.string.action_mark_completed
              )
            )
          }
        }
      }
    }
  }
}
