package com.kizitonwose.calendar.sample.shared

import android.os.Build
import androidx.annotation.RequiresApi
import androidx.compose.foundation.LocalIndication
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.composed
import androidx.compose.ui.semantics.Role
import com.kizitonwose.calendar.core.atStartOfMonth
import com.kizitonwose.calendar.core.nextMonth
import com.kizitonwose.calendar.core.previousMonth
import com.kizitonwose.calendar.core.yearMonth
import java.time.DayOfWeek
import java.time.LocalDate
import java.time.Month
import java.time.YearMonth
import java.time.format.TextStyle
import java.time.temporal.ChronoUnit
import java.util.*

@RequiresApi(Build.VERSION_CODES.O)
fun YearMonth.displayText(short: Boolean = false): String {
    return "${this.month.displayText(short = short)} ${this.year}"
}

@RequiresApi(Build.VERSION_CODES.O)
fun Month.displayText(short: Boolean = true): String {
    val style = if (short) TextStyle.SHORT else TextStyle.FULL
    return getDisplayName(style, Locale.ENGLISH)
}

@RequiresApi(Build.VERSION_CODES.O)
fun DayOfWeek.displayText(uppercase: Boolean = false): String {
    return getDisplayName(TextStyle.SHORT, Locale.ENGLISH).let { value ->
        if (uppercase) value.uppercase(Locale.ENGLISH) else value
    }
}

fun Modifier.clickable(
    enabled: Boolean = true,
    showRipple: Boolean = true,
    onClickLabel: String? = null,
    role: Role? = null,
    onClick: () -> Unit,
): Modifier = composed {
    clickable(
        interactionSource = remember { MutableInteractionSource() },
        indication = if (showRipple) LocalIndication.current else null,
        enabled = enabled,
        onClickLabel = onClickLabel,
        role = role,
        onClick = onClick,
    )
}

@RequiresApi(Build.VERSION_CODES.O)
data class DateSelection(val startDate: LocalDate? = null, val endDate: LocalDate? = null) {
    val daysBetween by lazy(LazyThreadSafetyMode.NONE) {
        if (startDate == null || endDate == null) null else {
            ChronoUnit.DAYS.between(startDate, endDate)
        }
    }
}

object ContinuousSelectionHelper {
    @RequiresApi(Build.VERSION_CODES.O)
    fun getSelection(
        clickedDate: LocalDate,
        dateSelection: DateSelection,
    ): DateSelection {
        val (selectionStartDate, selectionEndDate) = dateSelection
        return if (selectionStartDate != null) {
            if (clickedDate < selectionStartDate || selectionEndDate != null) {
                DateSelection(startDate = clickedDate, endDate = null)
            } else if (clickedDate != selectionStartDate) {
                DateSelection(startDate = selectionStartDate, endDate = clickedDate)
            } else {
                DateSelection(startDate = clickedDate, endDate = null)
            }
        } else {
            DateSelection(startDate = clickedDate, endDate = null)
        }
    }

    @RequiresApi(Build.VERSION_CODES.O)
    fun isInDateBetweenSelection(
        inDate: LocalDate,
        startDate: LocalDate,
        endDate: LocalDate,
    ): Boolean {
        if (startDate.yearMonth == endDate.yearMonth) return false
        if (inDate.yearMonth == startDate.yearMonth) return true
        val firstDateInThisMonth = inDate.yearMonth.nextMonth.atStartOfMonth()
        return firstDateInThisMonth in startDate..endDate && startDate != firstDateInThisMonth
    }

    @RequiresApi(Build.VERSION_CODES.O)
    fun isOutDateBetweenSelection(
        outDate: LocalDate,
        startDate: LocalDate,
        endDate: LocalDate,
    ): Boolean {
        if (startDate.yearMonth == endDate.yearMonth) return false
        if (outDate.yearMonth == endDate.yearMonth) return true
        val lastDateInThisMonth = outDate.yearMonth.previousMonth.atEndOfMonth()
        return lastDateInThisMonth in startDate..endDate && endDate != lastDateInThisMonth
    }
}
