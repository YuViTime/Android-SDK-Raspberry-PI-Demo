package com.yuwee.iot

import android.app.Application
import android.content.Intent
import android.os.Handler
import android.os.Looper
import android.util.Log
import androidx.multidex.MultiDex
import com.google.android.things.pio.Gpio
import com.google.android.things.pio.PeripheralManager
import com.yuwee.sdk.Yuwee
import com.yuwee.sdk.model.user_login.UserLoginResponse
import org.json.JSONObject

class Iot : Application() {

    lateinit var gpio : Gpio

    override fun onCreate() {
        super.onCreate()

        MultiDex.install(this)
        instance = this
        Yuwee.getInstance().init(this, appId, appSecret, clientId)

        val manager = PeripheralManager.getInstance()
        gpio = manager.openGpio("BCM6").apply { setDirection(Gpio.DIRECTION_OUT_INITIALLY_LOW)}

        if (!Yuwee.getInstance().userManager.isLoggedIn) {
            Handler(Looper.getMainLooper()).postDelayed({
                login()
            }, 4000)
        } else {
            Log.e(TAG, "Already logged in")
            setupListener()
        }
    }


    private fun login() {
        Log.e(TAG, "Logging in...")
        Yuwee.getInstance().userManager.createSessionViaCredentials(
            "tanayyuwee+1@gmail.com",
            "123456",
            "999999",
            object : Yuwee.OnCreateSessionListener {
                override fun onSessionCreateSuccess(p0: UserLoginResponse?) {
                    Log.e(TAG, "Login Success")
                    setupListener()
                }

                override fun onSessionCreateFailure(p0: UserLoginResponse?) {
                    Log.e(TAG, "Login Failed")
                }

            }
        )
    }

    private fun setupListener() {
        gpio.value = true
        Yuwee.getInstance().callManager.setOnIncomingCallEventListener(object :
            Yuwee.OnIncomingCallEventListener {
            override fun onIncomingCallAcceptSuccess() {
                val intent = Intent(instance, CallActivity::class.java)
                intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK
                instance.startActivity(intent)
            }

            override fun onIncomingCall(p0: JSONObject?) {
                Handler(Looper.getMainLooper()).postDelayed({
                    Yuwee.getInstance().callManager.acceptIncomingCall()
                }, 1000)
            }

            override fun onIncomingCallRejectSuccess() {

            }

        })
    }

    companion object {
        const val appId = "APP ID HERE"
        const val appSecret = "APP SECRET HERE"
        const val clientId = "CLIENT ID HERE"

        const val TAG = "TAG"

        lateinit var instance: Iot
    }
}