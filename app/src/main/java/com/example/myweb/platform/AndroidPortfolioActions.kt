package com.example.myweb.platform

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Build
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import com.example.myweb.MainActivity
import com.example.myweb.R

data class DeviceSnapshot(
    val manufacturer: String,
    val model: String,
    val androidVersion: String,
    val sdkLevel: Int,
    val screenWidthDp: Int,
    val screenHeightDp: Int,
    val densityDpi: Int,
    val localeTag: String
)

class AndroidPortfolioActions(private val context: Context) {
    companion object {
        const val PORTFOLIO_URL = "https://github.com/LeeMooho"
        const val NOTIFICATION_CHANNEL_ID = "portfolio_lab"
        const val NOTIFICATION_ID = 1001
    }

    fun deviceSnapshot(): DeviceSnapshot {
        val configuration = context.resources.configuration
        val locale = configuration.locales[0]
        return DeviceSnapshot(
            manufacturer = Build.MANUFACTURER.ifBlank { context.getString(R.string.unknown_value) },
            model = Build.MODEL.ifBlank { context.getString(R.string.unknown_value) },
            androidVersion = Build.VERSION.RELEASE.ifBlank { context.getString(R.string.unknown_value) },
            sdkLevel = Build.VERSION.SDK_INT,
            screenWidthDp = configuration.screenWidthDp,
            screenHeightDp = configuration.screenHeightDp,
            densityDpi = configuration.densityDpi,
            localeTag = locale.toLanguageTag()
        )
    }

    fun copyPortfolioLink() {
        val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
        clipboard.setPrimaryClip(
            ClipData.newPlainText(context.getString(R.string.portfolio_link_label), PORTFOLIO_URL)
        )
    }

    fun sharePortfolio() {
        val shareIntent = Intent(Intent.ACTION_SEND).apply {
            type = "text/plain"
            putExtra(Intent.EXTRA_SUBJECT, context.getString(R.string.portfolio_share_subject))
            putExtra(Intent.EXTRA_TEXT, context.getString(R.string.portfolio_share_text, PORTFOLIO_URL))
        }
        context.startActivity(
            Intent.createChooser(shareIntent, context.getString(R.string.share_chooser_title))
                .addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        )
    }

    fun openGitHubProfile() {
        context.startActivity(
            Intent(Intent.ACTION_VIEW, Uri.parse(PORTFOLIO_URL)).addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        )
    }

    fun showDemoNotification(): Boolean = try {
        val manager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            manager.createNotificationChannel(
                NotificationChannel(
                    NOTIFICATION_CHANNEL_ID,
                    context.getString(R.string.notification_channel_name),
                    NotificationManager.IMPORTANCE_DEFAULT
                ).apply {
                    description = context.getString(R.string.notification_channel_description)
                }
            )
        }
        val openApp = PendingIntent.getActivity(
            context,
            0,
            Intent(context, MainActivity::class.java),
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )
        val notification = NotificationCompat.Builder(context, NOTIFICATION_CHANNEL_ID)
            .setSmallIcon(R.drawable.ic_launcher_foreground)
            .setContentTitle(context.getString(R.string.demo_notification_title))
            .setContentText(context.getString(R.string.demo_notification_body))
            .setContentIntent(openApp)
            .setAutoCancel(true)
            .setPriority(NotificationCompat.PRIORITY_DEFAULT)
            .build()
        NotificationManagerCompat.from(context).notify(NOTIFICATION_ID, notification)
        true
    } catch (_: SecurityException) {
        false
    }
}
