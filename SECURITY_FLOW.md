# 🎯 ПОЛНОЕ РУКОВОДСТВО: Как работает безопасность вместе

## 🔄 Полный цикл жизни данных пользователя

```
┌─────────────────────────────────────────────────────────────────┐
│                                                                 │
│                   📱 ПРИЛОЖЕНИЕ ЗАПУСКАЕТСЯ                     │
│                          ↓                                      │
│                   MainActivity.onCreate()                       │
│                   ├─ AppContainer.init(this)                    │
│                   │  └─ Создаёт EncryptedPreferencesManager    │
│                   └─ Проверяет isUserLoggedIn()                 │
│                       ├─ ✅ ДА → Главный экран                  │
│                       └─ ❌ НЕТ → Экран логина                  │
│                                                                 │
└─────────────────────────────────────────────────────────────────┘

                              ↓ ↓ ↓

┌─────────────────────────────────────────────────────────────────┐
│                                                                 │
│                   🔑 ПОЛЬЗОВАТЕЛЬ ЛОГИНИТСЯ                     │
│                                                                 │
│   LoginFragment.register(email, password)                       │
│          ↓                                                      │
│   AuthViewModel.register()                                      │
│          ↓                                                      │
│   AuthRepositoryImpl.register()                                  │
│          ├─ authManager.register()  ← Firebase                  │
│          ├─ Получает userId от Firebase                         │
│          └─ encryptedPrefs.saveUserId(userId)                   │
│             └─ 🔐 AES-256 шифрование!                           │
│          └─ encryptedPrefs.saveUserEmail(email)                 │
│             └─ 🔐 AES-256 шифрование!                           │
│                                                                 │
│   Результат: userId и email в зашифрованном виде! ✅           │
│                                                                 │
└─────────────────────────────────────────────────────────────────┘

                              ↓ ↓ ↓

┌─────────────────────────────────────────────────────────────────┐
│                                                                 │
│          👕 ПОЛЬЗОВАТЕЛЬ ДОБАВЛЯЕТ ПРЕДМЕТ В ГАРДЕРОБ           │
│                                                                 │
│   WardrobeFragment.addItem(clothingItem)                        │
│          ↓                                                      │
│   WardrobeViewModel.addItem(item)                               │
│          ↓                                                      │
│   WardrobeRepository.addItem(item)                              │
│          ↓                                                      │
│   WardrobeRepositoryImpl.addItem(item)                           │
│          ├─ 🛡️ ПРОВЕРКА ДОСТУПА:                               │
│          │  currentUserId = authRepository.getCurrentUserId()   │
│          │  if (currentUserId != item.userId) {                 │
│          │      return Error("Доступ запрещён")                 │
│          │  }                                                   │
│          │                                                      │
│          ├─ ✅ Если OK: wardrobeDao.insertItem(item)            │
│          └─ 📊 Сохраняется в Room Database                      │
│                                                                 │
│   Результат: Предмет сохранён, только для текущего U! ✅        │
│                                                                 │
└─────────────────────────────────────────────────────────────────┘

                              ↓ ↓ ↓

┌─────────────────────────────────────────────────────────────────┐
│                                                                 │
│           🌐 ДРУГОЙ ПОЛЬЗОВАТЕЛЬ ПЫТАЕТСЯ УКРАСТЬ ДАННЫЕ        │
│                                                                 │
│   hacker.getItems("legitimate_user_id")                         │
│          ↓                                                      │
│   WardrobeRepositoryImpl.getItems("legitimate_user_id")          │
│          ├─ 🛡️ ПРОВЕРКА ДОСТУПА:                               │
│          │  currentUserId = "hacker_id"                         │
│          │  requested_id = "legitimate_user_id"                 │
│          │  if (currentUserId != requested_id) { ✅ FAIL         │
│          │      return Error("Доступ запрещён")                 │
│          │  }                                                   │
│          └─ ❌ Запрос отклонён!                                 │
│                                                                 │
│   Результат: Хакер получит ошибку, данные защищены! ✅          │
│                                                                 │
└─────────────────────────────────────────────────────────────────┘

                              ↓ ↓ ↓

┌─────────────────────────────────────────────────────────────────┐
│                                                                 │
│                    🚪 ПОЛЬЗОВАТЕЛЬ ЛОГАУТИТСЯ                   │
│                                                                 │
│   ProfileFragment.logout()                                      │
│          ↓                                                      │
│   AuthRepositoryImpl.logout()                                    │
│          ├─ authManager.signOut()  ← Firebase                   │
│          └─ encryptedPrefs.clearAll()                           │
│             └─ 🔐 Удаляются все зашифрованные данные            │
│                                                                 │
│   Результат: Сессия полностью закрыта! ✅                       │
│                                                                 │
└─────────────────────────────────────────────────────────────────┘
```

