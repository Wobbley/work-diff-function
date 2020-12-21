package no.lozo.workdiff.dto

import kotlin.time.ExperimentalTime

@ExperimentalTime
data class DiffResponse(val loggedHours: Double,
                        val expectedHours: Double,
                        val diffHours: Double)