package com.example.practice6architecturaldesignpatterns.mvi

import com.example.practice6architecturaldesignpatterns.data.api.FakeWeatherApi
import com.example.practice6architecturaldesignpatterns.data.repository.WeatherRepository
import com.example.practice6architecturaldesignpatterns.mvi.model.WeatherIntent
import com.example.practice6architecturaldesignpatterns.mvi.viewmodel.WeatherMviViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.setMain
import org.junit.After
import org.junit.Assert.*
import org.junit.Before
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class WeatherMviViewModelTest {

    @Before
    fun setUp() {
        Dispatchers.setMain(UnconfinedTestDispatcher())
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    private fun newViewModel() =
        WeatherMviViewModel(WeatherRepository(FakeWeatherApi(latencyMs = 0)))

    @Test
    fun selecting_a_known_city_ends_with_weather_and_no_loading() {
        val viewModel = newViewModel()
        viewModel.onIntent(WeatherIntent.SelectCity("Bukhara"))
        val state = viewModel.state.value
        assertFalse(state.isLoading)
        assertEquals("Bukhara", state.weather?.city)
        assertNull(state.error)
    }

    @Test
    fun selecting_an_unknown_city_ends_with_error() {
        val viewModel = newViewModel()
        viewModel.onIntent(WeatherIntent.SelectCity("Atlantis"))
        val state = viewModel.state.value
        assertFalse(state.isLoading)
        assertNull(state.weather)
        assertEquals("No forecast for Atlantis", state.error)
    }

    @Test
    fun dismissing_the_error_clears_it() {
        val viewModel = newViewModel()
        viewModel.onIntent(WeatherIntent.SelectCity("Atlantis"))
        viewModel.onIntent(WeatherIntent.DismissError)
        val state = viewModel.state.value
        assertNull(state.error)
    }
}
