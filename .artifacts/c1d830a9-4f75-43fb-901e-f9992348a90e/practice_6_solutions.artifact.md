# Practice 6: Architectural Design Patterns — Full Solutions

This document contains complete answers to all theoretical questions and fully implemented code files for all hands-on tasks from the Practice 6 assignment (`Practice 6 Architectural Design Patterns.docx`), written with simple logic and syntax as requested.

---

## Part 1 — Why architecture (Questions 1–2)

### Question 1 — What a proper architecture gives you (Theoretical)
- **Simplicity (KISS)**: In a 600-line `MainActivity`, mixing networking, parsing, shared preferences, validation, and UI updates makes the class bloated, hard to read, and difficult to understand.
- **Testability**: You cannot unit test network calls, UI updates, or SharedPreferences logic without spinning up an Android emulator or instrumentation test because they are all tightly coupled inside the Activity.
- **Low-Cost Maintenance**: Changing how JSON is parsed or how the network API works requires modifying the UI class, increasing the risk of introducing bugs.
- **First piece to move out**: Network/API calling logic (`WeatherRepository` or API client) because it has no dependency on Android UI components and can be tested independently.

### Question 2 — Starting from nothing (Theoretical)
**Plan for an existing app:**
1. **Analyze and Audit**: Identify God classes (like large Activities/Fragments) and tangled responsibilities.
2. **Extract Data Layer**: Move networking and database access into dedicated repositories/data sources.
3. **Decouple Business Logic**: Separate business rules from UI components.
4. **Choose a Pattern incrementally**: Introduce ViewModels (MVVM) or Presenters (MVP) screen by screen.
5. **Add Tests**: Write unit tests for repositories and viewmodels/presenters.

*Why "use MVVM everywhere" is bad advice*: Different screens have different complexities. A static About screen doesn't need a ViewModel, while a real-time chat screen or complex form might benefit from MVI or MVVM. Forcing a heavy pattern everywhere adds unnecessary boilerplate.

---

## Part 2 — One Model layer for all four patterns (Questions 3–4)

### [NEW] `data/model/Weather.kt`
```kotlin
package com.example.practice6architecturaldesignpatterns.data.model

data class Weather(
    val city: String,
    val temperatureC: Int,
    val condition: String
)
```

### [NEW] `data/api/WeatherApi.kt`
```kotlin
package com.example.practice6architecturaldesignpatterns.data.api

import com.example.practice6architecturaldesignpatterns.data.model.Weather

interface WeatherApi {
    suspend fun fetchWeather(city: String): Weather
}
```

### [NEW] `data/api/FakeWeatherApi.kt`
```kotlin
package com.example.practice6architecturaldesignpatterns.data.api

import com.example.practice6architecturaldesignpatterns.data.model.Weather
import kotlinx.coroutines.delay

class FakeWeatherApi(private val latencyMs: Long = 1_500) : WeatherApi {
    private val forecasts = mapOf(
        "Tashkent" to Weather("Tashkent", 24, "Sunny"),
        "Samarkand" to Weather("Samarkand", 21, "Cloudy"),
        "Bukhara" to Weather("Bukhara", 27, "Windy"),
    )

    override suspend fun fetchWeather(city: String): Weather {
        delay(latencyMs)
        return forecasts[city] ?: throw IllegalArgumentException("No forecast for $city")
    }
}
```

### [NEW] `data/repository/WeatherRepository.kt`
```kotlin
package com.example.practice6architecturaldesignpatterns.data.repository

import com.example.practice6architecturaldesignpatterns.data.api.WeatherApi
import com.example.practice6architecturaldesignpatterns.data.model.Weather

class WeatherRepository(private val api: WeatherApi) {
    suspend fun getWeather(city: String): Result<Weather> {
        return try {
            val weather = api.fetchWeather(city)
            Result.success(weather)
        } catch (e: IllegalArgumentException) {
            Result.failure(e)
        }
    }
}
```

### [NEW] `src/test/.../data/repository/WeatherRepositoryTest.kt`
```kotlin
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
```

### [NEW] `di/WeatherModule.kt`
```kotlin
package com.example.practice6architecturaldesignpatterns.di

import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import com.example.practice6architecturaldesignpatterns.data.api.FakeWeatherApi
import com.example.practice6architecturaldesignpatterns.data.repository.WeatherRepository
import com.example.practice6architecturaldesignpatterns.mvi.viewmodel.WeatherMviViewModel
import com.example.practice6architecturaldesignpatterns.mvvm.viewmodel.WeatherViewModel

object WeatherModule {
    val repository = WeatherRepository(FakeWeatherApi(latencyMs = 3_000))

    val weatherViewModelFactory: ViewModelProvider.Factory = viewModelFactory {
        initializer { WeatherViewModel(repository) }
    }

    val weatherMviViewModelFactory: ViewModelProvider.Factory = viewModelFactory {
        initializer { WeatherMviViewModel(repository) }
    }
}
```

