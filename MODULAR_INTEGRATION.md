# 🌤️ ИНТЕГРАЦИЯ Weather API в модульную архитектуру

## 📋 Полный пример: Отображение погоды на главном экране

### Шаг 1: Настройка API ключа

**Получить ключ:** https://openweathermap.org/api

**В build.gradle (root):**
```gradle
ext {
    OPENWEATHER_API_KEY = "YOUR_API_KEY_HERE"
}
```

**Или в BuildConfig (app/build.gradle.kts):**
```kotlin
buildTypes {
    debug {
        buildConfigField("String", "OPENWEATHER_API_KEY", "\"YOUR_API_KEY\"")
    }
    release {
        buildConfigField("String", "OPENWEATHER_API_KEY", "\"YOUR_API_KEY\"")
    }
}
```

---

### Шаг 2: Инициализация модуля (MainActivity.kt)

```kotlin
class MainActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        
        // 1. Инициализировать core:common
        CommonModuleProvider.initialize(this)
        
        // 2. Инициализировать features:weather с API ключом
        WeatherModuleProvider.initialize(BuildConfig.OPENWEATHER_API_KEY)
        
        // 3. Инициализировать app контейнер
        AppContainer.init(this)
        
        // Продолжить со старым кодом...
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)
    }
}
```

---

### Шаг 3: ViewModel для отображения погоды

**создать: `viewmodel/HomeViewModel.kt`**

```kotlin
package com.stepanov_ivan.weatherwearadvisor.viewmodel

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.stepanov_ivan.weatherwearadvisor.di.AppContainer
import com.stepanov_ivan.weatherwearadvisor.weather.model.WeatherData
import kotlinx.coroutines.launch

sealed class WeatherState {
    object Loading : WeatherState()
    data class Success(val weatherData: WeatherData) : WeatherState()
    data class Error(val message: String) : WeatherState()
}

class HomeViewModel : ViewModel() {
    
    private val weatherRepository = AppContainer.provideWeatherRepository()
    
    private val _weatherState = MutableLiveData<WeatherState>()
    val weatherState: LiveData<WeatherState> = _weatherState

    fun loadWeather(city: String) {
        viewModelScope.launch {
            _weatherState.value = WeatherState.Loading
            
            val result = weatherRepository.getWeatherByCity(city)
            
            result.onSuccess { weatherData ->
                _weatherState.value = WeatherState.Success(weatherData)
            }
            result.onFailure { error ->
                _weatherState.value = WeatherState.Error(
                    error.message ?: "Неизвестная ошибка"
                )
            }
        }
    }

    fun loadWeatherByCoordinates(latitude: Double, longitude: Double) {
        viewModelScope.launch {
            _weatherState.value = WeatherState.Loading
            
            val result = weatherRepository.getWeatherByCoordinates(latitude, longitude)
            
            result.onSuccess { weatherData ->
                _weatherState.value = WeatherState.Success(weatherData)
            }
            result.onFailure { error ->
                _weatherState.value = WeatherState.Error(
                    error.message ?: "Неизвестная ошибка"
                )
            }
        }
    }
}
```

---

### Шаг 4: Fragment для отображения

**обновить: `fragment/HomeFragment.kt`**

```kotlin
package com.stepanov_ivan.weatherwearadvisor.fragment

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import com.stepanov_ivan.weatherwearadvisor.databinding.FragmentHomeBinding
import com.stepanov_ivan.weatherwearadvisor.viewmodel.HomeViewModel
import com.stepanov_ivan.weatherwearadvisor.viewmodel.WeatherState

class HomeFragment : Fragment() {

    private var _binding: FragmentHomeBinding? = null
    private val binding get() = _binding!!
    
    private val viewModel: HomeViewModel by viewModels()

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentHomeBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        
        // Загрузить погоду для Москвы по умолчанию
        viewModel.loadWeather("Moscow")
        
        // Наблюдать за изменениями
        viewModel.weatherState.observe(viewLifecycleOwner) { state ->
            when (state) {
                is WeatherState.Loading -> {
                    binding.progressBar.visibility = View.VISIBLE
                    binding.contentGroup.visibility = View.GONE
                }
                is WeatherState.Success -> {
                    binding.progressBar.visibility = View.GONE
                    binding.contentGroup.visibility = View.VISIBLE
                    displayWeather(state.weatherData)
                }
                is WeatherState.Error -> {
                    binding.progressBar.visibility = View.GONE
                    binding.contentGroup.visibility = View.GONE
                    binding.tvError.text = "Ошибка: ${state.message}"
                    binding.tvError.visibility = View.VISIBLE
                }
            }
        }
    }

    private fun displayWeather(weatherData: com.stepanov_ivan.weatherwearadvisor.weather.model.WeatherData) {
        binding.tvCity.text = "📍 ${weatherData.city}"
        binding.tvTemperature.text = "${weatherData.temperature.toInt()}°"
        binding.tvFeelsLike.text = "Ощущается как: ${weatherData.feelsLike.toInt()}°"
        binding.tvCondition.text = "🌤️ ${weatherData.description.capitalize()}"
        binding.tvHumidity.text = "💧 Влажность: ${weatherData.humidity}%"
        binding.tvWindSpeed.text = "💨 Ветер: ${weatherData.windSpeed} м/с"
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
```

