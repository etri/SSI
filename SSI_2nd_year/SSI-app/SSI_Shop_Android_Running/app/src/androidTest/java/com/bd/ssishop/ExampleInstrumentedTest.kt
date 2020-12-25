package com.bd.ssishop

import android.util.Log
import androidx.test.platform.app.InstrumentationRegistry
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.bd.ssishop.api.SsiApi
import kotlinx.coroutines.runBlocking

import org.junit.Test
import org.junit.runner.RunWith

import org.junit.Assert.*
import retrofit2.Retrofit

/**
 * Instrumented test, which will execute on an Android device.
 *
 * See [testing documentation](http://d.android.com/tools/testing).
 */
@RunWith(AndroidJUnit4::class)
class ExampleInstrumentedTest {
    @Test
    fun useAppContext() {
        // Context of the app under test.
        val appContext = InstrumentationRegistry.getInstrumentation().targetContext
        assertEquals("com.bd.ssishop", appContext.packageName)

        var api = Retrofit.Builder()
            .baseUrl("http://10.0.2.2:8080")
//            .baseUrl("http://129.254.194.112:8080")
            .build()
            .create(SsiApi::class.java)


//        runBlocking {
//            val response = SsiApi.instance.test()
//            Log.d("test", response.data)
//        }

    }
}