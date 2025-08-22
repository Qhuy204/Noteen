package com.example.noteen.ui.component

import android.os.Build
import androidx.annotation.RequiresApi
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.DpSize
import androidx.compose.ui.unit.dp
import com.commandiron.wheel_picker_compose.WheelDateTimePicker
import com.commandiron.wheel_picker_compose.core.TimeFormat
import com.commandiron.wheel_picker_compose.core.WheelPickerDefaults
import com.example.noteen.ui.screen.brandBlue
import com.example.noteen.ui.screen.textSecondary
import java.time.LocalDateTime

@RequiresApi(Build.VERSION_CODES.O)
@Composable
fun DateTimePicker(
    initialDateTime: LocalDateTime,
    onDateTimeSelected: (LocalDateTime) -> Unit
) {
    WheelDateTimePicker(
        modifier = Modifier.fillMaxWidth(),
        startDateTime = initialDateTime,
        minDateTime = LocalDateTime.now(),
        maxDateTime = LocalDateTime.now().plusYears(100),
        timeFormat = TimeFormat.HOUR_24,
        size = DpSize(360.dp, 160.dp),
        rowCount = 5,
        textStyle = MaterialTheme.typography.bodyLarge,
        textColor = textSecondary,
        selectorProperties = WheelPickerDefaults.selectorProperties(
            enabled = true,
            shape = RoundedCornerShape(16.dp),
            color = brandBlue.copy(alpha = 0.1f),
            border = null
        ),
        onSnappedDateTime = { snapped ->
            if (snapped.isAfter(LocalDateTime.now())) {
                onDateTimeSelected(snapped)
            } else {
                onDateTimeSelected(LocalDateTime.now().plusMinutes(1))
            }
        }
    )
}