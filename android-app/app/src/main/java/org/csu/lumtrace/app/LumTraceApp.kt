package org.csu.lumtrace.app

import android.app.Application
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch
import org.csu.lumtrace.app.data.ApiClient

class LumTraceApp : Application() {
    override fun onCreate() {
        super.onCreate()
        ApiClient.init(this)
        CoroutineScope(SupervisorJob() + Dispatchers.IO).launch {
            ApiClient.tokenStore.restore()
        }
    }
}