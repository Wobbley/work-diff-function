package no.lozo.workdiff

import com.amazonaws.services.lambda.runtime.Context
import com.amazonaws.services.lambda.runtime.RequestHandler
import com.amazonaws.services.lambda.runtime.events.APIGatewayProxyRequestEvent
import com.amazonaws.services.lambda.runtime.events.APIGatewayProxyResponseEvent
import com.beust.klaxon.Klaxon
import no.lozo.workdiff.clockify.ClockifyClient
import no.lozo.workdiff.dto.DiffRequest
import no.lozo.workdiff.dto.DiffResponse
import java.lang.RuntimeException
import java.time.LocalDate
import kotlin.time.DurationUnit
import kotlin.time.ExperimentalTime
import kotlin.time.toDuration

@ExperimentalTime
class Function : RequestHandler<APIGatewayProxyRequestEvent, APIGatewayProxyResponseEvent> {

    override fun handleRequest(event: APIGatewayProxyRequestEvent, context: Context): APIGatewayProxyResponseEvent {
        try {
            val requestBody = Klaxon().parse<DiffRequest>(event.body)!!
            val start = LocalDate.parse(requestBody.startDate)
            val end = LocalDate.parse(requestBody.endDate)

            val loggedSeconds =
                ClockifyClient.getSummaryReport(requestBody.workspaceId, requestBody.apiKey, start, end)
                    .toDuration(DurationUnit.SECONDS)
            val workingDays = calculateWorkDays(start, end)
            val workingSeconds =
                workingDays.times(requestBody.hoursInWorkday).times(60).times(60).toDuration(DurationUnit.SECONDS)
            val diffSeconds = loggedSeconds.minus(workingSeconds)

            val headers = mapOf(Pair("Access-Control-Allow-Origin", "*"))
            val responseBody = DiffResponse(loggedSeconds.inHours, workingSeconds.inHours, diffSeconds.inHours)

            return APIGatewayProxyResponseEvent().withBody(Klaxon().toJsonString(responseBody)).withHeaders(headers)

        } catch (e: RuntimeException) {
            return APIGatewayProxyResponseEvent().withStatusCode(500).withBody(e.stackTraceToString())
        }
    }
}