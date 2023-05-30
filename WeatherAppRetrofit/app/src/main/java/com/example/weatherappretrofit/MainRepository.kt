package com.example.weatherappretrofit

import com.example.weatherappretrofit.api.NetworkState
import com.example.weatherappretrofit.api.RetrofitService
import com.example.weatherappretrofit.data.APIResponse

class MainRepository constructor(private val retrofitService: RetrofitService) {

    suspend fun getWeather(searchWeather: String): NetworkState<APIResponse> {
        val response = retrofitService.getWeather(searchWeather)
        return if (response.isSuccessful) {
            val responseBody = response.body()
            if (responseBody != null) {
                NetworkState.Success(responseBody)
            } else {
                NetworkState.Error(response)
            }
        } else {
            NetworkState.Error(response)
        }
    }

}