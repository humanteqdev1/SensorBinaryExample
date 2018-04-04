package com.example.back.sensortext

import android.hardware.Sensor
import android.os.Bundle
import android.support.v7.app.AppCompatActivity

class MainActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

//        SensorTelemetry.start(this, "some-id", arrayListOf(Sensor.TYPE_GYROSCOPE, Sensor.TYPE_ACCELEROMETER))
        SensorTelemetry.start(this, "some-id", Sensor.TYPE_GYROSCOPE)
    }
}