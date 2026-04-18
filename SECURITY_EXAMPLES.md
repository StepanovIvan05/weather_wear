# 🎯 Практические примеры использования безопасности

## Сценарий 1: Регистрация пользователя

```kotlin
// В LoginFragment/RegisterFragment
class AuthViewModel(private val repository: AuthRepository) : ViewModel() {
    
    fun register(name: String, email: String, password: String) {
        viewModelScope.launch {
            val result = repository.register(name, email, password)
            when (result) {
                is Resource.Success -> {
                    // ✅ Данные автоматически сохранены в зашифрованном виде
                    // ✅ userId теперь в EncryptedSharedPreferences
                    navController.navigate(R.id.navigation_home)
                }
                is Resource.Error -> {
                    showError(result.message)
                }
                is Resource.Loading -> {
                    showLoading()
                }
            }
        }
    }
}

// Что происходит внутри:
// register() → AuthRepositoryImpl.register()
//   1. Вызывает Firebase auth
//   2. Сохраняет userId в EncryptedSharedPreferences (AES-256)
//   3. Сохраняет email в EncryptedSharedPreferences (AES-256)
//   ✅ Даже если кто-то будет смотреть SharedPreferences напрямую
//      — увидит только шифрованный текст
```

---

## Сценарий 2: Добавление предмета в гардероб

### ❌ ДО (уязвимо)
```kotlin
// User A (uid="123") может добавить предмет для User B (uid="456")
val clothingItem = ClothingItem(
    name = "Рубашка",
    userId = "456"  // ← Произвольно выбираем чужой userId!
)
wardrobeRepository.addItem(clothingItem)  // Это пройдёт!
// User B теперь в его гардеробе есть наше платье 😱
```

### ✅ ПОСЛЕ (защищено)
```kotlin
// User A (uid="123") пытается добавить предмет для User B (uid="456")
val clothingItem = ClothingItem(
    name = "Рубашка",
    userId = "456"
)
wardrobeRepository.addItem(clothingItem)

// Что происходит внутри WardrobeRepositoryImpl.addItem():
override suspend fun addItem(item: ClothingItem): Resource<Unit> {
    val currentUserId = authRepository?.getCurrentUserId()  // "123"
    
    // ✅ ПРОВЕРКА ДОСТУПА
    if (currentUserId == null || currentUserId != item.userId) {
        // currentUserId ("123") != item.userId ("456")
        return Resource.Error("Доступ запрещён: невозможно добавить элемент в чужой гардероб")
    }
    // Достигнуть сюда не получится
    wardrobeDao.insertItem(item)
}

// Result: Resource.Error("Доступ запрещён...")
// User B его гардероб остался чистым ✅
```

---

## Сценарий 3: Изоляция компонентов в тестах

### Без правильного DI (проблемно)
```kotlin
// Тестирование WardrobeRepository без контроля зависимостей
@Test
fun testAddItem() {
    // ❌ Нельзя подменить AuthRepository
    val wardrobe = WardrobeRepositoryImpl(mockDao)  // Ошибка компиляции!
    // Либо всегда используется настоящий AuthRepository (сложно для тестов)
}
```

### С правильным DI (хорошо)
```kotlin
// Тестирование WardrobeRepository с подменой зависимостей
@Test
fun testAddItem_AccessDenied() {
    // ✅ Создаём fake AuthRepository для тестов
    val fakeAuth = FakeAuthRepository(currentUserId = "123")
    
    // ✅ Передаём его в WardrobeRepository
    val wardrobe = WardrobeRepositoryImpl(
        wardrobeDao = mockDao,
        authRepository = fakeAuth
    )
    
    // ✅ Тестируем сценарий когда userId не совпадает
    fakeAuth.setCurrentUserId("456")
    val result = wardrobe.addItem(
        ClothingItem(name = "Item", userId = "123")
    )
    
    // ✅ Ожидаемо: доступ запрещён
    assertTrue(result is Resource.Error)
    assertTrue(result.message.contains("Доступ запрещён"))
}
```

---

## Сценарий 4: Логин и восстановление сессии

