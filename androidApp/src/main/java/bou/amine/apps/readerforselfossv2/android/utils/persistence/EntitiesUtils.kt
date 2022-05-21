package bou.amine.apps.readerforselfossv2.android.utils.persistence

import bou.amine.apps.readerforselfossv2.android.model.getSourceTitle
import bou.amine.apps.readerforselfossv2.android.model.getTitleDecoded
import bou.amine.apps.readerforselfossv2.android.persistence.entities.AndroidItemEntity
import bou.amine.apps.readerforselfossv2.android.persistence.entities.SourceEntity
import bou.amine.apps.readerforselfossv2.android.persistence.entities.TagEntity
import bou.amine.apps.readerforselfossv2.rest.SelfossModel

fun TagEntity.toView(): SelfossModel.Tag =
        SelfossModel.Tag(
            this.tag,
            this.color,
            this.unread
        )

fun SourceEntity.toView(): SelfossModel.Source =
        SelfossModel.Source(
            this.id,
            this.title,
            this.tags.split(","),
            this.spout,
            this.error,
            this.icon
        )

fun SelfossModel.Source.toEntity(): SourceEntity =
        SourceEntity(
            this.id,
            this.getTitleDecoded(),
            this.tags.joinToString(","),
            this.spout,
            this.error,
            this.icon
        )

fun SelfossModel.Tag.toEntity(): TagEntity =
        TagEntity(
            this.tag,
            this.color,
            this.unread
        )

fun AndroidItemEntity.toView(): SelfossModel.Item =
        SelfossModel.Item(
            this.id,
            this.datetime,
            this.title,
            this.content,
            if (this.unread) 1 else 0,
            if (this.starred) 1 else 0,
            this.thumbnail,
            this.icon,
            this.link,
            this.sourcetitle,
            this.tags
        )

fun SelfossModel.Item.toEntity(): AndroidItemEntity =
    AndroidItemEntity(
        this.id,
        this.datetime,
        this.getTitleDecoded(),
        this.content,
        this.unread == 1,
        this.starred == 1,
        this.thumbnail,
        this.icon,
        this.link,
        this.getSourceTitle(),
        this.tags
    )