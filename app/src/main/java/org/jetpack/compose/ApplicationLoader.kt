package org.jetpack.compose

import android.annotation.SuppressLint
import android.app.Application
import android.content.Context
import android.content.Intent
import android.content.res.Configuration
import android.net.ConnectivityManager
import android.net.ConnectivityManager.NetworkCallback
import android.net.NetworkInfo
import android.os.Handler
import android.os.Process
import android.telephony.TelephonyManager
import android.util.Log
import java.io.File
import java.io.PrintWriter
import java.io.StringWriter
import java.io.Writer
import java.lang.Exception
import java.time.LocalDate
import kotlin.jvm.Volatile
import kotlin.system.exitProcess

class ApplicationLoader : Application() {
    private var uncaughtExceptionHandler: Thread.UncaughtExceptionHandler? = null

    override fun attachBaseContext(base: Context?) {
        super.attachBaseContext(base)
    }

    override fun onCreate() {
        applicationLoaderInstance = this
        try {
            Companion.applicationContext = this.applicationContext
        } catch (ignore: Throwable) {
        }
        uncaughtExceptionHandler = Thread.getDefaultUncaughtExceptionHandler()
        Thread.setDefaultUncaughtExceptionHandler(
            object : Thread.UncaughtExceptionHandler {
                override fun uncaughtException(thread: Thread?, throwable: Throwable?) {
                    val intent = Intent(applicationContext, DebugActivity::class.java)
                    intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK)
                    intent.putExtra("error", Log.getStackTraceString(throwable))
                    startActivity(intent)
                    Process.killProcess(Process.myPid())
                    exitProcess(1)
                }
            })
        super.onCreate()

        if (Companion.applicationContext == null) {
            Companion.applicationContext = this.applicationContext
        }

