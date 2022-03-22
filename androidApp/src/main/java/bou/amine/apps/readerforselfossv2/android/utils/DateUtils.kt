package bou.amine.apps.readerforselfossv2.android.utils

import android.text.format.DateUtils
import java.time.Instant
import java.time.LocalDateTime
import java.time.OffsetDateTime
import java.time.ZoneOffset
import java.time.format.DateTimeFormatter

fun parseDate(dateString: String): Instant {

    val FORMATTERV1 = "yyyy-MM-dd HH:mm:ss"

    return if (Config.apiVersion >= 4) {
        OffsetDateTime.parse(dateString).toInstant()
    } else {
        LocalDateTime.parse(dateString, DateTimeFormatter.ofPattern(FORMATTERV1)).toInstant(ZoneOffset.UTC)
    }
}

fun parseRelativeDate(dateString: String): String {

    val date = parseDate(dateString)

    return " " + DateUtils.getRelativeTimeSpanString(
            date.toEpochMilli(),
            Instant.now().toEpochMilli(),
            DateUtils.MINUTE_IN_MILLIS,
            DateUtils.FORMAT_ABBREV_RELATIVE
    )
}