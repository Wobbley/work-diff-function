package no.lozo.workdiff

import com.amazonaws.services.lambda.runtime.Context
import com.amazonaws.services.lambda.runtime.RequestHandler
import com.amazonaws.services.lambda.runtime.events.APIGatewayProxyRequestEvent
import com.amazonaws.services.lambda.runtime.events.APIGatewayProxyResponseEvent
import com.beust.klaxon.Klaxon
import no.lozo.workdiff.clockify.ClockifyClient
import no.lozo.workdiff.dto.HandlerInput
import no.lozo.workdiff.dto.HandlerOutput
import java.lang.RuntimeException
import java.time.LocalDate
import kotlin.time.DurationUnit
import kotlin.time.ExperimentalTime
import kotlin.time.toDuration

@ExperimentalTime
class Function : RequestHandler<APIGatewayProxyRequestEvent, APIGatewayProxyResponseEvent> {

    override fun handleRequest(event: APIGatewayProxyRequestEvent, context: Context): APIGatewayProxyResponseEvent {
        try {
            val input = Klaxon().parse<HandlerInput>(event.body)!!
            val start = LocalDate.parse(input.startDate)
            val end = LocalDate.parse(input.endDate)

            val loggedSeconds =
                ClockifyClient.getSummaryReport(input.workspaceId, input.apiKey, start, end).toDuration(DurationUnit.SECONDS)
            val workingDays = calculateWorkDays(start, end)
            val workingSeconds = workingDays.times(input.hoursInWorkday).times(60).times(60).toDuration(DurationUnit.SECONDS)
            val diffSeconds = loggedSeconds.minus(workingSeconds)

            val output = HandlerOutput(
                loggedSeconds.inHours,
                workingSeconds.inHours,
                diffSeconds.inHours
            )

            val headers = mapOf(Pair("Access-Control-Allow-Origin", "*"))
            return APIGatewayProxyResponseEvent().withBody(Klaxon().toJsonString(output)).withHeaders(headers)

        } catch (e: RuntimeException) {
            return APIGatewayProxyResponseEvent().withStatusCode(500).withBody(e.stackTraceToString())
        }
    }
}