# Архитектура безопасности WeatherWearAdvisor

## Реализованные способы повышения безопасности

### 1. 🔐 Шифрование данных (EncryptedSharedPreferences)

**Что защищаем:**
- `userId` пользователя
- Email пользователя
- Другие чувствительные данные

**Как это работает:**
- Использует `androidx.security:security-crypto`
- AES-256-GCM для шифрования значений
- AES-256-SIV для шифрования ключей
- Автоматически генерирует главный ключ (`MasterKey`)

**Файл:** `EncryptedPreferencesManager.kt`

```kotlin
// Пример использования
val encryptedPrefs = EncryptedPreferencesManager(context)
encryptedPrefs.saveUserId(userId)  // Сохраняется в зашифрованном виде
val userId = encryptedPrefs.getUserId()  // Автоматически расшифровывается
```

**Преимущества:**
- ✅ Даже при root-доступе данные останутся защищены
- ✅ Соответствует Android Security guidelines
- ✅ Прозрачное использование (как обычные SharedPreferences)

---

### 2. 🛡️ Управление доступом (Access Control)

**Проблема:** 
Без проверки доступа пользователь A может получить гардероб пользователя B, просто изменив параметр `userId`

**Решение:**
Каждый запрос к данным проверяет, принадлежат ли они текущему аутентифицированному пользователю

**Реализация в WardrobeRepositoryImpl:**
```kotlin
override suspend fun addItem(item: ClothingItem): Resource<Unit> {
    val currentUserId = authRepository?.getCurrentUserId()
    // ✅ Проверка доступа
    if (currentUserId == null || currentUserId != item.userId) {
        return Resource.Error("Доступ запрещён: невозможно добавить элемент в чужой гардероб")
    }
    // ... только потом добавляем
}
```

**Где применяется:**
- `getItems()` — получение гардероба
- `addItem()` — добавление предмета
- `updateItem()` — изменение предмета
- `deleteItem()` — удаление предмета

**Преимущества:**
- ✅ Каждый пользователь получает доступ только к своим данным
- ✅ SQL-инъекции не помогут (userId проверяется в коде)
- ✅ Защита от случайного перемешивания данных

---

### 3. 🏗️ Изоляция компонентов (Component Isolation)

**Архитектура:**

```
AppContainer (DI контейнер)
├── EncryptedPreferencesManager
│   └── Управляет зашифрованным хранилищем
│
├── AuthManager → AuthRepository
│   └── Аутентификация + управление доступом
│
├── WardrobeRepository
│   ├── WardrobeDao (Database)
│   └── AuthRepository (для провероки доступа)
│
└── LocationRepository
    └── Статические данные
```

**Стек вызовов при запросе данных:**
```
Fragment
  ↓
ViewModel
  ↓
WardrobeRepository ←── [Проверка доступа] ← AuthRepository
  ↓
WardrobeDao
  ↓
Room Database
```

**Файл:** [di/AppContainer.kt](app/src/main/java/com/stepanov_ivan/weatherwearadvisor/di/AppContainer.kt)

```kotlin
// Инициализация при старте приложения
AppContainer.init(context)

// Использование в коде
val authRepository = AppContainer.authRepository
val wardrobeRepository = AppContainer.provideWardrobeRepository(context)
```

**Преимущества:**
- ✅ Слабая связанность компонентов (loose coupling)
- ✅ Легко тестировать (можно подменять зависимости)
- ✅ Ясная иерархия ответственности
- ✅ Защита от циклических зависимостей

---

## Матрица безопасности

| Угроза | Решение | Статус |
|--------|---------|--------|
| **Утечка userId/email** | EncryptedSharedPreferences | ✅ ЗАЩИЩЕНО |
| **Пользователь видит чужой гардероб** | Access Control (userId проверка) | ✅ ЗАЩИЩЕНО |
| **Несанкционированное изменение данных** | Authorization checks | ✅ ЗАЩИЩЕНО |
| **Неконтролируемый доступ между компонентами** | DI контейнер + Interface segregation | ✅ ИЗОЛИРОВАНО |
| **Шифрование в БД (Room)** | ❌ TODO (EncryptedRoom) | ⏳ ПЛАНИРУЕТСЯ |
| **Логирование чувствительных данных** | ⚠️ Требует ревью кода | ⏳ ПЛАНИРУЕТСЯ |

---

## Как протестировать?

### Тест 1: Шифрование
```kotlin
// Нельзя просто так прочитать userId
val prefs = context.getSharedPreferences("weather_wear_encrypted_prefs", Context.MODE_PRIVATE)
val userId = prefs.getString("user_id", null)
// Result: null или шифрованная строка, не настоящий ID
```

### Тест 2: Управление доступом
```kotlin
// User A попытается получить гардероб User B
authRepository = UserA.login()
val wardrobeB = wardrobeRepository.getItems(userB.id)
// Result: Resource.Error("Доступ запрещён...")
```

### Тест 3: Изоляция компонентов
```kotlin
// Fragment не может создать Repository напрямую
val wardrobe = WardrobeRepositoryImpl(dao) // ❌ Нельзя, если нет authRepository
val wardrobe = AppContainer.provideWardrobeRepository(context) // ✅ Правильно
```

---

## Рекомендации на будущее

1. **Шифрование БД** → Использовать SQLCipher или EncryptedRoom
2. **Rate limiting** → Защита от brute-force атак на auth
3. **HTTPS + Certificate pinning** → Если будет API
4. **Hilt/Dagger** → Более мощный DI фреймворк вместо AppContainer
5. **Obfuscation** → ProGuard/R8 должен быть включен в release build

---

## Файлы для изучения

- 🔐 [EncryptedPreferencesManager.kt](app/src/main/java/com/stepanov_ivan/weatherwearadvisor/security/EncryptedPreferencesManager.kt)
- 🛡️ [AuthRepositoryImpl.kt](app/src/main/java/com/stepanov_ivan/weatherwearadvisor/repository/auth/AuthRepositoryImpl.kt)
- 🛡️ [WardrobeRepositoryImpl.kt](app/src/main/java/com/stepanov_ivan/weatherwearadvisor/repository/wardrobe/WardrobeRepositoryImpl.kt)
- 🏗️ [AppContainer.kt](app/src/main/java/com/stepanov_ivan/weatherwearadvisor/di/AppContainer.kt)
- 📱 [MainActivity.kt](app/src/main/java/com/stepanov_ivan/weatherwearadvisor/MainActivity.kt)