### [NEW] `ui/WeatherScreen.kt`
```kotlin
package com.example.practice6architecturaldesignpatterns.ui

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp

@Composable
fun WeatherScreen(
    status: String,
    onCitySelected: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(24.dp),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(text = status, style = MaterialTheme.typography.headlineSmall)
        Spacer(modifier = Modifier.height(32.dp))
        val cities = listOf("Tashkent", "Samarkand", "Bukhara", "Atlantis")
        for (city in cities) {
            Button(
                onClick = { onCitySelected(city) },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 4.dp)
            ) {
                Text(text = city)
            }
        }
    }
}
```

---

## Part 3 — MVC (Questions 5–6)

### Question 5 — MVC roles (Theoretical)
- **Model**: 1 (Weather data class), 2 (WeatherRepository), 5 (FakeWeatherApi)
- **View**: 3 (WeatherScreen), 4 (onClick lambda updating TextView)
- **Controller**: 6 (WeatherController)
- **Piece 4 roles**: Acts as both Controller (handling user click and calling repository) and View (updating UI/TextView). This inflates Activity size and makes testing difficult.
- **Sentence completion**: The Model has no knowledge of the **View**.

### [NEW] `mvc/model/WeatherModel.kt`
```kotlin
package com.example.practice6architecturaldesignpatterns.mvc.model

import com.example.practice6architecturaldesignpatterns.data.model.Weather
import com.example.practice6architecturaldesignpatterns.data.repository.WeatherRepository

class WeatherModel(private val repository: WeatherRepository) {
    var isLoading: Boolean = false
        private set
    var weather: Weather? = null
        private set
    var error: String? = null
        private set

    fun startLoading() {
        isLoading = true
        error = null
    }

    suspend fun load(city: String) {
        repository.getWeather(city)
            .onSuccess { weather = it }
            .onFailure { error = it.message ?: "Unknown error" }
        isLoading = false
    }
}
```

### [NEW] `mvc/view/WeatherView.kt`
```kotlin
package com.example.practice6architecturaldesignpatterns.mvc.view

interface WeatherView {
    fun render()
}
```

### [NEW] `mvc/controller/WeatherController.kt`
```kotlin
package com.example.practice6architecturaldesignpatterns.mvc.controller

import com.example.practice6architecturaldesignpatterns.mvc.model.WeatherModel
import com.example.practice6architecturaldesignpatterns.mvc.view.WeatherView
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch

class WeatherController(
    private val model: WeatherModel,
    private val view: WeatherView,
    private val scope: CoroutineScope = CoroutineScope(SupervisorJob() + Dispatchers.Main),
) {
    fun onCitySelected(city: String) {
        scope.launch {
            model.startLoading()
            view.render()
            model.load(city)
            view.render()
        }
    }
}
```

### [NEW] `mvc/view/MvcActivity.kt`
```kotlin
package com.example.practice6architecturaldesignpatterns.mvc.view

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import com.example.practice6architecturaldesignpatterns.di.WeatherModule
import com.example.practice6architecturaldesignpatterns.mvc.controller.WeatherController
import com.example.practice6architecturaldesignpatterns.mvc.model.WeatherModel
import com.example.practice6architecturaldesignpatterns.ui.WeatherScreen

class MvcActivity : ComponentActivity(), WeatherView {
    private val model = WeatherModel(WeatherModule.repository)
    private val controller = WeatherController(model, view = this)
    private var status by mutableStateOf("Pick a city")

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            MaterialTheme {
                WeatherScreen(status = status, onCitySelected = controller::onCitySelected)
            }
        }
    }

    // render() takes no parameters because the View reads the Model directly.
    override fun render() {
        status = when {
            model.isLoading -> "Loading…"
            model.error != null -> "Error: ${model.error}"
            model.weather != null -> {
                val w = model.weather!!
                "${w.city}: ${w.temperatureC}°C, ${w.condition}"
            }
            else -> "Pick a city"
        }
    }
}
```

---

## Part 4 — MVP (Questions 7–9)

### Question 7 — MVP rules (Theoretical)
- (A) breaks rule 1 (View cannot access Model/Repository directly).
- (B) breaks rule 3 (View is not completely passive, updating UI directly from repository result).
- Constructor parameter `private val activity: WeatherActivity` tightly couples Presenter to Android Activity, making unit testing impossible without an Android device/emulator.

