# QA API Petstore Tests

Фреймворк автотестов для REST API [Swagger Petstore (OpenAPI 3)](https://petstore3.swagger.io/), написанный на **Java 21** с использованием **REST-Assured** и **JUnit 5**.

Проект демонстрирует построение автотестового фреймворка «с нуля» по слоистой архитектуре, принятой в промышленных QA-командах: клиенты API, фабрики тестовых данных, модели, кастомные ассерты, JSON Schema валидация, E2E-сценарии, отчётность Allure и CI/CD на GitHub Actions.

## Стек технологий

| Категория            | Инструменты                                      |
|-----------------------|---------------------------------------------------|
| Язык / сборка          | Java 21, Gradle (Kotlin DSL)                      |
| HTTP-клиент            | REST-Assured 6                                    |
| Тестовый фреймворк     | JUnit 5 (Jupiter)                                 |
| Ассерты                 | AssertJ, кастомные domain-ассерты                 |
| Валидация схем          | rest-assured json-schema-validator               |
| Тестовые данные         | JavaFaker, фабрики (Factory pattern)             |
| Отчётность               | Allure Report (allure-junit5, allure-rest-assured) |
| Генерация моделей       | Lombok, Jackson (jackson-databind)                |
| Конфигурация            | dotenv-java (`.env`) + системные свойства         |
| Качество кода            | Checkstyle                                         |
| Тестовое окружение       | Docker (swaggerapi/petstore3)                     |
| CI/CD                     | GitHub Actions + автопубликация Allure-отчёта на GitHub Pages |

## Архитектура проекта

```
src/test/java/apiTests/
├── base/            # HTTP-клиенты по доменам API (Pet, Store, User) + базовый клиент с обёртками над REST-Assured
├── specs/           # RequestSpecification (базовые URI, заголовки, content-type)
├── testsConfig/     # Чтение конфигурации из .env / системных свойств
├── models/          # POJO-модели запросов/ответов (Pet, Order, User и т.д.)
├── factories/        # Генерация тестовых данных (PetFactory, OrderFactory, UserFactory)
├── asserts/          # Переиспользуемые доменные ассерты (PetAssertions, StoreAssertions, UserAssertions)
├── scenarios/        # Сквозные (E2E) сценарии полного жизненного цикла сущности
└── tests/            # Позитивные/негативные тест-кейсы по каждому домену API
```

Такое разделение изолирует HTTP-логику, тестовые данные, проверки и сами тест-кейсы друг от друга, упрощая поддержку и расширение набора тестов.

## Что покрыто тестами

- **Pet** — создание, получение по ID, поиск по статусу/тегу, обновление (JSON и form-data), удаление, загрузка изображения, обработка невалидного JSON и 404-кейсов
- **Store** — создание заказа, получение инвентаря, получение/удаление заказа, 404-кейсы
- **User** — создание (в т.ч. batch через `createWithList`), получение по username, обновление, удаление, login/logout, 404-кейсы
- **E2E-сценарии** — полный жизненный цикл заказа и пользователя (создание → чтение → изменение → удаление → проверка отсутствия)

Каждый тест снабжён Allure-аннотациями (`@Epic`, `@Feature`, `@Story`, `@Severity`, `@Owner`) для читаемой отчётности, а ответы API валидируются по JSON Schema.

Тесты размечены тегами, что позволяет запускать разные наборы:
- `Smoke` — критичный минимум
- `Positive` / `Negative` — полный регресс
- `E2E` — сквозные сценарии
- `Bug` — тесты, документирующие найденные баги/несоответствия окружения (с описанием причины в `@Description`)

## Известные проблемы

4 теста помечены тегом `Bug` и связаны с GitHub Issues. Они документируют несоответствия между OpenAPI-спецификацией и реальным поведением Docker-образа `swaggerapi/petstore3`:

- Загрузка изображения питомца — ожидается успешный ответ, получается ошибка
- Удаление несуществующих сущностей (Pet, Order, User) — ожидается 404, API возвращает другой статус

Тесты не пропускаются (`@Disabled`), а работают на публичном petstore, фиксируя расхождение. Подробности — в `@Description` и `@Issue` каждого теста.

## Запуск тестов

### Требования
- JDK 21
- Docker (для локального инстанса Petstore)

### 1. Поднять тестовое окружение

```bash
docker compose up -d
```

Поднимет `swaggerapi/petstore3` на `localhost:8080`.

### 2. Настроить конфигурацию (опционально)

```bash
cp .env.example .env
```

### 3. Запустить тесты

```bash
./gradlew clean check          # все тесты + checkstyle
./gradlew smokeTest            # только smoke-набор
./gradlew regressionTest       # полный регресс (Positive + Negative, включая Smoke)
./gradlew e2eTest              # только E2E-сценарии
```

> **Примечание:** теги `Smoke`, `Positive` и `Negative` могут совпадать на одних и тех же тестах. Задача `regressionTest` запускает все тесты с тегами `Positive` или `Negative`, поэтому smoke-тесты также включены в полный регресс.

### 4. Сгенерировать и открыть Allure-отчёт

```bash
./gradlew allureReport
./gradlew allureServe
```

## CI/CD

Пайплайн состоит из двух джобов в GitHub Actions (`ci.yml`):

### Джоб `test`

Запускается при каждом push/pull request в `main` (а также при push в `api-test` и ручном запуске):

1. Поднимает Petstore как service-контейнер
2. Прогоняет `./gradlew clean check --continue`
3. Генерирует Allure-отчёт и JUnit-результаты как артефакты (срок хранения — 14 дней)

### Джоб `publish-report`

Запускается только при push в `main` (после успешного завершения `test`):

1. Скачивает Allure-отчёт
2. Публикует его на GitHub Pages

## Автор

Никита Ткаченко
