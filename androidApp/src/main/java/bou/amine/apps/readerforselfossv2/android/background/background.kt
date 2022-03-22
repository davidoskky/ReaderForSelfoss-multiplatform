package bou.amine.apps.readerforselfossv2.android.background

import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Build
import androidx.preference.PreferenceManager
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationCompat.PRIORITY_DEFAULT
import androidx.core.app.NotificationCompat.PRIORITY_LOW
import androidx.room.Room
import androidx.work.Worker
import androidx.work.WorkerParameters
import bou.amine.apps.readerforselfossv2.android.MainActivity
import bou.amine.apps.readerforselfossv2.android.R
import bou.amine.apps.readerforselfossv2.android.api.selfoss.SelfossApi
import bou.amine.apps.readerforselfossv2.android.api.selfoss.getAndStoreAllItems
import bou.amine.apps.readerforselfossv2.android.persistence.database.AppDatabase
import bou.amine.apps.readerforselfossv2.android.persistence.entities.ActionEntity
import bou.amine.apps.readerforselfossv2.android.persistence.migrations.MIGRATION_1_2
import bou.amine.apps.readerforselfossv2.android.persistence.migrations.MIGRATION_2_3
import bou.amine.apps.readerforselfossv2.android.persistence.migrations.MIGRATION_3_4
import bou.amine.apps.readerforselfossv2.android.utils.Config
import bou.amine.apps.readerforselfossv2.android.utils.SharedItems
import bou.amine.apps.readerforselfossv2.android.utils.network.isNetworkAvailable
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import java.util.*
import kotlin.concurrent.schedule
import kotlin.concurrent.thread

class LoadingWorker(val context: Context, params: WorkerParameters) : Worker(context, params) {
    lateinit var db: AppDatabase

override fun doWork(): Result {
    val settings =
        this.context.getSharedPreferences(Config.settingsName, Context.MODE_PRIVATE)
    val sharedPref = PreferenceManager.getDefaultSharedPreferences(this.context)
    val periodicRefresh = sharedPref.getBoolean("periodic_refresh", false)
    if (periodicRefresh) {
        val api = SelfossApi(
            this.context,
            null,
            settings.getBoolean("isSelfSignedCert", false),
            sharedPref.getString("api_timeout", "-1")!!.toLong()
        )

        if (isNetworkAvailable(context)) {

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

                val notifyNewItems = sharedPref.getBoolean("notify_new_items", false)

                db = Room.databaseBuilder(
                    applicationContext,
                    AppDatabase::class.java, "selfoss-database"
                ).addMigrations(MIGRATION_1_2).addMigrations(MIGRATION_2_3)
                    .addMigrations(MIGRATION_3_4).build()

                val actions = db.actionsDao().actions()

                actions.forEach { action ->
                    when {
                        action.read -> doAndReportOnFail(
                            api.markItem(action.articleId),
                            action
                        )
                        action.unread -> doAndReportOnFail(
                            api.unmarkItem(action.articleId),
                            action
                        )
                        action.starred -> doAndReportOnFail(
                            api.starrItem(action.articleId),
                            action
                        )
                        action.unstarred -> doAndReportOnFail(
                            api.unstarrItem(action.articleId),
                            action
                        )
                    }
                }

                getAndStoreAllItems(context, api, db)
                SharedItems.updateDatabase(db)
                storeItems(notifyNewItems, notificationManager)
            }
        }
    }
    return Result.success()
}

    private fun storeItems(notifyNewItems: Boolean, notificationManager: NotificationManager) {
        CoroutineScope(Dispatchers.IO).launch {
                val apiItems = SharedItems.items


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

    private fun <T> doAndReportOnFail(call: Call<T>, action: ActionEntity) {
        call.enqueue(object : Callback<T> {
            override fun onResponse(
                call: Call<T>,
                response: Response<T>
            ) {
                thread {
                    db.actionsDao().delete(action)
                }
            }

            override fun onFailure(call: Call<T>, t: Throwable) {
            }
        })
    }
}