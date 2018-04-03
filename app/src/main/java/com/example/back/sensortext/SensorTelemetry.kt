package com.example.back.sensortext

import android.content.Context
import android.content.Intent
import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import android.hardware.SensorManager
import android.os.Parcel
import android.os.Parcelable
import java.io.Serializable

class SensorTelemetry(private val context: Context) : SensorEventListener, Serializable, Parcelable {
    companion object {
        const val SENSOR_TELEMETRY = "SENSOR_TELEMETRY"
    }

    private val bitmask7: Long = 0b01111111_00000000_00000000_00000000_00000000_00000000_00000000_00000000
    private val bitmask6: Long = 0b00000000_11111111_00000000_00000000_00000000_00000000_00000000_00000000
    private val bitmask5: Long = 0b00000000_00000000_11111111_00000000_00000000_00000000_00000000_00000000
    private val bitmask4: Long = 0b00000000_00000000_00000000_11111111_00000000_00000000_00000000_00000000
    private val bitmask3: Long = 0b00000000_00000000_00000000_00000000_11111111_00000000_00000000_00000000
    private val bitmask2: Long = 0b00000000_00000000_00000000_00000000_00000000_11111111_00000000_00000000
    private val bitmask1: Long = 0b00000000_00000000_00000000_00000000_00000000_00000000_11111111_00000000
    private val bitmask0: Long = 0b00000000_00000000_00000000_00000000_00000000_00000000_00000000_11111111

    private lateinit var sensorManager: SensorManager
    private val buffer = ArrayList<Byte>(8192)
    private var latestTimestamp = System.currentTimeMillis()

    constructor(parcel: Parcel) : this(context) {
        latestTimestamp = parcel.readLong()
    }

    override fun onAccuracyChanged(sensor: Sensor?, p1: Int) {}

    override fun onSensorChanged(event: SensorEvent?) {
        event?.let {
            if (it.values.size == 3) {
                //Adding x,y,z data
                for (i in 0 until it.values.size) {
                    val bitValue = it.values[i].toBits()
                    buffer.addAll(bitValue.toByteArray())
                }

                //Adding timestamp delta
                buffer.addAll((it.timestamp - latestTimestamp).to8BitByteArray())
                latestTimestamp = it.timestamp
            }
        }
    }

    fun start(id: String, type: Int) {
        val intent = Intent(context, TelemetryService::class.java)
        intent.putExtra(SENSOR_TELEMETRY, this)
        context.startService(intent)


        buildHeader(id)

        sensorManager = context.getSystemService(Context.SENSOR_SERVICE) as SensorManager
        sensorManager.getDefaultSensor(type)?.let {
            sensorManager.registerListener(this, it, 1000000)
        }
    }

    private fun buildHeader(id: String) {
        //Put session id
        id.forEach { buffer.add(it.toByte()) }

        //Put initial latestTimestamp
        latestTimestamp = System.currentTimeMillis()
        buffer.addAll(latestTimestamp.toByteArray())
    }

    fun stop() {
        sensorManager.unregisterListener(this)
    }

    private fun Long.toByteArray(): List<Byte> {
        val b7 = (this and bitmask7) shr (8 * 7)
        val b6 = (this and bitmask6) shr (8 * 6)
        val b5 = (this and bitmask5) shr (8 * 5)
        val b4 = (this and bitmask4) shr (8 * 4)
        val b3 = (this and bitmask3) shr (8 * 3)
        val b2 = (this and bitmask2) shr (8 * 2)
        val b1 = (this and bitmask1) shr (8 * 1)
        val b0 = (this and bitmask0)

        return arrayListOf(b7.toByte(), b6.toByte(), b5.toByte(), b4.toByte(),
                b3.toByte(), b2.toByte(), b1.toByte(), b0.toByte())
    }

    private fun Int.toByteArray(): Collection<Byte> {
        val b3 = (this and bitmask3.toInt()) shr (8 * 3)
        val b2 = (this and bitmask2.toInt()) shr (8 * 2)
        val b1 = (this and bitmask1.toInt()) shr (8 * 1)
        val b0 = (this and bitmask0.toInt())

        return arrayListOf(b3.toByte(), b2.toByte(), b1.toByte(), b0.toByte())
    }

    private fun Long.to8BitByteArray(): Collection<Byte> {
        val b1 = (this and bitmask1) shr (8 * 1)
        val b0 = (this and bitmask0)

        return arrayListOf(b1.toByte(), b0.toByte())
    }

    override fun writeToParcel(parcel: Parcel, flags: Int) {
        parcel.writeLong(latestTimestamp)
    }

    override fun describeContents(): Int {
        return 0
    }

    companion object CREATOR : Parcelable.Creator<SensorTelemetry> {
        override fun createFromParcel(parcel: Parcel): SensorTelemetry {
            return SensorTelemetry(parcel)
        }

        override fun newArray(size: Int): Array<SensorTelemetry?> {
            return arrayOfNulls(size)
        }
    }
}
