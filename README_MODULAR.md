# 🏗️ МОДУЛЬНАЯ АРХИТЕКТУРА WeatherWearAdvisor

## 📐 Структура модулей

```
project/
├── app/                                 ← 🎯 Главное приложение (UI слой)
│   ├── build.gradle.kts
│   ├── src/main/
│   │   ├── java/...
│   │   │   ├── MainActivity.kt
│   │   │   ├── Fragment/
│   │   │   ├── ViewModel/
│   │   │   ├── repository/
│   │   │   └── di/AppContainer.kt
│   │   └── AndroidManifest.xml
│   └── # Зависиmости: :core:common, :features:weather
│
├── core/                                ← 🔧 Слой инфраструктуры
│   └── common/
│       ├── build.gradle.kts
│       ├── src/main/
│       │   ├── java/...
│       │   │   ├── di/CommonModuleProvider.kt
│       │   │   └── security/EncryptedPreferencesManager.kt
│       │   └── AndroidManifest.xml
│       └── # Зависимости: Firebase, Security
│
└── features/                            ← ✨ Бизнес-логика (модули функции)
    └── weather/
        ├── build.gradle.kts
        ├── src/main/
        │   ├── java/...
        │   │   ├── WeatherModuleProvider.kt
        │   │   ├── api/OpenWeatherMapApi.kt
        │   │   ├── model/WeatherData.kt
        │   │   └── repository/WeatherRepository.kt
        │   └── AndroidManifest.xml
        └── # Зависимости: :core:common, Retrofit
```

---

## 🔀 Граф зависимостей

```
:app
├── :core:common ◄─── Общие компоненты (Security, DI)
├── :features:weather ◄─── API погоды (независимый модуль)
└── Локальные: Repository, ViewModel, Fragment, etc.

:features:weather
└── :core:common ◄─── Использует EncryptedPreferencesManager

:core:common
├── Firebase
└── androidx.security
```

**Правило:** Если модуль зависит от другого, то обратной зависимости быть не может ✅

---

## 🎯 Назначение каждого модуля

### 📦 `:app` — Главное приложение

**Что содержит:**
- `MainActivity.kt` — точка входа
- `Fragment/` — экраны приложения
- `ViewModel/` — бизнес-логика экранов
- `repository/` — локальные репозитории (Auth, Wardrobe, Location)
- `di/AppContainer.kt` — объединение модулей

**Зависимости от:**
- `:core:common` — Security, DI
- `:features:weather` — Weather API

**Может ли от него зависеть:**
- ❌ НЕТ (это главный модуль, от него зависят другие)

---

### 🔧 `:core:common` — Инфраструктура

**Что содержит:**
- `security/EncryptedPreferencesManager.kt` — управление шифрованными данными
- `di/CommonModuleProvider.kt` — точка входа модуля

**Зависимости от:**
- Firebase (для auth зависимостей)
- androidx.security (для шифрования)

**Может ли от него зависеть:**
- ✅ `:app` — использует Security
- ✅ `:features:weather` — использует EncryptedPreferencesManager
- ✅ Другие модули :features:*

**Почему он отдельный:**
- Других модулей нужны общие компоненты безопасности
- Можно обновлять security без пересборки всего приложения
- Легко добавлять новые модули (все будут иметь уже готовый Security)

---

### ✨ `:features:weather` — Модуль функции "Погода"

**Что содержит:**
- `api/OpenWeatherMapApi.kt` — Retrofit API клиент
- `model/WeatherData.kt` — моделиданных
- `repository/WeatherRepository.kt` — бизнес-логика получения погоды
- `WeatherModuleProvider.kt` — точка входа модуля

**Зависимости от:**
- `:core:common` — EncryptedPreferencesManager (если нужны безопасные данные)
- Retrofit — HTTP клиент
- OkHttp — сетевые запросы

**Может ли от него зависеть:**
- ✅ `:app` — главное приложение использует Weather
- ✅ Другие модули :features:* (если нужны данные о погоде)

**Почему он отдельный:**
- Можно разрабатывать независимо (команда может работать параллельно)
- Можно переиспользовать в других приложениях
- Легко добавить своё веб-приложение, которое будет использовать только этот модуль
- Если погода перестанет работать, это не затронет остальное приложение

---

## 📊 Организация работы команды

### До модульности: 👎 Конфликты
```
Developer A работает в :app на Auth
Developer B работает в :app на Weather API
→ Конфликты слияний (merge conflicts)
→ Tests переносят друг друга
→ Если A сломает что-то, B не может собрать
```

### После модульности: 👍 Независимость
```
Developer A → работает в :features:weather (Weather API)
Developer B → работает в :app (UI/ViewModels)
Developer C → работает в :core:common (Security)

→ Независимая разработка 
→ Параллельные сборки
→ Конфликты минимальны
→ Каждый может тестировать свой модуль отдельно
```

---

## 🔍 Как использовать модульную архитектуру?

### 1️⃣ Инициализация в MainActivity

```kotlin
class MainActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        
        // 1. Инициализировать core:common
        CommonModuleProvider.initialize(this)
        
        // 2. Инициализировать features:weather
        WeatherModuleProvider.initialize("API_KEY_HERE")
        
        // 3. Инициализировать app контейнер
        AppContainer.init(this)
        
        // Теперь всё готово к использованию
    }
}
```

