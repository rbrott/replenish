package com.replenish;

import android.content.Context
import android.util.Log
import com.fitbit.authentication.AuthenticationManager
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import kotlin.math.max

const val PREFS_NAME = "WaterMonitor"
const val PREF_WATER_CONSUMED = "WATER_CONSUMED"
const val PREF_WATER_NOTIFIED = "WATER_NOTIFIED"

class WaterMonitor(context: Context) {
    private val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
    private val accessToken  = AuthenticationManager.getCurrentAccessToken().accessToken
    private val stdLibClient = StdLibClient.createClient()
    private lateinit var hydrationInfo: HydrationInfo

    fun update(callback: () -> Unit) {
        Log.i("Replenish", accessToken)
        stdLibClient.getHydrationInfo(accessToken)
            .enqueue(object : Callback<HydrationInfo> {
                override fun onFailure(call: Call<HydrationInfo>, t: Throwable) {

                }

                override fun onResponse(call: Call<HydrationInfo>, response: Response<HydrationInfo>) {
                    hydrationInfo = response.body()!!
                    callback()
                }

            })
    }

    fun shouldNotify(): Boolean {
        return getWaterNeeded() - getWaterNotified() > 8.0f
    }

    fun getWaterConsumed() = prefs.getFloat(PREF_WATER_CONSUMED, 0.0f)

    private fun getWaterNotified() = prefs.getFloat(PREF_WATER_NOTIFIED, 0.0f)

    fun getWaterNeeded() = max(0.0f, hydrationInfo.ounces - getWaterConsumed())

    fun recordWaterConsumption(ounces: Float) {
        prefs.edit()
            .putFloat(PREF_WATER_CONSUMED, getWaterConsumed() + ounces)
            .putFloat(PREF_WATER_NOTIFIED, getWaterNotified() - ounces)
            .apply()
    }

    fun recordWaterNotified(ounces: Float) {
        prefs.edit()
            .putFloat(PREF_WATER_NOTIFIED, getWaterNotified() + ounces)
            .apply()
    }

    fun clear() {
        prefs.edit()
            .putFloat(PREF_WATER_NOTIFIED, 0.0f)
            .putFloat(PREF_WATER_CONSUMED, 0.0f)
            .apply()
    }

}
