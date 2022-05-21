package bou.amine.apps.readerforselfossv2.service

import bou.amine.apps.readerforselfossv2.utils.DateUtils

class SearchService(val dateUtils: DateUtils) {
    var displayedItems: String = "unread"
        set(value) {
            field = when (value) {
                "all" -> "all"
                "unread" -> "unread"
                "read" -> "read"
                "starred" -> "starred"
                else -> "all"
            }
        }

    var position = 0
    var searchFilter: String? = null
    var sourceIDFilter: Long? = null
    var sourceFilter: String? = null
    var tagFilter: String? = null
    var itemsCaching = false

    var badgeUnread = -1
    var badgeAll = -1
    var badgeStarred = -1
}