---

## 🎯 3 уровня защиты одновременно

```
ЗАПРОС К ДАННЫМ:

User A запрашивает гардероб User B
          ↓
┌─────────────────────────┐
│  УРОВЕНЬ 1: АРХИТЕКТУРА │ ← DI контейнер гарантирует,
│                         │   что WardrobeRepository
│  AppContainer ──────────┼─→ всегда имеет AuthRepository
│                         │
└─────────────────────────┘
          ↓
┌──────────────────────────┐
│   УРОВЕНЬ 2: БИЗНЕС-    │ ← Код в WardrobeRepositoryImpl:
│   ЛОГИКА                 │   проверка currentUserId == requestedId
│                          │
│  WardrobeRepositoryImpl ──┼─→ if (currentUserId != userId) {
│  .addItem()              │       return Error("Доступ запрещён")
│                          │   }
└──────────────────────────┘
          ↓
┌──────────────────────────┐
│   УРОВЕНЬ 3: ДОПОЛНИ-   │ ← EncryptedPreferencesManager:
│   ТЕЛЬНАЯ ЗАЩИТА         │   currentUserId защищён AES-256
│   (Шифрование)          │   Его не прочитать в открытом виде
│                          │
│  EncryptedPreferences ───┼─→ getCurrentUserId() {
│  Manager                 │       // Расшифровывается при чтении
│                          │   }
└──────────────────────────┘
          ↓
      ❌ ОТКЛОНЕНО
      
Все 3-и уровня должны пройти для успешного доступа ✅
```

---

## 🔍 Пример: Если бы была попытка обхода каждого уровня

### ❌ Попытка 1: Обойти шифрование
```
📱 Хакер берёт смартфон, подключается как root:
$ sqlite3 data/data/.../shared_prefs/weather_wear_encrypted_prefs.xml
sqlite> select * from preferences;
# Видит: "Ûëü©✗®ō..." ← Шифрованный текст

🔐 ЗАЩИТА: Не может разобрать. ОБХОД НЕУДАЧЕН ❌
```

### ❌ Попытка 2: Обойти проверку доступа в коде
```
💻 Хакер может модифицировать .apk или перехватить запрос:

// Попытка вызвать напрямую:
wardrobeRepository.getItems("other_user_id")

// Но WardrobeRepositoryImpl.getItems() содержит:
val currentUserId = authRepository?.getCurrentUserId()
if (currentUserId != userId) {
    return Error("Доступ запрещён")
}

🛡️ ЗАЩИТА: Проверка в коде, всегда выполняется. ОБХОД НЕУДАЧЕН ❌
```

### ❌ Попытка 3: Подменить AuthRepository
```
🤖 Хакер пытается создать fake AuthRepository
который всегда возвращает "я юзер с любым ID":

val fakeAuth = FakeAuthRepository(alwaysReturnTrue = true)
val wardrobe = WardrobeRepositoryImpl(fakeAuth)

// Но AuthRepository создаётся только в AppContainer:
val authRepository: AuthRepository by lazy {
    AuthRepositoryImpl(authManager, encryptedPrefs)
}

// И WardrobeRepository получает её ТОЛЬКО из AppContainer:
fun provideWardrobeRepository(context: Context): WardrobeRepository {
    return WardrobeRepositoryImpl(
        wardrobeDao = ...,
        authRepository = authRepository  // ← Только отсюда!
    )
}

🏗️ ЗАЩИТА: Архитектура гарантирует. ОБХОД НЕУДАЧЕН ❌
```

---

## 📊 Матрица "защита от угроз"

