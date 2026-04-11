package uk.ac.tees.mad.F5250116.data

import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import retrofit2.http.GET
import retrofit2.http.Query

data class ExchangeRatesResponse(
    val amount: Double,
    val base: String,
    val date: String,
    val rates: Map<String, Double>
)

interface ExchangeRateService {
    @GET("latest")
    suspend fun getLatestRates(
        @Query("from") from: String,
        @Query("to") to: String
    ): ExchangeRatesResponse
}

object ExchangeRateApi {
    val service: ExchangeRateService by lazy {
        Retrofit.Builder()
            .baseUrl("https://api.frankfurter.app/")
            .addConverterFactory(GsonConverterFactory.create())
            .build()
            .create(ExchangeRateService::class.java)
    }
}
