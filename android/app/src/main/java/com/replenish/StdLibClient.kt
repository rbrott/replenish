package com.replenish;

import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.module.kotlin.registerKotlinModule
import retrofit2.Call
import retrofit2.Retrofit
import retrofit2.converter.jackson.JacksonConverterFactory
import retrofit2.http.GET
import retrofit2.http.Query

const val DOMAIN = "https://rbrott.lib.id/"
const val BASE = "replenish@dev/"

interface StdLibClient {

    companion object {
        fun createClient(): StdLibClient {
            val mapper = ObjectMapper().registerKotlinModule()
            val retrofit = Retrofit.Builder()
                .baseUrl(DOMAIN)
                .addConverterFactory(JacksonConverterFactory.create(mapper))
                .build()
            return retrofit.create(StdLibClient::class.java)
        }
    }

    @GET(BASE)
    fun getHydrationInfo(@Query("accessToken") accessToken: String): Call<HydrationInfo>
}