package bou.amine.apps.readerforselfossv2.android.background

import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Build
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationCompat.PRIORITY_DEFAULT
import androidx.core.app.NotificationCompat.PRIORITY_LOW
import androidx.room.Room
import androidx.work.Worker
import androidx.work.WorkerParameters
import bou.amine.apps.readerforselfossv2.android.MainActivity
import bou.amine.apps.readerforselfossv2.android.MyApp
import bou.amine.apps.readerforselfossv2.android.R
import bou.amine.apps.readerforselfossv2.android.model.preloadImages
import bou.amine.apps.readerforselfossv2.android.persistence.database.AppDatabase
import bou.amine.apps.readerforselfossv2.android.persistence.entities.ActionEntity
import bou.amine.apps.readerforselfossv2.android.persistence.migrations.MIGRATION_1_2
import bou.amine.apps.readerforselfossv2.android.persistence.migrations.MIGRATION_2_3
import bou.amine.apps.readerforselfossv2.android.persistence.migrations.MIGRATION_3_4
import bou.amine.apps.readerforselfossv2.android.utils.Config
import bou.amine.apps.readerforselfossv2.android.utils.network.isNetworkAvailable
import bou.amine.apps.readerforselfossv2.repository.Repository
import bou.amine.apps.readerforselfossv2.rest.SelfossModel
import bou.amine.apps.readerforselfossv2.utils.ItemType
import com.russhwolf.settings.Settings
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import org.kodein.di.DIAware
import org.kodein.di.instance
import java.util.*
import kotlin.concurrent.schedule
import kotlin.concurrent.thread

class LoadingWorker(val context: Context, params: WorkerParameters) : Worker(context, params), DIAware {
    lateinit var db: AppDatabase

    override val di by lazy { (applicationContext as MyApp).di }
    private val repository : Repository by instance()

override fun doWork(): Result {
    val settings = Settings()
    val periodicRefresh = settings.getBoolean("periodic_refresh", false)
    if (periodicRefresh) {

        if (context.isNetworkAvailable()) {

            CoroutineScope(Dispatchers.IO).launch {
                val notificationManager =
                    applicationContext.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

                val notification =
                    NotificationCompat.Builder(applicationContext, Config.syncChannelId)
                        .setContentTitle(context.getString(R.string.loading_notification_title))
                        .setContentText(context.getString(R.string.loading_notification_text))
                        .setOngoing(true)
                        .setPriority(PRIORITY_LOW)
                        .setChannelId(Config.syncChannelId)
                        .setSmallIcon(R.drawable.ic_stat_cloud_download_black_24dp)

                notificationManager.notify(1, notification.build())

                val notifyNewItems = settings.getBoolean("notify_new_items", false)

                db = Room.databaseBuilder(
                    applicationContext,
                    AppDatabase::class.java, "selfoss-database"
                ).addMigrations(MIGRATION_1_2).addMigrations(MIGRATION_2_3)
                    .addMigrations(MIGRATION_3_4).build()

                val actions = db.actionsDao().actions()

                actions.forEach { action ->
                    when {
                        action.read -> doAndReportOnFail(
                            repository.markAsRead(action.articleId.toInt()),
                            action
                        )
                        action.unread -> doAndReportOnFail(
                            repository.unmarkAsRead(action.articleId.toInt()),
                            action
                        )
                        action.starred -> doAndReportOnFail(
                            repository.starr(action.articleId.toInt()),
                            action
                        )
                        action.unstarred -> doAndReportOnFail(
                            repository.unstarr(action.articleId.toInt()),
                            action
                        )
                    }
                }

                if (context.isNetworkAvailable()) {
                    launch {
                        try {
                            val newItems = repository.allItems(ItemType.UNREAD)
                            handleNewItemsNotification(newItems, notifyNewItems, notificationManager)
                            val readItems = repository.allItems(ItemType.ALL)
                            val starredItems = repository.allItems(ItemType.STARRED)
                            // TODO: save all to DB
                        } catch (e: Throwable) {}
                    }
                }
            }
        }
    }
    return Result.success()
}

    private fun handleNewItemsNotification(
        newItems: List<SelfossModel.Item>?,
        notifyNewItems: Boolean,
        notificationManager: NotificationManager
    ) {
        CoroutineScope(Dispatchers.IO).launch {
                val apiItems = newItems.orEmpty()


                val newSize = apiItems.filter { it.unread }.size
                if (notifyNewItems && newSize > 0) {

                    val intent = Intent(context, MainActivity::class.java).apply {
                        flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
                    }
                    val pflags = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                        PendingIntent.FLAG_IMMUTABLE
                    } else {
                        0
                    }
                    val pendingIntent: PendingIntent = PendingIntent.getActivity(context, 0, intent, pflags)

                    val newItemsNotification =
                        NotificationCompat.Builder(applicationContext, Config.newItemsChannelId)
                            .setContentTitle(context.getString(R.string.new_items_notification_title))
                            .setContentText(
                                context.getString(
                                    R.string.new_items_notification_text,
                                    newSize
                                )
                            )
                            .setPriority(PRIORITY_DEFAULT)
                            .setChannelId(Config.newItemsChannelId)
                            .setContentIntent(pendingIntent)
                            .setAutoCancel(true)
                            .setSmallIcon(R.drawable.ic_tab_fiber_new_black_24dp)

                    Timer("", false).schedule(4000) {
                        notificationManager.notify(2, newItemsNotification.build())
                    }
                }
                apiItems.map { it.preloadImages(context) }
            Timer("", false).schedule(4000) {
                notificationManager.cancel(1)
            }
        }
    }

    private fun doAndReportOnFail(result: Boolean, action: ActionEntity) {
        // TODO: Failures should be reported
        if (result) {
            thread {
                db.actionsDao().delete(action)
            }
        }
    }
}