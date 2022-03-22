package bou.amine.apps.readerforselfossv2.android

import androidx.test.espresso.matcher.ViewMatchers
import android.view.View
import android.widget.EditText
import org.hamcrest.Description
import org.hamcrest.Matcher
import org.hamcrest.Matchers
import org.hamcrest.TypeSafeMatcher

fun isHintOrErrorEnabled(): Matcher<View> =
        object : TypeSafeMatcher<View>() {
            override fun describeTo(description: Description?) {
            }

            override fun matchesSafely(item: View?): Boolean {
                if (item !is EditText) {
                    return false
                }

                return item.error.isNotEmpty()
            }
        }

fun withMenu(id: Int, titleId: Int): Matcher<View> =
        Matchers.anyOf(
                ViewMatchers.withId(id),
                ViewMatchers.withText(titleId)
        )
