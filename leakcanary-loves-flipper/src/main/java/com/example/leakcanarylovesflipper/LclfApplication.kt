package com.example.leakcanarylovesflipper

import android.app.Application
import com.facebook.flipper.android.AndroidFlipperClient
import com.facebook.flipper.android.utils.FlipperUtils
import com.facebook.flipper.plugins.inspector.DescriptorMapping
import com.facebook.flipper.plugins.inspector.InspectorFlipperPlugin
import com.facebook.soloader.SoLoader
import leakcanary.AppWatcher
import leakcanary.DefaultOnHeapAnalyzedListener
import leakcanary.LeakCanary
import leakcanary.OnHeapAnalyzedListener
import shark.HeapAnalysis

class LclfApplication : Application() {
    override fun onCreate() {
        super.onCreate()
        maybeInstallFipper()
        installLeakCanary()
    }

    private fun maybeInstallFipper() {
        SoLoader.init(this, false)
        if (BuildConfig.DEBUG && FlipperUtils.shouldEnableFlipper(this)) {
            with(AndroidFlipperClient.getInstance(this)) {
                addPlugin(
                    InspectorFlipperPlugin(this@LclfApplication, DescriptorMapping.withDefaults())
                )
                addPlugin(LeakCanary2FlipperPlugin())
                start()
            }
            installLeakCanary()
        }
    }

    private fun installLeakCanary() {
        AppWatcher.config
        LeakCanary.config = LeakCanary.config.copy(
            dumpHeapWhenDebugging = true,
            retainedVisibleThreshold = 1,
            onHeapAnalyzedListener = object : OnHeapAnalyzedListener {
                override fun onHeapAnalyzed(heapAnalysis: HeapAnalysis) {
                    AndroidFlipperClient.getInstance(applicationContext)
                        ?.getPlugin<LeakCanary2FlipperPlugin>("LeakCanary2")
                        ?.reportLeak(heapAnalysis)
                    DefaultOnHeapAnalyzedListener(this@LclfApplication).onHeapAnalyzed(heapAnalysis)
                }
            })
    }

    fun createLeakFor(any: Any) = leakyList.add(any)

    companion object {
        private val leakyList: MutableList<Any> = mutableListOf()
    }
}