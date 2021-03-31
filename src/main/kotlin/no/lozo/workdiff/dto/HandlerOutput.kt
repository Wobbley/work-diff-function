package no.lozo.workdiff.dto

import kotlin.time.ExperimentalTime

@ExperimentalTime
data class HandlerOutput(val loggedHours: Double,
                         val expectedHours: Double,
                         val diffHours: Double)