### [NEW] `mvp/WeatherContract.kt`
```kotlin
package com.example.practice6architecturaldesignpatterns.mvp

import com.example.practice6architecturaldesignpatterns.data.model.Weather

interface WeatherContract {
    interface View {
        fun showLoading()
        fun showWeather(weather: Weather)
        fun showError(message: String)
    }
}
```

### [NEW] `mvp/presenter/WeatherPresenter.kt`
```kotlin
package com.example.practice6architecturaldesignpatterns.mvp.presenter

import com.example.practice6architecturaldesignpatterns.data.repository.WeatherRepository
import com.example.practice6architecturaldesignpatterns.mvp.WeatherContract
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch

class WeatherPresenter(
    private val view: WeatherContract.View,
    private val repository: WeatherRepository,
    private val scope: CoroutineScope = CoroutineScope(SupervisorJob() + Dispatchers.Main),
) {
    fun onCitySelected(city: String) {
        view.showLoading()
        scope.launch {
            val result = repository.getWeather(city)
            result.onSuccess { weather ->
                view.showWeather(weather)
            }.onFailure { e ->
                view.showError(e.message ?: "Unknown error")
            }
        }
    }

    fun destroy() {
        scope.coroutineContext[kotlinx.coroutines.Job]?.cancel()
    }
}
```

### [NEW] `mvp/view/MvpActivity.kt`
```kotlin
package com.example.practice6architecturaldesignpatterns.mvp.view

import android.os.Bundle
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import com.example.practice6architecturaldesignpatterns.data.model.Weather
import com.example.practice6architecturaldesignpatterns.di.WeatherModule
import com.example.practice6architecturaldesignpatterns.mvp.WeatherContract
import com.example.practice6architecturaldesignpatterns.mvp.presenter.WeatherPresenter
import com.example.practice6architecturaldesignpatterns.ui.WeatherScreen

class MvpActivity : ComponentActivity(), WeatherContract.View {
    private var status by mutableStateOf("Pick a city")
    private val presenter = WeatherPresenter(
        view = this,
        repository = WeatherModule.repository,
    )

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        Log.d("MVP", "onCreate   activity=${hashCode()}")
        setContent {
            MaterialTheme {
                WeatherScreen(status = status, onCitySelected = presenter::onCitySelected)
            }
        }
    }

    override fun onDestroy() {
        Log.d("MVP", "onDestroy  activity=${hashCode()}")
        presenter.destroy()
        super.onDestroy()
    }

    override fun showLoading() { status = "Loading…" }

    override fun showWeather(weather: Weather) {
        Log.d("MVP", "showWeather called on activity=${hashCode()}")
        status = "${weather.city}: ${weather.temperatureC}°C, ${weather.condition}"
    }

    override fun showError(message: String) { status = "Error: $message" }
}
```

### [NEW] `src/test/.../mvp/WeatherPresenterTest.kt`
```kotlin
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
        view = view,
        repository = WeatherRepository(FakeWeatherApi(latencyMs = 0)),
        scope = CoroutineScope(Dispatchers.Unconfined),
    )

    @Test
    fun known_city_shows_loading_then_weather() {
        presenter.onCitySelected("Tashkent")
        assertEquals(listOf("loading", "weather:Tashkent"), view.events)
    }

    @Test
    fun unknown_city_shows_loading_then_error() {
        presenter.onCitySelected("Atlantis")
        assertEquals(listOf("loading", "error:No forecast for Atlantis"), view.events)
    }

    @Test
    fun two_requests_in_a_row_show_two_loading_events() {
        presenter.onCitySelected("Tashkent")
        presenter.onCitySelected("Samarkand")
        assertEquals(listOf("loading", "weather:Tashkent", "loading", "weather:Samarkand"), view.events)
    }
}
```

---

## Part 5 — MVVM (Questions 10–11)

### Question 10 — MVVM package mapping & ViewModel (Theoretical)
- `data/api`: `WeatherApi`, `FakeWeatherApi`
- `data/model`: `Weather`
- `data/repository`: `WeatherRepository`
- `presentation/viewmodel`: `WeatherViewModel`, `WeatherUiState`
- `presentation/view`: `MvvmActivity`, `WeatherScreen`
- `di`: `WeatherModule`
- (a) ViewModel must not hold Activity/Composable reference to prevent memory leaks across configuration changes; it exposes observable StateFlow instead.

