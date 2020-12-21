package no.lozo.workdiff.dto

data class DiffRequest(val workspaceId: String,
                       val apiKey: String,
                       val startDate: String,
                       val endDate: String,
                       val hoursInWorkday: Double = 8.0)