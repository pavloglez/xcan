package com.jpdgbv.xcan.feature.dashboard.ui

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.DirectionsCar
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.SheetState
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.jpdgbv.xcan.core.model.CarProfile
import com.jpdgbv.xcan.core.ui.components.bounceClick
import com.jpdgbv.xcan.core.ui.components.pressBounce

private val ElectricBlue = Color(0xFF00C8FF)
private val CharcoalSurface = Color(0xFF1A1A2E)
private val LightGrayText = Color(0xFFB0BEC5)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CarPickerBottomSheet(
    cars: List<CarProfile>,
    sheetState: SheetState,
    onCarSelected: (CarProfile) -> Unit,
    onDismiss: () -> Unit,
    modifier: Modifier = Modifier
) {
    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = sheetState,
        containerColor = CharcoalSurface,
        modifier = modifier
    ) {
        Column(Modifier.padding(horizontal = 16.dp)) {
            Text(
                text = "Select Vehicle",
                color = Color.White,
                fontWeight = FontWeight.Bold,
                fontSize = 18.sp
            )
            Spacer(Modifier.height(4.dp))
            Text(
                text = "Choose which car to log this session for",
                color = LightGrayText,
                fontSize = 13.sp
            )
            Spacer(Modifier.height(16.dp))
            HorizontalDivider(color = Color.White.copy(alpha = 0.08f))
            LazyColumn {
                items(cars) { car ->
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .bounceClick(onClick = { onCarSelected(car) })
                            .padding(vertical = 12.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = Icons.Filled.DirectionsCar,
                            contentDescription = null,
                            tint = ElectricBlue,
                            modifier = Modifier.size(22.dp)
                        )
                        Spacer(Modifier.width(12.dp))
                        Column {
                            Text(
                                text = car.name,
                                color = Color.White,
                                fontWeight = FontWeight.Medium,
                                fontSize = 15.sp
                            )
                            Text(
                                text = "${car.year} · ${car.make}",
                                color = LightGrayText,
                                fontSize = 12.sp
                            )
                        }
                    }
                    HorizontalDivider(color = Color.White.copy(alpha = 0.05f))
                }
            }
            Spacer(Modifier.height(24.dp))
        }
    }
}
