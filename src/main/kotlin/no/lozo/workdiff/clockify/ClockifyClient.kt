package no.lozo.workdiff.clockify

import com.beust.klaxon.JsonObject
import com.beust.klaxon.Parser
import com.beust.klaxon.lookup
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import java.io.IOException
import java.time.LocalDate
import java.time.LocalTime
import java.time.OffsetDateTime
import java.time.ZoneOffset

object ClockifyClient {
    private val okHttpClient = OkHttpClient()
    private val parser = Parser.default()
    private const val baseUrl: String = "https://reports.api.clockify.me/v1"

    fun getSummaryReport(workspaceID: String,
                         apiKey: String,
                         start: LocalDate,
                         end: LocalDate): Int {

        val startDateTime = OffsetDateTime.of(start, LocalTime.of(0,0,1), ZoneOffset.UTC).withNano(0)
        val endDateTime = OffsetDateTime.of(end.plusDays(1), LocalTime.of(0,0,1), ZoneOffset.UTC).withNano(0)

        val postBody =
                """
            {
              "dateRangeStart": "$startDateTime",
              "dateRangeEnd": "$endDateTime",
            	  "summaryFilter": {
                "groups": [
                  "USER"
                ]
              },
            	"exportType": "JSON"
            }
        """.trimIndent()

        val request = Request.Builder()
                .addHeader("X-Api-Key", apiKey)
                .url("$baseUrl/workspaces/$workspaceID/reports/summary")
                .post(postBody.toRequestBody("application/json".toMediaType()))
                .build()

        okHttpClient.newCall(request).execute().use { response ->
            if (!response.isSuccessful) throw IOException("Unexpected code $response")

            val responseStream = response.body!!.byteStream()
            val jsonArray = (parser.parse(responseStream) as JsonObject).lookup<Int>("totals.totalTime")

            return jsonArray[0]
        }
    }
}