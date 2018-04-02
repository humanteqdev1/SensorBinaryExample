package com.example.back.sensortext

import android.content.Context
import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import android.hardware.SensorManager
import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import android.util.Log

class MainActivity : AppCompatActivity(), SensorEventListener {
    private val bitmask7: Long = 0b01111111_00000000_00000000_00000000_00000000_00000000_00000000_00000000
    private val bitmask6: Long = 0b00000000_11111111_00000000_00000000_00000000_00000000_00000000_00000000
    private val bitmask5: Long = 0b00000000_00000000_11111111_00000000_00000000_00000000_00000000_00000000
    private val bitmask4: Long = 0b00000000_00000000_00000000_11111111_00000000_00000000_00000000_00000000
    private val bitmask3: Long = 0b00000000_00000000_00000000_00000000_11111111_00000000_00000000_00000000
    private val bitmask2: Long = 0b00000000_00000000_00000000_00000000_00000000_11111111_00000000_00000000
    private val bitmask1: Long = 0b00000000_00000000_00000000_00000000_00000000_00000000_11111111_00000000
    private val bitmask0: Long = 0b00000000_00000000_00000000_00000000_00000000_00000000_00000000_11111111

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        sensorManager = getSystemService(Context.SENSOR_SERVICE) as SensorManager
        sensorManager.getDefaultSensor(Sensor.TYPE_GYROSCOPE)?.let {
            sensorManager.registerListener(this, it, 1000000)
        }
    }

    private lateinit var sensorManager: SensorManager

    override fun onAccuracyChanged(sensor: Sensor?, accuracy: Int) {
    }

    override fun onSensorChanged(event: SensorEvent?) {
        event?.let {
            val size = it.values.size

            val builder = StringBuilder(256)
            for (i in 0 until size) {
                val value = it.values[i].toBits().toLong()
                Log.e("-- TEST", "\n${it.values[i]} = $value\n")

                val b3 = (value and bitmask3) shr (8 * 3)
                val b2 = (value and bitmask2) shr (8 * 2)
                val b1 = (value and bitmask1) shr (8 * 1)
                val b0 = (value and bitmask0)

                builder.append("${b3.toChar()}${b2.toChar()}${b1.toChar()}${b0.toChar()}")
            }

            val b7 = (it.timestamp and bitmask7) shr (8 * 7)
            val b6 = (it.timestamp and bitmask6) shr (8 * 6)
            val b5 = (it.timestamp and bitmask5) shr (8 * 5)
            val b4 = (it.timestamp and bitmask4) shr (8 * 4)
            val b3 = (it.timestamp and bitmask3) shr (8 * 3)
            val b2 = (it.timestamp and bitmask2) shr (8 * 2)
            val b1 = (it.timestamp and bitmask1) shr (8 * 1)
            val b0 = (it.timestamp and bitmask0)
            builder.append("${b7.toChar()}${b6.toChar()}${b5.toChar()}${b4.toChar()}" +
                    "${b3.toChar()}${b2.toChar()}${b1.toChar()}${b0.toChar()}")

            decodeBytes(builder.toString())
            sensorManager.unregisterListener(this)
        }
    }

    private fun getBits(str: String, offset: Int): Int {
        val b3 = str[0 + offset].toInt() shl (8 * 3)
        val b2 = str[1 + offset].toInt() shl (8 * 2)
        val b1 = str[2 + offset].toInt() shl (8 * 1)
        val b0 = str[3 + offset].toInt()

        return 0 or b3 or b2 or b1 or b0
    }

    private fun getLongBits(str: String, offset: Int): Long {
        val b19 = str[0 + offset].toLong() shl (8 * 7)
        val b18 = str[1 + offset].toLong() shl (8 * 6)
        val b17 = str[2 + offset].toLong() shl (8 * 5)
        val b16 = str[3 + offset].toLong() shl (8 * 4)
        val b15 = str[4 + offset].toLong() shl (8 * 3)
        val b14 = str[5 + offset].toLong() shl (8 * 2)
        val b13 = str[6 + offset].toLong() shl (8 * 1)
        val b12 = str[7 + offset].toLong()

        return 0L or b19 or b18 or b17 or b16 or b15 or b14 or b13 or b12
    }

    private fun decodeBytes(str: String) {
        val restoredX = Float.fromBits(getBits(str, 0))
        val restoredY = Float.fromBits(getBits(str, 4))
        val restoredZ = Float.fromBits(getBits(str, 8))
        val restoredTimestamp = getLongBits(str, 12)

        Log.e("--- TEST", "$restoredX $restoredY $restoredZ $restoredTimestamp")
    }
}