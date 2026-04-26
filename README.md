# Weather Wear Advisor (Android)

Приложение для подбора одежды на основе погодных условий.

---

# 📖 Руководства

Подробнее о выборе локации для погоды: [LOCATION_GUIDE.md](LOCATION_GUIDE.md)

---

# 🛠️ Требования

Перед сборкой убедитесь, что у вас установлено:

- JDK 11+
- Android SDK
- Android Studio (рекомендуется)
- Подключённый телефон или эмулятор

---

# 📦 Сборка APK (Windows)

## 1. Открыть терминал

Перейдите в корневую папку проекта:

```bash
cd путь_к_проекту
```

---

## 2. Сборка Debug APK

```bash
gradlew.bat assembleDebug
```

После успешной сборки APK будет находиться здесь:

```text
app/build/outputs/apk/debug/app-debug.apk
```

---

## 3. Сборка Release APK

```bash
gradlew.bat assembleRelease
```

APK появится:

```text
app/build/outputs/apk/release/app-release.apk
```

---

# 📱 Установка на устройство

## Вариант 1 — через ADB

### 1. Включить на телефоне:

- Режим разработчика
- USB Debugging

---

### 2. Проверить подключение:

```bash
adb devices
```

---

### 3. Установить APK:

```bash
adb install -r app/build/outputs/apk/debug/app-debug.apk
```

---

## Вариант 2 — вручную

1. Скопировать APK на телефон
2. Открыть через файловый менеджер
3. Нажать "Установить"

---

# ⚠️ Возможные проблемы

## adb не найден

Добавьте в PATH:

```text
C:\Users\USERNAME\AppData\Local\Android\Sdk\platform-tools
```

---

## INSTALL_FAILED_ALREADY_EXISTS

```bash
adb install -r app-debug.apk
```

---

## INSTALL_FAILED_VERSION_DOWNGRADE

Удалите старую версию приложения с телефона.

---



# 👨‍💻 Автор

Ivan Stepanov
