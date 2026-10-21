package com.example.myweb.platform

import android.app.Application
import android.app.NotificationManager
import android.content.ClipboardManager
import android.content.Context
import android.content.Intent
import androidx.test.core.app.ApplicationProvider
import org.junit.Assert.*
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.Shadows.shadowOf
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [28], qualifiers = "en-rUS-w412dp-h915dp-xhdpi")
class AndroidPortfolioActionsTest {
    private lateinit var application: Application
    private lateinit var actions: AndroidPortfolioActions

    @Before fun setup() {
        application = ApplicationProvider.getApplicationContext()
        actions = AndroidPortfolioActions(application)
    }

    @Test fun deviceSnapshotContainsOnlyExpectedDisplayAndBuildValues() {
        val snapshot = actions.deviceSnapshot()
        assertTrue(snapshot.manufacturer.isNotBlank())
        assertTrue(snapshot.model.isNotBlank())
        assertTrue(snapshot.androidVersion.isNotBlank())
        assertEquals(28, snapshot.sdkLevel)
        assertEquals(412, snapshot.screenWidthDp)
        assertEquals(915, snapshot.screenHeightDp)
        assertTrue(snapshot.densityDpi > 0)
        assertEquals("en-US", snapshot.localeTag)
    }

    @Test fun copyPortfolioLinkUsesPlainTextClipboard() {
        actions.copyPortfolioLink()
        val clipboard = application.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
        assertEquals(AndroidPortfolioActions.PORTFOLIO_URL, clipboard.primaryClip?.getItemAt(0)?.text)
    }

    @Suppress("DEPRECATION")
    @Test fun sharePortfolioStartsChooserWithTextOnlySendIntent() {
        actions.sharePortfolio()
        val chooser = shadowOf(application).nextStartedActivity
        assertEquals(Intent.ACTION_CHOOSER, chooser.action)
        val send = chooser.getParcelableExtra<Intent>(Intent.EXTRA_INTENT)!!
        assertEquals(Intent.ACTION_SEND, send.action)
        assertEquals("text/plain", send.type)
        assertTrue(send.getStringExtra(Intent.EXTRA_TEXT)!!.contains(AndroidPortfolioActions.PORTFOLIO_URL))
    }

    @Test fun githubProfileUsesViewIntentWithHttpsUrl() {
        actions.openGitHubProfile()
        val intent = shadowOf(application).nextStartedActivity
        assertEquals(Intent.ACTION_VIEW, intent.action)
        assertEquals(AndroidPortfolioActions.PORTFOLIO_URL, intent.dataString)
        assertEquals("https", intent.data?.scheme)
    }

    @Test fun demoNotificationCreatesChannelAndPostsNotification() {
        assertTrue(actions.showDemoNotification())
        val manager = application.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        assertNotNull(manager.getNotificationChannel(AndroidPortfolioActions.NOTIFICATION_CHANNEL_ID))
        assertTrue(manager.activeNotifications.any { it.id == AndroidPortfolioActions.NOTIFICATION_ID })
    }
}
