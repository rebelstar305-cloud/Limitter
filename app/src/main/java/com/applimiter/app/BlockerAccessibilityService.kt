package com.applimiter.app

import android.accessibilityservice.AccessibilityService
import android.content.SharedPreferences
import android.os.Handler
import android.os.Looper
import android.view.accessibility.AccessibilityEvent
import android.widget.Toast
import java.util.Calendar

class BlockerAccessibilityService : AccessibilityService() {

    private val targetPackage = "com.instagram.android"
    private lateinit var prefs: SharedPreferences
    private var isTargetForeground = false
    private val handler = Handler(Looper.getMainLooper())

    private val tickRunnable = object : Runnable {
        override fun run() {
            if (isTargetForeground) {
                checkAndTick()
            }
            handler.postDelayed(this, 1000)
        }
    }

    override fun onServiceConnected() {
        prefs = getSharedPreferences("applimiter_prefs", MODE_PRIVATE)
        handler.post(tickRunnable)
    }

    override fun onAccessibilityEvent(event: AccessibilityEvent?) {
        val pkg = event?.packageName?.toString() ?: return
        if (event.eventType == AccessibilityEvent.TYPE_WINDOW_STATE_CHANGED) {
            isTargetForeground = (pkg == targetPackage)
            if (isTargetForeground) {
                checkLimitAndMaybeBlock()
            }
        }
    }

    private fun currentHourBucket(): Int {
        val cal = Calendar.getInstance()
        return cal.get(Calendar.YEAR) * 1000000 + cal.get(Calendar.DAY_OF_YEAR) * 100 + cal.get(Calendar.HOUR_OF_DAY)
    }

    private fun resetIfNewHour() {
        val savedBucket = prefs.getInt("hourBucket", -1)
        val nowBucket = currentHourBucket()
        if (savedBucket != nowBucket) {
            prefs.edit()
                .putInt("hourBucket", nowBucket)
                .putInt("usedSeconds", 0)
                .apply()
        }
    }

    private fun checkAndTick() {
        resetIfNewHour()
        val used = prefs.getInt("usedSeconds", 0) + 1
        prefs.edit().putInt("usedSeconds", used).apply()
        checkLimitAndMaybeBlock()
    }

    private fun checkLimitAndMaybeBlock() {
        resetIfNewHour()
        val limitMinutes = prefs.getInt("limitMinutes", 10)
        val usedSeconds = prefs.getInt("usedSeconds", 0)
        if (usedSeconds >= limitMinutes * 60) {
            performGlobalAction(GLOBAL_ACTION_HOME)
            Toast.makeText(this, "Time limit reached. Try again next hour.", Toast.LENGTH_LONG).show()
        }
    }

    override fun onInterrupt() {}
}