        applicationHandler = Handler(Companion.applicationContext!!.mainLooper)
    }

    private fun getStackTrace(th: Throwable?): String {
        val result: Writer = StringWriter()

        val printWriter = PrintWriter(result)
        var cause = th

        while (cause != null) {
            cause.printStackTrace(printWriter)
            cause = cause.cause
        }
        val stacktraceAsString = result.toString()
        printWriter.close()

        return stacktraceAsString
    }

    override fun onConfigurationChanged(newConfig: Configuration) {
        super.onConfigurationChanged(newConfig)
    }

    companion object {
        private var applicationLoaderInstance: ApplicationLoader? = null

        @SuppressLint("StaticFieldLeak")
        @Volatile
        var applicationContext: Context? = null

        @Volatile
        var currentNetworkInfo: NetworkInfo? = null

        @Volatile
        var applicationHandler: Handler? = null

        private val connectivityManager: ConnectivityManager? = null

        @Volatile
        private var applicationInited = false

        @Volatile
        private var networkCallback: NetworkCallback? = null
        private val lastNetworkCheckTypeTime: Long = 0
        private val lastKnownNetworkType = -1

        var startTime: Long = 0

        @Volatile
        var isScreenOn: Boolean = false

        @Volatile
        var mainInterfacePaused: Boolean = true

        @Volatile
        var mainInterfaceStopped: Boolean = true

        @Volatile
        var externalInterfacePaused: Boolean = true

        @Volatile
        var mainInterfacePausedStageQueue: Boolean = true
        var canDrawOverlays: Boolean = false

        @Volatile
        var mainInterfacePausedStageQueueTime: Long = 0

        fun getFilesDirFixed(): File {
            for (a in 0..9) {
                val path = applicationContext!!.getFilesDir()
                if (path != null) {
                    return path
                }
            }
            try {
                val info = applicationContext!!.getApplicationInfo()
                val path = File(info.dataDir, "files")
                path.mkdirs()
                return path
            } catch (e: Exception) {
                //FileLog.e(e);
            }
            return File("/data/data/org.anime.project/files")
        }

        fun postInitApplication() {
            if (applicationInited || applicationContext == null) {
                return
            }
            applicationInited = true
        }

        fun isExpired(): Boolean {
            return isExpired(13, 12, 2024)
        }

        fun isExpired(day: Int, month: Int, year: Int): Boolean {
            require(isValidDate(day, month, year)) { "Invalid date provided." }
            val currentDate = LocalDate.now()
            val expirationDate = LocalDate.of(year, month, day)
            return currentDate.isAfter(expirationDate)
        }

        private fun isValidDate(day: Int, month: Int, year: Int): Boolean {
            try {
                LocalDate.of(year, month, day)
                return true
            } catch (e: Exception) {
                return false
            }
        }

        fun startPushService() {
        }

        private fun ensureCurrentNetworkGet(force: Boolean) {
        }

        fun isVPN(): Boolean {
            try {
                val cm =
                    applicationContext!!.getSystemService(CONNECTIVITY_SERVICE) as ConnectivityManager
                val activeNetworkInfo = cm.getActiveNetworkInfo()
                if (activeNetworkInfo != null && activeNetworkInfo.isConnected()) {
                } else {
                    return true
                }
                return cm.getNetworkInfo(ConnectivityManager.TYPE_VPN)!!.isConnectedOrConnecting()
            } catch (e: Exception) {
                return false
            }
        }

        fun isRoaming(): Boolean {
            try {
                ensureCurrentNetworkGet(false)
                return currentNetworkInfo != null && currentNetworkInfo!!.isRoaming()
            } catch (e: Exception) {
                //FileLog.e(e);
            }
            return false
        }

        fun isConnectedOrConnectingToWiFi(): Boolean {
            try {
                ensureCurrentNetworkGet(false)
                if (currentNetworkInfo != null && (currentNetworkInfo!!.getType() == ConnectivityManager.TYPE_WIFI || currentNetworkInfo!!.getType() == ConnectivityManager.TYPE_ETHERNET)) {
                    val state = currentNetworkInfo!!.getState()
                    if (state == NetworkInfo.State.CONNECTED || state == NetworkInfo.State.CONNECTING || state == NetworkInfo.State.SUSPENDED) {
                        return true
                    }
                }
            } catch (e: Exception) {
                //FileLog.e(e);
            }
            return false
        }

        fun isConnectedToWiFi(): Boolean {
            try {
                ensureCurrentNetworkGet(false)
                if (currentNetworkInfo != null && (currentNetworkInfo!!.getType() == ConnectivityManager.TYPE_WIFI || currentNetworkInfo!!.getType() == ConnectivityManager.TYPE_ETHERNET) && currentNetworkInfo!!.getState() == NetworkInfo.State.CONNECTED) {
                    return true
                }
            } catch (e: Exception) {
                //FileLog.e(e);
            }
            return false
        }

        fun isConnectionSlow(): Boolean {
            try {
                ensureCurrentNetworkGet(false)
                if (currentNetworkInfo != null && currentNetworkInfo!!.getType() == ConnectivityManager.TYPE_MOBILE) {
                    when (currentNetworkInfo!!.getSubtype()) {
                        TelephonyManager.NETWORK_TYPE_1xRTT, TelephonyManager.NETWORK_TYPE_CDMA, TelephonyManager.NETWORK_TYPE_EDGE, TelephonyManager.NETWORK_TYPE_GPRS, TelephonyManager.NETWORK_TYPE_IDEN -> return true
                    }
                }
            } catch (ignore: Throwable) {
            }
            return false
        }

        fun isNetworkOnlineFast(): Boolean {
            try {
                ensureCurrentNetworkGet(false)
                if (currentNetworkInfo == null) {
                    return true
                }
                if (currentNetworkInfo!!.isConnectedOrConnecting() || currentNetworkInfo!!.isAvailable()) {
                    return true
                }

                var netInfo = connectivityManager!!.getNetworkInfo(ConnectivityManager.TYPE_MOBILE)
                if (netInfo != null && netInfo.isConnectedOrConnecting()) {
                    return true
                } else {
                    netInfo = connectivityManager.getNetworkInfo(ConnectivityManager.TYPE_WIFI)
                    if (netInfo != null && netInfo.isConnectedOrConnecting()) {
                        return true
                    }
                }
            } catch (e: Exception) {
                //FileLog.e(e);
                return true
            }
            return false
        }

        fun isNetworkOnlineRealtime(): Boolean {
            try {
                val connectivityManager =
                    applicationContext!!.getSystemService(CONNECTIVITY_SERVICE) as ConnectivityManager
                var netInfo = connectivityManager.getActiveNetworkInfo()
                if (netInfo != null && (netInfo.isConnectedOrConnecting() || netInfo.isAvailable())) {
                    return true
                }

                netInfo = connectivityManager.getNetworkInfo(ConnectivityManager.TYPE_MOBILE)

                if (netInfo != null && netInfo.isConnectedOrConnecting()) {
                    return true
                } else {
                    netInfo = connectivityManager.getNetworkInfo(ConnectivityManager.TYPE_WIFI)
                    if (netInfo != null && netInfo.isConnectedOrConnecting()) {
                        return true
                    }
                }
            } catch (e: Exception) {
                //FileLog.e(e);
                return true
            }
            return false
        }
    }
}