---

### Шаг 5: Layout для отображения

**обновить: `layout/fragment_home.xml`**

```xml
<?xml version="1.0" encoding="utf-8"?>
<LinearLayout xmlns:android="http://schemas.android.com/apk/res/android"
    android:layout_width="match_parent"
    android:layout_height="match_parent"
    android:orientation="vertical"
    android:padding="16dp">

    <ProgressBar
        android:id="@+id/progressBar"
        android:layout_width="wrap_content"
        android:layout_height="wrap_content"
        android:layout_gravity="center"
        android:visibility="gone" />

    <LinearLayout
        android:id="@+id/contentGroup"
        android:layout_width="match_parent"
        android:layout_height="wrap_content"
        android:orientation="vertical"
        android:visibility="gone">

        <TextView
            android:id="@+id/tvCity"
            android:layout_width="wrap_content"
            android:layout_height="wrap_content"
            android:textSize="24sp"
            android:textStyle="bold" />

        <TextView
            android:id="@+id/tvTemperature"
            android:layout_width="wrap_content"
            android:layout_height="wrap_content"
            android:textSize="48sp"
            android:textStyle="bold"
            android:textColor="@android:color/holo_blue_dark" />

        <TextView
            android:id="@+id/tvFeelsLike"
            android:layout_width="wrap_content"
            android:layout_height="wrap_content"
            android:textSize="16sp" />

        <TextView
            android:id="@+id/tvCondition"
            android:layout_width="wrap_content"
            android:layout_height="wrap_content"
            android:textSize="18sp"
            android:layout_marginTop="8dp" />

        <TextView
            android:id="@+id/tvHumidity"
            android:layout_width="wrap_content"
            android:layout_height="wrap_content"
            android:textSize="14sp"
            android:layout_marginTop="4dp" />

        <TextView
            android:id="@+id/tvWindSpeed"
            android:layout_width="wrap_content"
            android:layout_height="wrap_content"
            android:textSize="14sp"
            android:layout_marginTop="4dp" />

    </LinearLayout>

    <TextView
        android:id="@+id/tvError"
        android:layout_width="wrap_content"
        android:layout_height="wrap_content"
        android:textColor="@android:color/holo_red_dark"
        android:textSize="16sp"
        android:visibility="gone" />

</LinearLayout>
```

---

## 🔄 Архитектурный цикл

```
User открывает App
     ↓
MainActivity.onCreate()
     ├─ CommonModuleProvider.initialize(context)
     ├─ WeatherModuleProvider.initialize(apiKey)  ← Retrofit клиент готов
     └─ AppContainer.init(context)
     ↓
HomeFragment загружается
     ↓
HomeViewModel.loadWeather("Moscow")
     ↓
AppContainer.provideWeatherRepository()  ← Получаем WeatherRepository
     ↓
WeatherRepository.getWeatherByCity("Moscow")  ← Делаем HTTP запрос
     ↓
OpenWeatherMapApi.getWeatherByCity()  ← Retrofit
     ↓
HTTP GET https://api.openweathermap.org/data/2.5/weather?q=Moscow...
     ↓
Получена OpenWeatherResponse
     ↓
Конвертируется в WeatherData ← toWeatherData()
     ↓
Result.success(weatherData)
     ↓
ViewModel обновляет LiveData
     ↓
Fragment получает изменение и обновляет UI
     ↓
Пользователь видит: "Москва 15°, ясно"
```

---

## 🧪 Тестирование Weather модуля

### Unit тест для Repository

**создать: `features/weather/src/test/...WeatherRepositoryTest.kt`**

