package com.example.noteen.utils

import android.content.Context
import android.os.Build
import androidx.annotation.RequiresApi
import com.example.noteen.R
import org.json.JSONObject
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter

fun String.escapedForJS(): String {
    return JSONObject.quote(this)
}

//@RequiresApi(Build.VERSION_CODES.O)
//fun formatNoteDate(dateString: String): String {
//    return try {
//        val dateTime = LocalDateTime.parse(dateString, DateTimeFormatter.ISO_DATE_TIME)
//        val now = LocalDateTime.now()
//        val today = now.toLocalDate()
//        val yesterday = today.minusDays(1)
//        val inputDate = dateTime.toLocalDate()
//        val timeFormatter = DateTimeFormatter.ofPattern("HH:mm")
//
//        when {
//            inputDate == today -> "Today ${dateTime.format(timeFormatter)}"
//            inputDate == yesterday -> "Yesterday ${dateTime.format(timeFormatter)}"
//            inputDate.year == today.year -> dateTime.format(DateTimeFormatter.ofPattern("MMM dd"))
//            else -> dateTime.format(DateTimeFormatter.ofPattern("dd/MM/yyyy"))
//        }
//    } catch (e: Exception) {
//        "Invalid date"
//    }
//}

@RequiresApi(Build.VERSION_CODES.O)
fun formatNoteDate(context: Context, dateString: String): String {
    return try {
        val dateTime = LocalDateTime.parse(dateString, DateTimeFormatter.ISO_DATE_TIME)
        val now = LocalDateTime.now()
        val duration = java.time.Duration.between(dateTime, now)

        val minutes = duration.toMinutes()
        val hours = duration.toHours()
        val days = duration.toDays()

        val timeFormatter = DateTimeFormatter.ofPattern("HH:mm")
        val today = now.toLocalDate()
        val yesterday = today.minusDays(1)
        val inputDate = dateTime.toLocalDate()

        when {
            minutes < 1 -> context.getString(R.string.just_now)
            minutes < 60 -> "${minutes} ${context.getString(R.string.minutes_ago)}"
            hours <= 12 -> "${hours} ${context.getString(R.string.hours_ago)}"
            hours > 12 && inputDate == today -> "${context.getString(R.string.today)} ${dateTime.format(timeFormatter)}"
            inputDate == yesterday -> "${context.getString(R.string.yesterday)} ${dateTime.format(timeFormatter)}"
            days <= 3 -> "${days} ${context.getString(R.string.days_ago)}"
            inputDate.year == today.year -> dateTime.format(DateTimeFormatter.ofPattern("MMM dd"))
            else -> dateTime.format(DateTimeFormatter.ofPattern("dd/MM/yyyy"))
        }
    } catch (e: Exception) {
        "Invalid date"
    }
}
