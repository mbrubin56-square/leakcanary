package com.example.leakcanarylovesflipper

import com.facebook.flipper.core.FlipperConnection
import com.facebook.flipper.core.FlipperObject
import com.facebook.flipper.core.FlipperPlugin
import shark.HeapAnalysis
import java.util.*

class LeakCanary2FlipperPlugin : FlipperPlugin {
    private var mConnection: FlipperConnection? = null
    private val leakList: MutableList<String> = ArrayList()

    override fun getId(): String = "LeakCanary2"

    override fun runInBackground(): Boolean = false

    override fun onDisconnect() {
        mConnection = null
    }

    override fun onConnect(connection: FlipperConnection) {
        mConnection = connection
    }

    private fun sendLeakList(heapAnalysis: HeapAnalysis) {
        if (mConnection == null) return

        val lines = heapAnalysis.toString().split('\n')
        lines.forEachIndexed { index, line ->
            mConnection?.send(
                "newRow",
                FlipperObject.Builder()
                    .put("id", index)
                    .put("line", line)
                    .build()
            )
        }
    }

    fun reportLeak(heapAnalysis: HeapAnalysis) =  sendLeakList(heapAnalysis)
}