```kotlin
package com.stepanov_ivan.weatherwearadvisor.weather.repository

import com.stepanov_ivan.weatherwearadvisor.weather.api.OpenWeatherMapApi
import com.stepanov_ivan.weatherwearadvisor.weather.model.OpenWeatherResponse
import kotlinx.coroutines.runBlocking
import org.junit.Before
import org.junit.Test
import org.mockito.Mock
import org.mockito.MockitoAnnotations
import org.mockito.kotlin.whenever

class WeatherRepositoryTest {

    @Mock
    private lateinit var mockApi: OpenWeatherMapApi

    private lateinit var repository: WeatherRepository

    @Before
    fun setUp() {
        MockitoAnnotations.openMocks(this)
        repository = WeatherRepositoryImpl(mockApi, "test_api_key")
    }

    @Test
    fun `getWeatherByCity returns success when API call succeeds`() = runBlocking {
        // Arrange
        val mockResponse = OpenWeatherResponse(
            main = OpenWeatherResponse.Main(
                temp = 15.0,
                feels_like = 13.0,
                humidity = 65
            ),
            weather = listOf(
                OpenWeatherResponse.Weather(
                    main = "Clear",
                    description = "ясно",
                    icon = "01d"
                )
            ),
            wind = OpenWeatherResponse.Wind(speed = 5.0),
            name = "Moscow"
        )

        whenever(mockApi.getWeatherByCity("Moscow", "test_api_key"))
            .thenReturn(mockResponse)

        // Act
        val result = repository.getWeatherByCity("Moscow")

        // Assert
        assert(result.isSuccess)
        result.onSuccess { weatherData ->
            assert(weatherData.city == "Moscow")
            assert(weatherData.temperature == 15.0)
        }
    }
}
```

---

## 📊 Сравнение: ДО и ПОСЛЕ модульности

### ДО: Монолит :app
```
Добавить Weather API:
1. Добавить Retrofit в app/build.gradle.kts
2. Создать OpenWeatherMapApi в app/.../api/
3. Создать WeatherRepository в app/.../repository/
4. Создать ViewModel в app/.../viewmodel/
5. Обновить Fragment
6. Обновить Layout
7. Пересобрать весь :app (изменился код App, API, Repository, ViewModel, Fragment, Layout)
→ Время сборки: 5 мин
→ Конфликты merge (если кто-то ещё правил :app)
```

### ПОСЛЕ: Модули
```
Добавить Weather API:
1. Создан :features:weather модуль (Retrofit, API, Repository уже готовы)
2. Просто добавить ViewModel и Fragment в :app
3. Пересобрать только :app (остальное необновлено)
→ Время сборки: 1 мин (только :app, :features:weather уже скомпилирован)
→ Никаких конфликтов merge (Weather модуль отдельно)
```

---

## 🎯 Преимущества для Development процесса

### Независимая разработка
```
Developer API:
→ Работает в :features:weather
→ Создаёт API клиент + Repository
→ Может писать unit тесты (не нужен UI)
→ Готово за 2 часа

Developer UI:
→ Разводит работу в :app
→ Создаёт ViewModel + Fragment + Layout
→ Может работать параллельно (API статус известен)
→ Готово за 3 часа

Результат: Параллельная разработка = готово за 3 часа (вместо 5)
```

### Debug без UI
```
// Можно тестировать Weather без разработки UI
fun testWeatherAPI() {
    val apiKey = "your_key"
    WeatherModuleProvider.initialize(apiKey)
    val repo = WeatherModuleProvider.getWeatherRepository()
    
    runBlocking {
        val result = repo.getWeatherByCity("Moscow")
        println(result)  // Какие данные получаются
    }
}
```

---

## 🚀 Next Steps

1. ✅ Модульная архитектура создана
2. ✅ Weather API модуль готов
3. ⏳ Добавить UI для отображения (HomeViewModel + HomeFragment)
4. ⏳ Добавить LocationProvider для geo-based weather
5. ⏳ Добавить :features:outfits модуль (рекомендации одежды по погоде)

---

## 📚 Файлы для изучения

- [README_MODULAR.md](README_MODULAR.md) — архитектура модулей
- [Код Weather API](features/weather/src/main/java/com/stepanov_ivan/weatherwearadvisor/weather/)
- [Обновлённый AppContainer](app/src/main/java/com/stepanov_ivan/weatherwearadvisor/di/AppContainer.kt)
- [main/MainActivity.kt](app/src/main/java/com/stepanov_ivan/weatherwearadvisor/MainActivity.kt)

---

**Статус:** ✅ Готово к использованию
**Модули:** 3 ✅ (:app, :core:common, :features:weather)
**API:** OpenWeatherMap ✅
**Безопасность:** Шифрование + Access Control ✅