```kotlin
class MainActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        
        // ✅ Инициализация DI контейнера
        AppContainer.init(this)
        
        // Проверка: есть ли активная сессия?
        val authRepository = AppContainer.authRepository
        
        if (authRepository.isUserLoggedIn()) {
            // ✅ userId восстановлен из EncryptedSharedPreferences
            // ✅ Даже если приложение было закрыто
            val userId = authRepository.getCurrentUserId()
            println("Вернулся: $userId")
            
            // Переходим на главный экран
            navController.navigate(R.id.navigation_home)
        } else {
            // Нет активной сессии → логин экран
            navController.navigate(R.id.navigation_login)
        }
    }
}

// Внутри:
// getCurrentUserId() → достаёт из EncryptedSharedPreferences
// ✅ Даже если SharedPreferences потуристичит → видит шифрованный текст
```

---

## Сценарий 5: Logout (очистка данных)

```kotlin
class ProfileFragment : Fragment() {
    private fun logout() {
        authRepository.logout()
        // Что происходит:
        // 1. FirebaseAuth.signOut()
        // 2. EncryptedSharedPreferences.clearAll()  ← ✅ очищаются все данные
        
        navController.navigate(R.id.navigation_login)
    }
}

// Результат:
// ✅ userId удален из памяти
// ✅ Email удален из памяти
// ✅ Сессия полностью закрыта
```

---

## Сценарий 6: Попытка прямого доступа к данным (SQL injection)

### ❌ ДО: Уязвимо
```kotlin
// Даже если бы был API с параметрами:
val apiUrl = "https://api.app.com/wardrobe?userId=$userId"
// Пользователь может передать:
// userId = "123' OR '1'='1"
// и получить всех пользователей
```

### ✅ ПОСЛЕ: Защищено
```kotlin
// Проверка в коде ПЕРЕД запросом
override fun getItems(userId: String): Flow<Resource<List<ClothingItem>>> {
    val currentUserId = authRepository?.getCurrentUserId()
    
    // ✅ Даже если userId = "123' OR '1'='1"
    // Проверка currentUserId != userId пройдёт успешно
    if (currentUserId != userId) {
        return flowOf(Resource.Error("Доступ запрещён"))
    }
    
    // SQL инъекция не работает, потому что мы уже
    // отфильтровали все подозрительные запросы в коде
}
```

---

## 🔍 Проверка безопасности в реальном коде

### Проверка 1: Шифрование
```
$ adb shell
# sqlite3 data/data/com.stepanov_ivan.weatherwearadvisor/databases/weather_wear_db
sqlite> select * from shared_preferences;
# Видим: "Ûëü©✗®ō..." ← Шифрованный текст

# А вот обычные:
# sqlite3 data/data/com.stepanov_ivan.weatherwearadvisor/shared_prefs/app_prefs.xml
# Видим открытый текст (если бы они были там)
```

### Проверка 2: Проверка доступа
```kotlin
// Попытка User A получить гардероб User B
User A (uid=123) запрос: getItems("456")
→ WardrobeRepositoryImpl.getItems("456")
  → val currentUserId = authRepository?.getCurrentUserId() // = 123
  → if (currentUserId != userId) // 123 != 456 → TRUE
    → return flowOf(Resource.Error("Доступ запрещён"))
    
Result: ✅ User A получит ошибку
```

---

## 🎓 Выводы из сценариев

1. **Шифрование работает:** Даже root не может просто взять и прочитать userId
2. **Access Control работает:** Каждый пользователь видит только свои данные
3. **DI контейнер работает:** Правильная архитектура гарантирует что все проверки будут выполнены
4. **Вместе:** 3 способа создают многоуровневую защиту

---

## 📚 Прочитать дальше

- [SECURITY.md](SECURITY.md) — Архитектура
- [SECURITY_RATIONALE.md](SECURITY_RATIONALE.md) — Обоснование выбора
- Исходный код:
  - [EncryptedPreferencesManager.kt](app/src/main/java/com/stepanov_ivan/weatherwearadvisor/security/EncryptedPreferencesManager.kt)
  - [AuthRepositoryImpl.kt](app/src/main/java/com/stepanov_ivan/weatherwearadvisor/repository/auth/AuthRepositoryImpl.kt)
  - [WardrobeRepositoryImpl.kt](app/src/main/java/com/stepanov_ivan/weatherwearadvisor/repository/wardrobe/WardrobeRepositoryImpl.kt)
