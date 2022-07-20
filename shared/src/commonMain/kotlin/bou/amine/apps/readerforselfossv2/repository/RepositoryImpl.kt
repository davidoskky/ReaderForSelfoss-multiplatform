package bou.amine.apps.readerforselfossv2.repository

import bou.amine.apps.readerforselfossv2.rest.SelfossApi
import bou.amine.apps.readerforselfossv2.rest.SelfossModel
import com.russhwolf.settings.Settings

class RepositoryImpl(api: SelfossApi) : Repository {
    val settings = Settings()

    override lateinit var items: List<SelfossModel.Item>
    override lateinit var selectedItems: List<SelfossModel.Item>

    override fun getItems(): List<SelfossModel.Item> {
        return items
    }

    override fun getMoreItems(): List<SelfossModel.Item> {
        TODO("Not yet implemented")
    }

    override fun stats(): SelfossModel.Stats {
        TODO("Not yet implemented")
    }

    override fun getTags(): List<SelfossModel.Tag> {
        TODO("Not yet implemented")
    }

    override fun getSpouts(): List<SelfossModel.Spout> {
        TODO("Not yet implemented")
    }

    override fun getSources(): List<SelfossModel.Source> {
        TODO("Not yet implemented")
    }

    override fun markAsRead(id: String): Boolean {
        TODO("Not yet implemented")
    }

    override fun unmarkAsRead(id: String): Boolean {
        TODO("Not yet implemented")
    }

    override fun starr(id: String): Boolean {
        TODO("Not yet implemented")
    }

    override fun unstarr(id: String): Boolean {
        TODO("Not yet implemented")
    }

    override fun markAllAsRead(ids: List<String>): Boolean {
        TODO("Not yet implemented")
    }

    override fun createSource(
        title: String,
        url: String,
        spout: String,
        tags: String,
        filter: String
    ): Boolean {
        TODO("Not yet implemented")
    }

    override fun deleteSource(id: Int): Boolean {
        TODO("Not yet implemented")
    }

    override fun updateRemote(): Boolean {
        TODO("Not yet implemented")
    }

    override fun login(): Boolean {
        TODO("Not yet implemented")
    }

    override fun refreshLoginInformation(): Boolean {
        TODO("Not yet implemented")
    }
}