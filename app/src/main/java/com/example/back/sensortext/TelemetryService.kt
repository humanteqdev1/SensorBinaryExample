package com.example.back.sensortext

import android.app.IntentService
import android.content.Context
import android.content.Intent
import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import android.hardware.SensorManager
import com.example.back.sensortext.SensorTelemetry.Companion.SENSOR_TELEMETRY_SESSION_ID
import com.example.back.sensortext.SensorTelemetry.Companion.SENSOR_TELEMETRY_TYPE
import java.nio.ByteBuffer
import java.nio.charset.Charset

class TelemetryService : IntentService("TelemetryService") {
    override fun onHandleIntent(intent: Intent) {
        val bundle = intent.extras
        bundle?.let {
            val sessionId = intent.extras.getString(SENSOR_TELEMETRY_SESSION_ID)
            val type = intent.extras.getInt(SENSOR_TELEMETRY_TYPE)

            if (sessionId != null)
                SensorTelemetry(this).start(sessionId, type, 1000)
        }
    }
}

class SensorTelemetry(private val context: Context) : SensorEventListener {
    companion object {
        const val SENSOR_TELEMETRY_SESSION_ID = "SENSOR_TELEMETRY_SESSION_ID"
        const val SENSOR_TELEMETRY_TYPE = "SENSOR_TELEMETRY_TYPE"
        //        const val MB_1 = 1 * 1024 * 1024
        const val MB_1 = 64
        private const val PACK_SIZE = 14
        const val limit = MB_1 - PACK_SIZE

        fun start(context: Context, id: String, typeList: List<Int>) {
            val intent = Intent(context, TelemetryService::class.java)
            intent.putExtra(SENSOR_TELEMETRY_SESSION_ID, id)
            typeList.forEach {
                intent.putExtra(SENSOR_TELEMETRY_TYPE, it)
                context.startService(intent)
            }
        }

        fun start(context: Context, id: String, type: Int) {
            val intent = Intent(context, TelemetryService::class.java)
            intent.putExtra(SENSOR_TELEMETRY_SESSION_ID, id)
            intent.putExtra(SENSOR_TELEMETRY_TYPE, type)
            context.startService(intent)
        }
    }

    private lateinit var sensorManager: SensorManager
    private var latestTimestamp = System.currentTimeMillis()
    private val byteBuffer = ByteBuffer.allocate(MB_1)
    private var id = ""

    fun start(id: String, type: Int, rate: Int) {
        this.id = id
        buildHeader(id)

        sensorManager = context.getSystemService(Context.SENSOR_SERVICE) as SensorManager
        sensorManager.getDefaultSensor(type)?.let {
            sensorManager.registerListener(this, it, rate * 1000)
        }
    }

    override fun onAccuracyChanged(sensor: Sensor?, p1: Int) {}

    override fun onSensorChanged(event: SensorEvent?) {
        if (byteBuffer.position() > limit) {
            sendData()
        }

        event?.let {
            if (it.values.size == 3) {
                //Adding x,y,z data
                for (i in 0 until it.values.size) {
//                    Log.e("--- VALUE:", "${it.values[i]}")
                    byteBuffer.putFloat(it.values[i])
                }

                val now = System.currentTimeMillis()
                val delta = (now - latestTimestamp) / 1000
//                Log.e("--- Delta:", "$now - $latestTimestamp = $delta")
                latestTimestamp = now
                //Adding timestamp delta
                byteBuffer.putShort(delta.toShort())
            }
        }

//        parseString()
    }

//    var offset = 0
//    private fun parseString() {
//        val builder = StringBuilder(128)
//
//        builder.append("\n\nDECODING\nHeader: ${byteBuffer.getHeader(0..6)}\n")
//        builder.append("Timestamp: ${byteBuffer.getTimestamp(7)}\n")
//        builder.append("X: ${byteBuffer.getValue(15 + offset)}\n")
//        builder.append("Y: ${byteBuffer.getValue(19 + offset)}\n")
//        builder.append("Z: ${byteBuffer.getValue(23 + offset)}\n")
//        builder.append("Delta: ${byteBuffer.getShortValue(27 + offset)}\n")
//        offset+=14
//
//        Log.e("--- decoded:", "$builder")
//    }

    private fun buildHeader(id: String) {
        byteBuffer.clear()

        //Put session id
        byteBuffer.put(id.toByteArray())

        //Put initial latestTimestamp
        latestTimestamp = System.currentTimeMillis()
        byteBuffer.putLong(latestTimestamp)
    }

    fun stop() {
        sensorManager.unregisterListener(this)
        sendData()
    }

    fun sendData() {
        val tempByteBuffer = byteBuffer.array().sliceArray(0..byteBuffer.position())
        buildHeader(id)
        //Send tempByteBuffer
    }

    private fun ByteBuffer.getHeader(range: IntRange): String {
        return String(this.array().sliceArray(range), Charset.defaultCharset())
    }

    private fun ByteBuffer.getTimestamp(start: Int): String {
        return this.getLong(start).toString()
    }

    private fun ByteBuffer.getValue(start: Int): String {
        return this.getFloat(start).toString()
    }

    private fun ByteBuffer.getShortValue(start: Int): String {
        return this.getShort(start).toString()
    }
}