### [NEW] `mvvm/viewmodel/WeatherUiState.kt`
```kotlin
package com.example.practice6architecturaldesignpatterns.mvvm.viewmodel

import com.example.practice6architecturaldesignpatterns.data.model.Weather

sealed interface WeatherUiState {
    data object Idle : WeatherUiState
    data object Loading : WeatherUiState
    data class Success(val weather: Weather) : WeatherUiState
    data class Error(val message: String) : WeatherUiState
}
```

### [NEW] `mvvm/viewmodel/WeatherViewModel.kt`
```kotlin
package com.example.practice6architecturaldesignpatterns.mvvm.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.practice6architecturaldesignpatterns.data.repository.WeatherRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class WeatherViewModel(private val repository: WeatherRepository) : ViewModel() {
    private val _uiState = MutableStateFlow<WeatherUiState>(WeatherUiState.Idle)
    val uiState: StateFlow<WeatherUiState> = _uiState.asStateFlow()

    fun onCitySelected(city: String) {
        _uiState.value = WeatherUiState.Loading
        viewModelScope.launch {
            val result = repository.getWeather(city)
            result.onSuccess { weather ->
                _uiState.value = WeatherUiState.Success(weather)
            }.onFailure { e ->
                _uiState.value = WeatherUiState.Error(e.message ?: "Unknown error")
            }
        }
    }
}
```

### [NEW] `mvvm/view/MvvmActivity.kt`
```kotlin
package com.example.practice6architecturaldesignpatterns.mvvm.view

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.viewModels
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.getValue
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.practice6architecturaldesignpatterns.di.WeatherModule
import com.example.practice6architecturaldesignpatterns.mvvm.viewmodel.WeatherUiState
import com.example.practice6architecturaldesignpatterns.mvvm.viewmodel.WeatherViewModel
import com.example.practice6architecturaldesignpatterns.ui.WeatherScreen

class MvvmActivity : ComponentActivity() {
    private val viewModel: WeatherViewModel by viewModels {
        WeatherModule.weatherViewModelFactory
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            MaterialTheme {
                val uiState by viewModel.uiState.collectAsStateWithLifecycle()
                WeatherScreen(
                    status = uiState.toStatusText(),
                    onCitySelected = viewModel::onCitySelected,
                )
            }
        }
    }
}

private fun WeatherUiState.toStatusText(): String = when (this) {
    is WeatherUiState.Idle -> "Pick a city"
    is WeatherUiState.Loading -> "Loading…"
    is WeatherUiState.Success -> "${weather.city}: ${weather.temperatureC}°C, ${weather.condition}"
    is WeatherUiState.Error -> "Error: $message"
}
```

---

## Part 6 — MVI (Questions 12–13)

### Question 12 — MVI principles (Theoretical)
- (1) Mutable properties break Immutability and Unidirectional Data Flow.
- (2) Direct mutation and calling load from event handler breaks unidirectional data flow.
- (3) Mutable properties in state break immutability.
- (4) Direct mutation from outside ViewModel breaks encapsulation and MVI unidirectional flow.

### [NEW] `mvi/model/WeatherState.kt`
```kotlin
package com.example.practice6architecturaldesignpatterns.mvi.model

import com.example.practice6architecturaldesignpatterns.data.model.Weather

data class WeatherState(
    val isLoading: Boolean = false,
    val weather: Weather? = null,
    val error: String? = null,
)

sealed interface WeatherIntent {
    data class SelectCity(val city: String) : WeatherIntent
    data object DismissError : WeatherIntent
}
```

### [NEW] `mvi/viewmodel/WeatherMviViewModel.kt`
```kotlin
package com.example.practice6architecturaldesignpatterns.mvi.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.practice6architecturaldesignpatterns.data.repository.WeatherRepository
import com.example.practice6architecturaldesignpatterns.mvi.model.WeatherIntent
import com.example.practice6architecturaldesignpatterns.mvi.model.WeatherState
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class WeatherMviViewModel(private val repository: WeatherRepository) : ViewModel() {
    private val _state = MutableStateFlow(WeatherState())
    val state: StateFlow<WeatherState> = _state.asStateFlow()

    fun onIntent(intent: WeatherIntent) {
        when (intent) {
            is WeatherIntent.SelectCity -> load(intent.city)
            WeatherIntent.DismissError -> {
                _state.update { it.copy(error = null) }
            }
        }
    }

    private fun load(city: String) {
        _state.update { it.copy(isLoading = true, error = null) }
        viewModelScope.launch {
            val result = repository.getWeather(city)
            result.onSuccess { weather ->
                _state.update { it.copy(isLoading = false, weather = weather, error = null) }
            }.onFailure { e ->
                _state.update { it.copy(isLoading = false, error = e.message ?: "Unknown error") }
            }
        }
    }
}
```