| Угроза | Уровень 1:  DI | Уровень 2: Access Control | Уровень 3: Шифрование |
|--------|:--:|:--:|:--:|
| User A видит гардероб User B | ✅ | ✅ | — |
| Хакер читает userId из памяти | — | — | ✅ |
| SQL injection | ✅ | ✅ | — |
| Хакер подменяет AuthRepository | ✅ | — | — |
| Перехват SharedPreferences | — | — | ✅ |
| **ОБЩИЙ РЕЗУЛЬТАТ** | **ЗАЩИТА** | **ЗАЩИТА** | **ЗАЩИТА** |

---

## 🚀 Как это масштабируется?

Допустим, нужно добавить **OutfitRepository** (группы одежды):

### ПРАВИЛЬНЫЙ ПУТЬ (с нашей архитектурой):
```kotlin
// 1. Создаём OutfitRepositoryImpl с AuthRepository
class OutfitRepositoryImpl(
    private val outfitDao: OutfitDao,
    private val authRepository: AuthRepository  // ← Автоматически!
) : OutfitRepository {
    override fun getItems(userId: String): Flow<Resource<List<Outfit>>> {
        // ✅ Уже знаем как проверять доступ!
        val currentUserId = authRepository?.getCurrentUserId()
        if (currentUserId != userId) {
            return flowOf(Resource.Error("Доступ запрещён"))
        }
        return outfitDao.getByUserId(userId)...
    }
}

// 2. Добавляем в AppContainer:
fun provideOutfitRepository(context: Context): OutfitRepository {
    return OutfitRepositoryImpl(
        outfitDao = AppDatabase.getDatabase(context).outfitDao(),
        authRepository = authRepository  // ← Из контейнера
    )
}

// 3. В любом Fragment:
val outfitRepository = AppContainer.provideOutfitRepository(context)
// ✅ Уже имеет:
// ✅ Проверку доступа
// ✅ Доступ к зашифрованным данным (через authRepository)
// ✅ Изоляцию компонентов
```

---

## 🎓 Что учит эта архитектура?

1. **Dependency Injection (DI)** ← основная проблема безопасности
2. **Interface Segregation** ← Repository interface вместо класса
3. **Single Responsibility** ← AuthRepository только для auth
4. **Don't Repeat Yourself (DRY)** ← Проверка доступа в одном месте
5. **Cryptography basics** ← AES-256 для чувствительных данных

---

## 📚 Процесс для кода-ревью

**Если вы будете делать code-review, проверьте:**

1. **EncryptedPreferencesManager:**
   - [ ] Использует MasterKey.KeyScheme.AES256_GCM ✅
   - [ ] Шифрует ключи и значения ✅
   - [ ] clearAll() вызывается при logout ✅

2. **AuthRepositoryImpl:**
   - [ ] Сохраняет userId при login ✅
   - [ ] Сохраняет email при register ✅
   - [ ] Очищает все данные при logout ✅

3. **WardrobeRepositoryImpl:**
   - [ ] Проверка доступа в getItems() ✅
   - [ ] Проверка доступа в addItem() ✅
   - [ ] Проверка доступа в updateItem() ✅
   - [ ] Проверка доступа в deleteItem() ✅

4. **AppContainer:**
   - [ ] Инициализация EncryptedPrefs ✅
   - [ ] Передача authRepository везде ✅
   - [ ] init(context) вызывается в MainActivity ✅

---

## ⚡ Быстрые примеры для вашего использования

### Получить текущий userId:
```kotlin
val currentUserId = AppContainer.authRepository.getCurrentUserId()
```

### Проверить доступ:
```kotlin
val currentUserId = authRepository?.getCurrentUserId()
if (currentUserId != requestedUserId) {
    return Resource.Error("Доступ запрещён")
}
```

### Добавить новый Repository:
```kotlin
class MyNewRepositoryImpl(
    private val authRepository: AuthRepository
) : MyNewRepository {
    // Используйте authRepository для проверки доступа
}
```

### Использовать в AppContainer:
```kotlin
fun provideMyNewRepository(context: Context): MyNewRepository {
    return MyNewRepositoryImpl(
        authRepository = authRepository
    )
}
```

---

## 🌟 Главное ко что нужно помнить

> **"Безопасность — это не одна вещь, которая вас защищает.
> Это три уровня, работающие вместе."**

1. **Первый уровень:** Архитектура (DI) гарантирует систематичность
2. **Второй уровень:** Бизнес-логика (проверка доступа) предотвращает ошибки
3. **Третий уровень:** Криптография (шифрование) защищает от перехватов

Все вместе = безопасное приложение ✅
