package no.lozo.workdiff.dto

//Can't use data class due to binding issues Lambda
class HandlerInput {
    var workspaceId: String = ""
    var apiKey: String = ""
    var startDate: String = ""
    var endDate: String = ""
    var hoursInWorkday: Double = 8.0
}
