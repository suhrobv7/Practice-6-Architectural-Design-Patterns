package com.example.practice6architecturaldesignpatterns.mvp

import com.example.practice6architecturaldesignpatterns.data.api.FakeWeatherApi
import com.example.practice6architecturaldesignpatterns.data.model.Weather
import com.example.practice6architecturaldesignpatterns.data.repository.WeatherRepository
import com.example.practice6architecturaldesignpatterns.mvp.presenter.WeatherPresenter
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import org.junit.Assert.assertEquals
import org.junit.Test

class FakeView : WeatherContract.View {
    val events = mutableListOf<String>()

    override fun showLoading() { events += "loading" }
    override fun showWeather(weather: Weather) { events += "weather:${weather.city}" }
    override fun showError(message: String) { events += "error:$message" }
}

class WeatherPresenterTest {
    private val view = FakeView()
    private val presenter = WeatherPresenter(
        viewRef = view,
        repo = WeatherRepository(FakeWeatherApi(latencyMs = 0)),
        jobScope = CoroutineScope(Dispatchers.Unconfined),
    )

    @Test
    fun known_city_shows_loading_then_weather() {
        presenter.selectCity("Tashkent")
        assertEquals(listOf("loading", "weather:Tashkent"), view.events)
    }

    @Test
    fun unknown_city_shows_loading_then_error() {
        presenter.selectCity("Atlantis")
        assertEquals(listOf("loading", "error:No forecast for Atlantis"), view.events)
    }

    @Test
    fun two_requests_in_a_row_show_two_loading_events() {
        presenter.selectCity("Tashkent")
        presenter.selectCity("Samarkand")
        assertEquals(listOf("loading", "weather:Tashkent", "loading", "weather:Samarkand"), view.events)
    }
}
