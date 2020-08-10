package com.yuwee.iot

import android.os.Bundle
import android.util.Log
import com.google.android.things.pio.Gpio
import com.google.android.things.pio.PeripheralManager
import com.yuwee.sdk.Yuwee
import com.yuwee.sdk.YuweeCallActivity
import com.yuwee.sdk.view.YuweeVideoView
import org.json.JSONObject
import java.util.*

/**
 * Skeleton of an Android Things activity.
 *
 * Android Things peripheral APIs are accessible through the PeripheralManager
 * For example, the snippet below will open a GPIO pin and set it to HIGH:
 *
 * val manager = PeripheralManager.getInstance()
 * val gpio = manager.openGpio("BCM6").apply {
 *     setDirection(Gpio.DIRECTION_OUT_INITIALLY_LOW)
 * }
 * gpio.value = true
 *
 * You can find additional examples on GitHub: https://github.com/androidthings
 */
class CallActivity : YuweeCallActivity() {

    private lateinit var yvRemoteView: YuweeVideoView
    private lateinit var yvLocalView: YuweeVideoView
    private var task: Task? = null
    private var timer: Timer? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_call)
        yvRemoteView = findViewById(R.id.yv_full)
        yvLocalView = findViewById(R.id.yv_small)

        Yuwee.getInstance().callManager.init()

        setUpCallEndListener()
    }

    override fun setRemoteVideoView(): YuweeVideoView {
        return yvRemoteView
    }

    override fun setLocalVideoView(): YuweeVideoView {
        return yvLocalView
    }

    private inner class Task : TimerTask() {
        override fun run() {
            Iot.instance.gpio.value = !Iot.instance.gpio.value
        }
    }

    private fun setUpCallEndListener() {
        Yuwee.getInstance().callManager.setCallEventListener(object : Yuwee.OnCallEventListener {
            override fun onCallTimeOut() {
                Log.e(Iot.TAG, "onCallTimeOut")
            }

            override fun onCallAccept() {
                Log.e(Iot.TAG, "onCallAccept")
            }

            override fun onCallConnectionFailed() {
                Log.e(Iot.TAG, "onCallConnectionFailed")
            }

            override fun onCallReject() {
                Log.e(Iot.TAG, "onCallReject")
            }

            override fun onCallReconnected() {
                Log.e(Iot.TAG, "onCallReconnected")
            }

            override fun onCallEnd(p0: JSONObject?) {
                Log.e(Iot.TAG, "onCallEnd")
                finish()
            }

            override fun onCallReconnectionFailed() {
                Log.e(Iot.TAG, "onCallReconnectionFailed")
            }

            override fun onCallConnected() {
                Log.e(Iot.TAG, "onCallConnected")

                cancelTimer()

                task = Task()
                timer = Timer()
                timer?.scheduleAtFixedRate(task, 0, 300)
            }

            override fun onRemoteCallHangUp(p0: JSONObject?) {
                Log.e(Iot.TAG, "onRemoteCallHangUp")
            }

            override fun onCallReconnecting() {
                Log.e(Iot.TAG, "onCallReconnecting")
            }

            override fun onCallDisconnected() {
                Log.e(Iot.TAG, "onCallDisconnected")
            }

        })
    }

    private fun cancelTimer(){
        task?.cancel()
        timer?.cancel()
        task = null
        timer = null
    }

    override fun onDestroy() {
        super.onDestroy()
        cancelTimer()
        Iot.instance.gpio.value = true
    }
}