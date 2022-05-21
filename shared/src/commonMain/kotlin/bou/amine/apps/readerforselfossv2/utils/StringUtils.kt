package bou.amine.apps.readerforselfossv2.utils

fun String?.isEmptyOrNullOrNullString(): Boolean =
    this == null || this == "null" || this.isEmpty()

fun String.longHash(): Long {
    var h = 98764321261L
    val l = this.length
    val chars = this.toCharArray()

    for (i in 0 until l) {
        h = 31 * h + chars[i].code.toLong()
    }
    return h
}

fun String.toStringUriWithHttp(): String =
    if (!this.startsWith("https://") && !this.startsWith("http://")) {
        "http://" + this
    } else {
        this
    }