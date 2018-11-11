package com.hydration;

// TODO
const val BASE_URL = ""

// TODO fill in retrofit stuff once the backend is more finalized
interface StdLibClient {
    companion object {
        fun createClient(): StdLibClient {
//            val retrofit = Retrofit.Builder()
//                .baseUrl(BASE_URL)
//                .addConverterFactory(GsonConverterFactory.create())
//                .build()
//            return retrofit.create(StdLibClient::class.java)
            return object : StdLibClient {
                override fun getDehydrationLevel(): Double {
                    return 5.0
                }

            }
        }
    }

    fun getDehydrationLevel(): Double
}