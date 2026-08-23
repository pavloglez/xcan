package com.jpdgbv.xcan.feature.config

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.jpdgbv.xcan.core.ui.components.GlassTopAppBar
import com.jpdgbv.xcan.core.ui.LocalHazeState
import dev.chrisbanes.haze.hazeSource
import com.jpdgbv.xcan.core.ui.theme.DeepCharcoal
import com.jpdgbv.xcan.core.ui.theme.ElectricBlue
import androidx.compose.ui.tooling.preview.Preview
import com.jpdgbv.xcan.core.ui.theme.XCanTheme

@Composable
fun ConfigRoute(
    viewModel: ConfigViewModel = hiltViewModel()
) {
    val state by viewModel.state.collectAsStateWithLifecycle()

    ConfigScreen(
        state = state,
        onToggleMetric = { viewModel.onIntent(ConfigIntent.ToggleMetric(it)) }
    )
}

@Composable
fun ConfigScreen(
    state: ConfigState,
    onToggleMetric: (Boolean) -> Unit
) {
    Scaffold(
        modifier = Modifier.fillMaxSize(),
        containerColor = DeepCharcoal,
        topBar = {
            GlassTopAppBar(title = "ECU Configuration")
        }
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(DeepCharcoal)
                .hazeSource(LocalHazeState.current)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding)
                    .padding(horizontal = 16.dp)
            ) {

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = "Use Metric Units",
                    style = MaterialTheme.typography.headlineMedium,
                    color = MaterialTheme.colorScheme.onSurface
                )
                Text(
                    text = "Show speed in KM/H instead of MPH",
                    style = MaterialTheme.typography.bodyLarge,
                    color = MaterialTheme.colorScheme.onBackground
                )
            }
            Switch(
                checked = state.useMetric,
                onCheckedChange = onToggleMetric,
                colors = SwitchDefaults.colors(
                    checkedThumbColor = ElectricBlue,
                    checkedTrackColor = ElectricBlue.copy(alpha = 0.5f)
                )
            )
        }
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
fun ConfigScreenPreview() {
    XCanTheme {
        ConfigScreen(
            state = ConfigState(useMetric = true),
            onToggleMetric = {}
        )
    }
}