### [NEW] `mvi/view/MviActivity.kt`
```kotlin
package com.example.practice6architecturaldesignpatterns.mvi.view

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.viewModels
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.getValue
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.practice6architecturaldesignpatterns.di.WeatherModule
import com.example.practice6architecturaldesignpatterns.mvi.model.WeatherIntent
import com.example.practice6architecturaldesignpatterns.mvi.model.WeatherState
import com.example.practice6architecturaldesignpatterns.mvi.viewmodel.WeatherMviViewModel
import com.example.practice6architecturaldesignpatterns.ui.WeatherScreen

class MviActivity : ComponentActivity() {
    private val viewModel: WeatherMviViewModel by viewModels {
        WeatherModule.weatherMviViewModelFactory
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            MaterialTheme {
                val state by viewModel.state.collectAsStateWithLifecycle()
                WeatherScreen(
                    status = state.toStatusText(),
                    onCitySelected = { city ->
                        viewModel.onIntent(WeatherIntent.SelectCity(city))
                    },
                )
            }
        }
    }
}

private fun WeatherState.toStatusText(): String = when {
    isLoading -> "Loading…"
    error != null -> "Error: $error"
    weather != null -> "${weather.city}: ${weather.temperatureC}°C, ${weather.condition}"
    else -> "Pick a city"
}
```

### [NEW] `src/test/.../mvi/WeatherMviViewModelTest.kt`
```kotlin
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
```

### [NEW] `MainActivity.kt` (Launcher Activity)
```kotlin
package com.example.practice6architecturaldesignpatterns

import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.example.practice6architecturaldesignpatterns.mvc.view.MvcActivity
import com.example.practice6architecturaldesignpatterns.mvp.view.MvpActivity
import com.example.practice6architecturaldesignpatterns.mvvm.view.MvvmActivity
import com.example.practice6architecturaldesignpatterns.mvi.view.MviActivity

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            MaterialTheme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(24.dp),
                        verticalArrangement = Arrangement.Center,
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text(
                            text = "Weather Patterns Launcher",
                            style = MaterialTheme.typography.headlineMedium
                        )
                        Spacer(modifier = Modifier.height(32.dp))

                        Button(
                            onClick = { startActivity(Intent(this@MainActivity, MvcActivity::class.java)) },
                            modifier = Modifier.fillMaxWidth().padding(vertical = 8.dp)
                        ) {
                            Text("MVC Pattern")
                        }

                        Button(
                            onClick = { startActivity(Intent(this@MainActivity, MvpActivity::class.java)) },
                            modifier = Modifier.fillMaxWidth().padding(vertical = 8.dp)
                        ) {
                            Text("MVP Pattern")
                        }

                        Button(
                            onClick = { startActivity(Intent(this@MainActivity, MvvmActivity::class.java)) },
                            modifier = Modifier.fillMaxWidth().padding(vertical = 8.dp)
                        ) {
                            Text("MVVM Pattern")
                        }

                        Button(
                            onClick = { startActivity(Intent(this@MainActivity, MviActivity::class.java)) },
                            modifier = Modifier.fillMaxWidth().padding(vertical = 8.dp)
                        ) {
                            Text("MVI Pattern")
                        }
                    }
                }
            }
        }
    }
}
```

---

## Part 7 — Compare and choose (Questions 14–15)

### Question 14 — Comparison Table
| Pattern | Logic Holder | Imports `android.*`? | Imports `androidx.*`? | View holds reference to | Survives Rotation? |
|---|---|---|---|---|---|
| **MVC** | Controller & Model | No (Model/Controller) / Yes (Activity View) | Yes (Activity View) | Controller | No (Activity destroyed) |
| **MVP** | Presenter | No (Presenter) / Yes (Activity View) | Yes (Activity View) | Presenter | No (Activity destroyed) |
| **ViewModel (MVVM)** | ViewModel | No | Yes (`androidx.lifecycle.*`) | Observes StateFlow | Yes (ViewModel survives) |
| **MVI** | ViewModel | No | Yes (`androidx.lifecycle.*`) | Observes StateFlow | Yes (ViewModel survives) |

### Question 15 — Choose a pattern (Theoretical)
1. **About screen**: No pattern, only a data layer (too simple for architecture). Cost: none.
2. **Chat screen**: MVVM or MVI (handles complex asynchronous state changes, typing indicator, messages list). Cost: boilerplate.
3. **5-year-old app with 40 Activities**: No pattern / gradual refactoring (MVP or MVVM per module). Cost: refactoring risk.
- Situations (1) and (2) can live in the same app with different structures, supported by the lecture statement: *each module in a complex project can have its own structure*.
