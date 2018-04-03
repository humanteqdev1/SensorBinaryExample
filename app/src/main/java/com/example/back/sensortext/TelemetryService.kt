package com.example.back.sensortext

import android.app.IntentService
import android.content.Intent
import com.example.back.sensortext.SensorTelemetry.Companion.SENSOR_TELEMETRY

class TelemetryService : IntentService("TelemetryService") {
    private var sensorTelemetry: SensorTelemetry? = null

    override fun onHandleIntent(intent: Intent) {
        sensorTelemetry = intent.extras.getParcelable(SENSOR_TELEMETRY)

        handler.handle(intent.getParcelableExtra(CONFIG_KEY))
    }

}