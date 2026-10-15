package com.example.practice6architecturaldesignpatterns.data.repository

import com.example.practice6architecturaldesignpatterns.data.api.FakeWeatherApi
import kotlinx.coroutines.runBlocking
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class WeatherRepositoryTest {
    private val repository = WeatherRepository(FakeWeatherApi(latencyMs = 0))

    @Test
    fun known_city_returns_success() = runBlocking<Unit> {
        val result = repository.getWeather("Tashkent")
        assertTrue(result.isSuccess)
        assertEquals(24, result.getOrNull()?.temperatureC)
    }

    @Test
    fun unknown_city_returns_failure() = runBlocking<Unit> {
        val result = repository.getWeather("Atlantis")
        assertTrue(result.isFailure)
    }
}