### 2️⃣ Использование Weather в Fragment

```kotlin
class HomeFragment : Fragment() {
    private val weatherRepository: WeatherRepository by lazy {
        AppContainer.provideWeatherRepository()  // ← Из модуля :features:weather
    }
    
    private fun loadWeather(city: String) {
        lifecycleScope.launch {
            val result = weatherRepository.getWeatherByCity(city)
            result.onSuccess { weatherData ->
                binding.tvTemperature.text = "${weatherData.temperature}°"
            }
            result.onFailure { error ->
                showError(error.message)
            }
        }
    }
}
```

### 3️⃣ Использование Security из core:common

```kotlin
val encryptedPrefs = CommonModuleProvider.getEncryptedPreferencesManager()
encryptedPrefs.saveUserId("user123")  // ← Шифруется
val userId = encryptedPrefs.getUserId()  // ← Расшифровывается
```

---

## 🚀 Добавление нового модуля

Допустим, нужно добавить модуль `:features:outfits` (группировка одежды после разработки Weather).

### Шаг 1: Создать структуру
```
features/
└── outfits/
    ├── build.gradle.kts
    ├── src/main/
    │   ├── java/com/stepanov...
    │   │   ├── OutfitModuleProvider.kt
    │   │   ├── model/Outfit.kt
    │   │   ├── repository/OutfitRepository.kt
    │   │   └── di/OutfitLocalDi.kt
    │   └── AndroidManifest.xml
    └── proguard-rules.pro
```

### Шаг 2: build.gradle.kts
```kotlin
plugins {
    alias(libs.plugins.android.library)
    alias(libs.plugins.kotlin.android)
}

android { ... }

dependencies {
    implementation(project(":core:common"))  // Зависит только от core
    implementation("пакеты для модуля")
}
```

### Шаг 3: Добавить в settings.gradle.kts
```kotlin
include(":features:outfits")
```

### Шаг 4: Обновить app/build.gradle.kts
```kotlin
dependencies {
    implementation(project(":features:outfits"))
}
```

### Шаг 5: Использовать в app
```kotlin
val outfitRepository = AppContainer.provideOutfitRepository()
```

**Готово!** Модуль полностью интегрирован и может разрабатываться отдельно ✅

---

## 📈 Преимущества модульной архитектуры

| Аспект | Монолит | Модули |
|--------|:-------:|:------:|
| **Время сборки** | 5-10 мин | 1-2 мин* |
| **Размер APK** | Full | Можно оптимизировать** |
| **Параллельная разработка** | ❌ Конфликты | ✅ Независимость |
| **Переиспользование кода** | ❌ Сложно | ✅ Просто |
| **Тестирование модуля** | ❌ Нужна вся app | ✅ Отдельно |
| **Добавление новой функции** | ❌ Изменѐтся 5+ файлов | ✅ Новый модуль |

*сборка только измененных модулей  
**Dynamic feature modules в Google Play

---

## ⚠️ Правила при работе с модулями

### ✅ ПРАВИЛЬНО
```kotlin
// ✅ :features:weather зависит от :core:common
// :features:weather/build.gradle.kts
dependencies {
    implementation(project(":core:common"))
}
```

### ❌ НЕПРАВИЛЬНО
```kotlin
// ❌ :core:common зависит от :features:weather
// (циклическая зависимость!)
// :core:common/build.gradle.kts
dependencies {
    implementation(project(":features:weather"))  // ❌ ЗАПРЕЩЕНО
}
```

### ✅ ПРАВИЛЬНО
```kotlin
// ✅ Модули используют интерфейсы для взаимодействия
// :features:weather/repository/WeatherRepository.kt
interface WeatherRepository {
    suspend fun getWeatherByCity(city: String): Result<WeatherData>
}

// :app/Fragment используют интерфейс
private val weatherRepository: WeatherRepository by lazy {
    AppContainer.provideWeatherRepository()
}
```

### ❌ НЕПРАВИЛЬНО
```kotlin
// ❌ Импортировать внутреннюю класс из другого модуля
// :app/Fragment
import com.stepanov_ivan.weatherwearadvisor.weather.repository.WeatherRepositoryImpl  // ❌

val weatherRepository = WeatherRepositoryImpl(...)  // ❌ Нарушена инкапсуляция
```

---

## 📚 Файлы документации

- [README_MODULAR.md](README_MODULAR.md) ← ты здесь
- [MODULAR_INTEGRATION.md](MODULAR_INTEGRATION.md) ← примеры интеграции
- Исходный код:
  - [CommonModuleProvider.kt](core/common/src/main/java/.../di/CommonModuleProvider.kt)
  - [WeatherModuleProvider.kt](features/weather/src/main/java/.../WeatherModuleProvider.kt)
  - [AppContainer.kt обновлённый](app/src/main/java/.../di/AppContainer.kt)

---

## 🎓 Вывод

Модульная архитектура с Weather API позволяет:

✅ **Масштабировать** — добавлять новые модули без влияния на существующие
✅ **Параллелизм** — разные команды работают независимо
✅ **Переиспользование** — Weather API можно использовать в других проектах
✅ **Чистота кода** — каждый модуль отвечает за одно
✅ **Тестируемость** — модули тестируются изолированно

**Это как перейти с однокомнатной квартиры на многоэтажный комплекс с отдельными офисами** 🏢
