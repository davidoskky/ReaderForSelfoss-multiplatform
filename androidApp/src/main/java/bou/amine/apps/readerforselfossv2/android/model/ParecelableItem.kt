package bou.amine.apps.readerforselfossv2.android.model

import android.os.Build
import android.os.Parcel
import android.os.Parcelable
import androidx.annotation.RequiresApi
import bou.amine.apps.readerforselfossv2.rest.SelfossModel
import com.google.gson.annotations.SerializedName

fun SelfossModel.Item.toParcelable() : ParecelableItem =
    ParecelableItem(
        this.id,
        this.datetime,
        this.title,
        this.content,
        this.unread,
        this.starred,
        this.thumbnail,
        this.icon,
        this.link,
        this.sourcetitle,
        this.tags.joinToString(",")
    )
fun ParecelableItem.toModel() : SelfossModel.Item =
    SelfossModel.Item(
        this.id,
        this.datetime,
        this.title,
        this.content,
        this.unread,
        this.starred,
        this.thumbnail,
        this.icon,
        this.link,
        this.sourcetitle,
        this.tags.split(",")
    )
data class ParecelableItem(
    @SerializedName("id") val id: Int,
    @SerializedName("datetime") val datetime: String,
    @SerializedName("title") val title: String,
    @SerializedName("content") val content: String,
    @SerializedName("unread") var unread: Boolean,
    @SerializedName("starred") var starred: Boolean,
    @SerializedName("thumbnail") val thumbnail: String?,
    @SerializedName("icon") val icon: String?,
    @SerializedName("link") val link: String,
    @SerializedName("sourcetitle") val sourcetitle: String,
    @SerializedName("tags") val tags: String
) : Parcelable {

    companion object {
        @JvmField
        val CREATOR: Parcelable.Creator<ParecelableItem> = object : Parcelable.Creator<ParecelableItem> {
            override fun createFromParcel(source: Parcel): ParecelableItem = ParecelableItem(source)
            override fun newArray(size: Int): Array<ParecelableItem?> = arrayOfNulls(size)
        }
    }

    constructor(source: Parcel) : this(
        id = source.readInt(),
        datetime = source.readString().orEmpty(),
        title = source.readString().orEmpty(),
        content = source.readString().orEmpty(),
        unread = source.readByte().toInt() != 0,
        starred = source.readByte().toInt() != 0,
        thumbnail = source.readString(),
        icon = source.readString(),
        link = source.readString().orEmpty(),
        sourcetitle = source.readString().orEmpty(),
        tags = source.readString().orEmpty()
    )

    override fun describeContents() = 0

    override fun writeToParcel(dest: Parcel, flags: Int) {
        dest.writeInt(id)
        dest.writeString(datetime)
        dest.writeString(title)
        dest.writeString(content)
        dest.writeByte(if (unread) 1 else 0)
        dest.writeByte(if (starred) 1 else 0)
        dest.writeString(thumbnail)
        dest.writeString(icon)
        dest.writeString(link)
        dest.writeString(sourcetitle)
        dest.writeString(tags)
    }
}