package no.lozo.workdiff

import com.amazonaws.services.lambda.runtime.Context
import com.amazonaws.services.lambda.runtime.RequestHandler
import no.lozo.workdiff.clockify.ClockifyClient
import no.lozo.workdiff.dto.HandlerInput
import no.lozo.workdiff.dto.HandlerOutput
import java.time.LocalDate
import kotlin.time.DurationUnit
import kotlin.time.ExperimentalTime
import kotlin.time.toDuration


@ExperimentalTime
class Function : RequestHandler<HandlerInput, HandlerOutput> {

    override fun handleRequest(input: HandlerInput?, context: Context?): HandlerOutput {
        input?.let {
            val start = LocalDate.parse(it.startDate)
            val end = LocalDate.parse(it.endDate)

            val loggedSeconds =
                ClockifyClient.getSummaryReport(it.workspaceId, it.apiKey, start, end).toDuration(DurationUnit.SECONDS)
            val workingDays = calculateWorkDays(start, end)
            val workingSeconds = workingDays.times(it.hoursInWorkday).times(60).times(60).toDuration(DurationUnit.SECONDS)
            val diffSeconds = loggedSeconds.minus(workingSeconds)

            return HandlerOutput(
                loggedSeconds.inHours,
                workingSeconds.inHours,
                diffSeconds.inHours
            )
        }
        return HandlerOutput(0.0, 0.0, 0.0)
    }
}