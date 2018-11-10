package com.hydration;

import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

// TODO fill in retrofit stuff once the backend is more finalized
interface StdLibClient {
    companion object {
        val BASE_URL: String = TODO("fill in")

        fun createClient(): StdLibClient {
            val retrofit = Retrofit.Builder()
                .baseUrl(BASE_URL)
                .addConverterFactory(GsonConverterFactory.create())
                .build()
            return retrofit.create(StdLibClient:: class.java)
        }
    }
}