package no.lozo.workdiff

import java.time.DayOfWeek
import java.time.LocalDate
import java.time.temporal.ChronoUnit

fun calculateWorkDays(start: LocalDate,
                      end: LocalDate): Int {

    val holidays = arrayListOf<LocalDate>(LocalDate.of(2021, 5, 1))

    val daysBetween = ChronoUnit.DAYS.between(start, end.plusDays(1))

    return generateSequence(start, { date: LocalDate -> date.plusDays(1) })
            .take(daysBetween.toInt())
            .filterNot { holidays.contains(it) }
            .filterNot { it.dayOfWeek == DayOfWeek.SATURDAY || it.dayOfWeek == DayOfWeek.SUNDAY }
            .count()
}