package bou.amine.apps.readerforselfossv2.android.utils

import bou.amine.apps.readerforselfossv2.android.utils.Config
import bou.amine.apps.readerforselfossv2.android.utils.parseDate
import org.junit.Test

class DateUtilsTest {

    @Test
    fun parseDateV4() {

        Config.apiVersion = 4
        val dateString = "2013-04-07T13:43:00+01:00"

        val milliseconds = parseDate(dateString).toEpochMilli()
        val correctMilliseconds : Long = 1365338580000

        assert(milliseconds == correctMilliseconds)
    }

    @Test
    fun parseDateV1() {
        Config.apiVersion = 0
        val dateString = "2013-04-07 13:43:00"

        val milliseconds = parseDate(dateString).toEpochMilli()
        val correctMilliseconds = 1365342180000

        assert(milliseconds == correctMilliseconds)
    }
}