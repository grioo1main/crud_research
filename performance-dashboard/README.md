# Performance Dashboard - Инструкция по установке

## 📁 Структура проекта

```
performance-dashboard/
├── index.html              ← Главный HTML файл
├── package.json            ← Зависимости проекта
├── vite.config.js          ← Конфиг Vite
├── eslint.config.js        ← Конфиг ESLint
├── src/
│   ├── main.jsx           ← Точка входа React
│   ├── App.js             ← Главный компонент
│   ├── components/        ← React компоненты
│   │   ├── Dashboard.js
│   │   ├── LoadTestPanel.js
│   │   ├── ResponseTimeChart.js
│   │   ├── ThroughputChart.js
│   │   ├── MethodComparison.js
│   │   └── MetricsTable.js
│   ├── services/          ← API сервисы
│   │   └── api.js
│   └── styles/            ← CSS стили
│       └── App.css
```

## 🚀 Быстрый старт (2 варианта)

### Вариант 1: Использовать готовую папку (рекомендуется)

Если у тебя уже есть папка `performance-dashboard` от Vite:

```bash
# 1. Перейди в папку проекта
cd ~/A\ My\ other\ GitHub\ Project/performance-dashboard

# 2. Сделай резервную копию старого src (на всякий случай)
mv src src_backup

# 3. Скопируй новые файлы (замени путь на свой, где лежит скачанная папка)
cp -r /путь/к/скачанной/папке/performance-dashboard/src .
cp /путь/к/скачанной/папке/performance-dashboard/index.html .
cp /путь/к/скачанной/папке/performance-dashboard/package.json .

# 4. Установи зависимости
npm install

# 5. Запусти проект
npm run dev
```

### Вариант 2: Создать проект заново

```bash
# 1. Перейди в папку с проектами
cd ~/A\ My\ other\ GitHub\ Project

# 2. Удали старую папку (или переименуй)
mv performance-dashboard performance-dashboard-old

# 3. Скопируй новую папку (замени путь)
cp -r /путь/к/скачанной/папке/performance-dashboard .

# 4. Перейди в папку
cd performance-dashboard

# 5. Установи зависимости
npm install

# 6. Запусти проект
npm run dev
```

## 📋 Пошаговые команды для копирования

Если ты скачал папку в `Downloads`:

```bash
cd ~/A\ My\ other\ GitHub\ Project/performance-dashboard

# Удаляем старые файлы (кроме node_modules)
rm -rf src public index.html

# Копируем новые файлы
cp -r ~/Downloads/performance-dashboard/src .
cp ~/Downloads/performance-dashboard/index.html .
cp ~/Downloads/performance-dashboard/package.json .

# Устанавливаем зависимости
npm install

# Запускаем
npm run dev
```

## 🔧 Проверка структуры

После копирования у тебя должна быть такая структура:

```bash
# Проверь структуру
tree src/

# Должно вывести:
src/
├── main.jsx
├── App.js
├── components/
│   ├── Dashboard.js
│   ├── LoadTestPanel.js
│   ├── ResponseTimeChart.js
│   ├── ThroughputChart.js
│   ├── MethodComparison.js
│   └── MetricsTable.js
├── services/
│   └── api.js
└── styles/
    └── App.css
```

## 🌐 Настройка API

По умолчанию фронтенд обращается к `http://localhost:8080/api`

Если твой бэкенд работает на другом порту, измени в файле `src/services/api.js`:

```javascript
const API_BASE = 'http://localhost:ТВОЙ_ПОРТ/api';
```

## 🚀 Команды для разработки

```bash
# Запуск dev-сервера
npm run dev

# Сборка для продакшена
npm run build

# Предпросмотр собранного проекта
npm run preview
```

## 🐛 Возможные проблемы

### Ошибка "Cannot find module"
```bash
# Удали node_modules и установи заново
rm -rf node_modules package-lock.json
npm install
```

### Ошибка порта (5173 занят)
```bash
# Запусти на другом порте
npm run dev -- --port 3000
```

### CORS ошибки
Убедись, что твой бэкенд разрешает запросы с `http://localhost:5173`

## 📦 Зависимости проекта

- `axios` - HTTP клиент
- `chart.js` + `react-chartjs-2` - Графики
- `react` + `react-dom` - React
- `vite` - Сборщик

## 🎨 Функционал дашборда

1. **Карточки метрик** - общая статистика (RPS, время отклика, ошибки)
2. **Автообновление** - каждые 5 секунд
3. **Нагрузочное тестирование** - запуск тестов с настройкой
4. **Графики**:
   - Время отклика по методам (линейный)
   - Пропускная способность (столбчатый)
   - Сравнение методов (радар)
5. **Таблица метрик** - детализация по HTTP методам

## ✅ Проверка работы

1. Запусти бэкенд на порту 8080
2. Запусти фронтенд: `npm run dev`
3. Открой `http://localhost:5173`
4. Должен появиться дашборд с темной темой
