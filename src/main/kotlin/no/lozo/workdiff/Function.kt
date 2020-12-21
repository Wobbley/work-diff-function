package no.lozo.workdiff

import com.microsoft.azure.functions.*
import com.microsoft.azure.functions.annotation.AuthorizationLevel
import com.microsoft.azure.functions.annotation.FunctionName
import com.microsoft.azure.functions.annotation.HttpTrigger
import no.lozo.workdiff.clockify.ClockifyClient
import no.lozo.workdiff.dto.DiffRequest
import no.lozo.workdiff.dto.DiffResponse
import java.time.LocalDate
import java.util.*
import kotlin.time.DurationUnit
import kotlin.time.ExperimentalTime
import kotlin.time.toDuration

class Function {

    @ExperimentalTime
    @FunctionName("diff")
    fun run(
        @HttpTrigger(name = "request", methods = [HttpMethod.POST], authLevel = AuthorizationLevel.ANONYMOUS)
        diffRequest: HttpRequestMessage<Optional<DiffRequest>>,
    ): HttpResponseMessage {

        if (!diffRequest.body.isPresent) {
            return diffRequest.createResponseBuilder(HttpStatus.BAD_REQUEST)
                .body("Document not found.")
                .build();
        }

        val (workspaceId, apiKey, startString, endString, hoursInWorkday) = diffRequest.body.get()
        val start = LocalDate.parse(startString)
        val end = LocalDate.parse(endString)

        val loggedSeconds =
            ClockifyClient.getSummaryReport(workspaceId, apiKey, start, end).toDuration(DurationUnit.SECONDS)
        val workingDays = calculateWorkDays(start, end)
        val workingSeconds = workingDays.times(hoursInWorkday).times(60).times(60).toDuration(DurationUnit.SECONDS)
        val diffSeconds = loggedSeconds.minus(workingSeconds)

        val responseBody = DiffResponse(
            loggedSeconds.inHours,
            workingSeconds.inHours,
            diffSeconds.inHours
        )

        return diffRequest.createResponseBuilder(HttpStatus.OK)
            .header("Content-Type", "application/json")
            .body(responseBody)
            .build()
    }
}