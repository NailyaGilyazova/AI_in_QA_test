# Astra QA Agent

**Astra QA Agent** - демонстрационная AI QA-система, которая превращает короткий чек-лист в тестовые сценарии, JSON test cases, Selenium Page Object, UI-автотесты, Allure-результаты, bug report и итоговый отчет агента.

Проект сделан как инженерный прототип AI-assisted QA automation. Он показывает, как связать LLM, Selenium/TestNG, Maven, Allure, PII-защиту и агентский оркестратор со skills.

## Что делает система

На вход Astra получает:

- `checklist.txt` - короткий QA-чек-лист;
- `application.url` из `application.properties` - URL тестируемой страницы;
- LLM-настройки и LLM-профили;
- задачу пользователя в agent mode.

Основной маршрут:

1. Собрать prompt из чек-листа.
2. Проверить prompt на PII и при необходимости замаскировать чувствительные данные.
3. Сгенерировать тестовые сценарии через LLM.
4. Сгенерировать JSON test cases.
5. Открыть страницу в headless Chrome и снять rendered DOM.
6. Сгенерировать Java Page Object.
7. Сгенерировать Selenium/TestNG UI-тесты.
8. Провалидировать сгенерированный Java-код.
9. Запустить сгенерированные тесты.
10. Собрать Allure results.
11. Сгенерировать bug report в JSON.
12. Сформировать итоговое agent summary.

## Текущая архитектура

В проекте есть два режима запуска.

### Agent Mode

Основной режим для развития проекта.

Entry point:

`src/main/java/agent/QaAgentMain.java`

Агент устроен как последовательный skill-based orchestrator:

```text
QaAgentMain
  -> QaAgent
      -> AgentSkillRegistry.defaultSkills()
          -> AgentSkill.execute(AgentContext)
```

Перед выполнением skills агент определяет intent задачи через `AgentIntentResolver`.
Intent влияет на `canHandle()` каждого skill и позволяет запускать не только полный QA-цикл,
но и ограниченные маршруты:

- `FULL_QA_CYCLE` - полный маршрут от сценариев до summary.
- `TESTCASES_ONLY` - генерация сценариев и JSON test cases без UI-автотестов.
- `SCENARIOS_ONLY` - генерация только тестовых сценариев.
- `PII_ONLY` - сборка prompt, PII scan/masking и итоговый summary.

Ключевые классы ядра:

```text
src/main/java/agent/
  AgentContext.java        # состояние одного запуска агента
  AgentIntent.java         # тип маршрута запуска агента
  AgentIntentResolver.java # определение маршрута по задаче пользователя
  AgentRunResult.java      # итог выполнения агента
  AgentSkill.java          # контракт skill
  AgentSkillRegistry.java  # порядок skills
  AgentStepResult.java     # результат одного шага
  AgentStepStatus.java     # статус шага
  QaAgent.java             # исполнитель skills
  QaAgentMain.java         # CLI entry point
```

Реализации skills лежат отдельно:

```text
src/main/java/agent/skills/
  BuildScenariosPromptSkill.java
  PiiScanSkill.java
  ScenarioGenerationSkill.java
  TestcaseGenerationSkill.java
  UiTestGenerationSkill.java
  TestRunSkill.java
  AllureResultsSkill.java
  BugReportSkill.java
  SummarySkill.java
```

### Pipeline Mode

Классический линейный pipeline без skill-оркестратора.

Entry point:

`src/main/java/pipeline/PipelineMain.java`

Этот режим сохранен для сравнения и демонстрации более простого подхода.

## Agent Skills

Текущий порядок skills задается в:

`src/main/java/agent/AgentSkillRegistry.java`

Список skills:

- `BuildScenariosPromptSkill` - собирает prompt из `checklist.txt`.
- `PiiScanSkill` - проверяет prompt на PII и маскирует найденные данные.
- `ScenarioGenerationSkill` - генерирует тестовые сценарии через LLM.
- `TestcaseGenerationSkill` - генерирует JSON test cases и валидирует структуру.
- `UiTestGenerationSkill` - снимает rendered DOM, генерирует Page Object и UI-тесты.
- `TestRunSkill` - запускает `org.demo.generated.GeneratedUiTest`.
- `AllureResultsSkill` - собирает Allure results.
- `BugReportSkill` - генерирует `bug_report.json`.
- `SummarySkill` - формирует итоговый отчет агента.

Чтобы добавить новый skill:

1. Создать класс в `src/main/java/agent/skills/`.
2. Реализовать интерфейс `AgentSkill`.
3. Использовать `AgentContext` для чтения и записи состояния.
4. Добавить skill в `AgentSkillRegistry.defaultSkills()`.

Минимальный шаблон:

```java
package agent.skills;

import agent.AgentContext;
import agent.AgentSkill;
import agent.AgentStepResult;

public class CustomSkill implements AgentSkill {
    @Override
    public String name() {
        return "CUSTOM_SKILL";
    }

    @Override
    public AgentStepResult execute(AgentContext context) {
        return AgentStepResult.passed(name(), "Done");
    }
}
```

## Остальные пакеты

```text
src/main/java/client/
  LlmClient.java           # выбор настроенного LLM-провайдера
  LlmProfile.java          # имена LLM-профилей
  GigaChatLlmClient.java   # GigaChat SDK client
  OllamaLlmClient.java     # локальный Ollama HTTP client

src/main/java/config/
  ApplicationConfig.java   # чтение application.properties

src/main/java/generator/
  GeneratedTestSupport.java
  JavaCodeExtractor.java
  JavaCodePostProcessor.java
  PageObjectContractAnalyzer.java
  PageObjectGenerator.java
  TestGenerator.java

src/main/java/parser/
  JsonExtractor.java
  TestcasesParser.java

src/main/java/security/
  PiiMasker.java
  PiiPatterns.java
  PiiReport.java
  PiiScanner.java

src/main/java/ui/
  UiSnapshotClient.java    # rendered DOM snapshot через headless Chrome

src/main/java/util/
  FilesUtil.java
  TestRunExecutor.java
```

## LLM-конфигурация

Настройки находятся в:

`application.properties`

Базовая конфигурация GigaChat:

```properties
llm.provider=gigachat
gigachat.model=GigaChat-3-Ultra
gigachat.apiUrl=https://api.giga.chat/v1
llm.systemPrompt=You are a QA engineer. Return structured output.
gigachat.scope=GIGACHAT_API_PERS
gigachat.verifySslCerts=false
gigachat.connectTimeoutSeconds=30
gigachat.readTimeoutSeconds=120
```

`gigachat.scope` принимает `GIGACHAT_API_PERS`, `GIGACHAT_API_B2B` или
`GIGACHAT_API_CORP`. Отключение проверки SSL оставлено совместимым с примером
SDK, но для рабочей среды рекомендуется установить корневой сертификат НУЦ
Минцифры и включить `gigachat.verifySslCerts=true`.

### Локальная модель через Ollama

Сначала установите Ollama, скачайте выбранную модель и убедитесь, что локальный
сервер доступен. Например:

```zsh
ollama pull qwen3:latest
ollama serve
```

Чтобы переключиться с GigaChat на Ollama, в `application.properties`
закомментируйте активные строки GigaChat и раскомментируйте строки Ollama:

```properties
#llm.provider=gigachat
#gigachat.model=GigaChat-3-Ultra

llm.provider=ollama
ollama.model=qwen3:latest
ollama.baseUrl=http://localhost:11434
ollama.connectTimeoutSeconds=10
ollama.readTimeoutSeconds=300
```

В режиме `ollama` переменная `GIGACHAT_AUTH_KEY` не требуется. Модель должна
быть заранее скачана командой `ollama pull <имя-модели>`.

`PipelineMain`, agent skills и генераторы используют общий `LlmClient` и не
содержат выбора конкретного провайдера. Для переключения между GigaChat и
Ollama достаточно изменить только `llm.provider` и соответствующее свойство
модели в `application.properties`; правки Java-кода не требуются. При старте
Pipeline выводит фактически выбранные provider и model.

## LLM-профили

LLM-вызовы в Agent Mode разделены по профилям. Это позволяет отдельно
настраивать температуру, лимиты, `topP`, модель и system prompt для
каждого этапа. `PipelineMain` оставлен как упрощенный legacy-режим: его
прямые вызовы генерации сценариев, test cases и bug report используют
профиль `default`.

Общие значения:

```properties
llm.default.temperature=0.2
llm.default.maxTokens=4096
llm.default.topP=1.0
```

Текущие профили:

```properties
llm.scenarios.temperature=0.4
llm.scenarios.maxTokens=4096

llm.testcases.temperature=0.1
llm.testcases.maxTokens=4096

llm.pageObject.temperature=0.1
llm.pageObject.maxTokens=8192

llm.autotests.temperature=0.1
llm.autotests.maxTokens=8192

llm.bugReport.temperature=0.1
llm.bugReport.maxTokens=4096

llm.summary.temperature=0.4
llm.summary.maxTokens=4096
```

Доступные имена профилей заданы в:

`src/main/java/client/LlmProfile.java`

Профили:

- `default`
- `scenarios`
- `testcases`
- `pageObject`
- `autotests`
- `bugReport`
- `summary`

Для каждого профиля можно задать:

- `temperature`
- `maxTokens`
- `topP`
- `model`
- `systemPrompt`

Пример переопределения модели только для Page Object:

```properties
llm.pageObject.model=GigaChat-3-Ultra
llm.pageObject.temperature=0.1
```

## Быстрый старт

### 1. Требования

- Java 17+
- Maven 3.9+
- Google Chrome
- интернет-доступ и ключ GigaChat либо установленный локальный Ollama

### 2. Установить ключ GigaChat (не требуется для Ollama)

```zsh
export GIGACHAT_AUTH_KEY='ваш_ключ_авторизации'
```

### 3. Указать URL

```properties
application.url=https://www.saucedemo.com/
```

### 4. Подготовить чек-лист

Файл:

`checklist.txt`

Пример:

```text
Страница логина https://www.saucedemo.com/

Чек-лист:
1. Отображаются поля username и password, а также кнопка Log In
2. Успешный вход с валидными данными
3. Ошибка при неверном пароле
4. Ошибка при пустом поле username
```

### 5. Запустить Agent Mode

Интерактивно:

```zsh
mvn compile exec:java -Dexec.mainClass=agent.QaAgentMain
```

После запуска ввести задачу:

```text
Проверь логин на saucedemo.com по checklist.txt
```

Одной командой:

```zsh
mvn compile exec:java -Dexec.mainClass=agent.QaAgentMain -Dexec.args="Проверь логин на saucedemo.com по checklist.txt"
```

## Команды

Agent Mode:

```zsh
mvn compile exec:java -Dexec.mainClass=agent.QaAgentMain
```

Agent Mode подбирает маршрут по тексту задачи:

```zsh
mvn compile exec:java -Dexec.mainClass=agent.QaAgentMain -Dexec.args="Сгенерируй только тесткейсы без автоматизации"
```

В этом режиме агент остановится после `generated/testcases.json` и не будет генерировать UI-автотесты, запускать Maven, собирать Allure или bug report.

```zsh
mvn compile exec:java -Dexec.mainClass=agent.QaAgentMain -Dexec.args="Проверь checklist.txt на PII"
```

В этом режиме агент выполнит только сборку prompt, PII scan/masking и итоговый summary.

Pipeline Mode:

```zsh
mvn compile exec:java
```

Запустить тесты:

```zsh
mvn test
```

Собрать Allure HTML-report:

```zsh
mvn allure:report
```

## Генерируемые Java-файлы

Pipeline и Agent Mode создают или перезаписывают:

```text
src/test/java/org/demo/generated/support/GeneratedBaseUiTest.java
src/test/java/org/demo/generated/pages/GeneratedPageObject.java
src/test/java/org/demo/generated/GeneratedUiTest.java
```

## Артефакты в generated/

Набор файлов зависит от выбранного intent. Полный QA-цикл может создать
следующие артефакты; например, `scenarios_prompt_masked.txt` появляется
только при обнаружении PII.

```text
generated/scenarios_prompt.txt
generated/scenarios_prompt_masked.txt
generated/pii_report.txt
generated/scenarios_raw.json
generated/scenarios.txt

generated/testcases_prompt.txt
generated/testcases_raw.json
generated/testcases_llm.txt
generated/testcases.json

generated/page_snapshot.html
generated/page_object_prompt.txt
generated/page_object_raw.json
generated/page_object_llm.txt

generated/autotests_prompt.txt
generated/autotests_raw.json
generated/autotests_llm.txt

generated/test_run.log
generated/allure_report.log
generated/allure_results.txt

generated/bug_report_prompt.txt
generated/bug_report_raw.json
generated/bug_report_llm.txt
generated/bug_report.json

generated/agent_trace.txt
generated/agent_summary_prompt.txt
generated/agent_summary_raw.json
generated/agent_summary.txt
```

Самые полезные файлы для отладки:

- `generated/agent_trace.txt`
- `generated/page_snapshot.html`
- `generated/page_object_llm.txt`
- `generated/autotests_llm.txt`
- `generated/test_run.log`
- `generated/bug_report.json`
- `generated/agent_summary.txt`

## Валидация генерации

Проект специально добавляет ограничения вокруг LLM-ответов:

- JSON извлекается и валидируется через Jackson.
- Java-код извлекается из ответа LLM и нормализуется.
- Page Object должен иметь правильный package, имя класса, конструктор и `open()`.
- Page Object не должен отдавать наружу public `By` поля.
- UI-тест должен наследоваться от `GeneratedBaseUiTest`.
- UI-тест не должен напрямую использовать `driver`, `By`, `ExpectedConditions` и локаторы.
- UI-тест может вызывать только публичные методы сгенерированного Page Object.
- Для Page Object и UI-тестов есть retry-попытки, если LLM нарушила контракт.

## PII и безопасность

Перед первым LLM-вызовом агент проверяет prompt на базовые чувствительные данные:

- email
- phone
- password
- API secret
- bearer token
- bank card
- IBAN
- SNILS

Если PII найдены:

- создается `generated/pii_report.txt`;
- создается `generated/scenarios_prompt_masked.txt`;
- в LLM отправляется замаскированный prompt.

Ограничение: сейчас PII-защита применяется к первому prompt на основе чек-листа. Для production-сценария ее нужно расширить на все LLM-входы.

## Allure

Allure results:

`target/allure-results`

Настройка:

`src/test/resources/allure.properties`

HTML-report:

```zsh
mvn allure:report
```

Результат:

`target/site/allure-maven-plugin`

## Troubleshooting

### Не задан ключ GigaChat API

Установите ключ авторизации, полученный в настройках проекта GigaChat API:

```zsh
export GIGACHAT_AUTH_KEY='ваш_ключ_авторизации'
```

После `export` запускайте Maven в той же сессии shell. В режиме Ollama
ключ не нужен.

### Ollama недоступна или модель не найдена

Проверьте, что сервер запущен, а имя в `ollama.model` точно совпадает
с локально скачанной моделью:

```zsh
ollama list
ollama serve
```

### Не генерируется Page Object или UI-тест

Проверить:

- `generated/page_snapshot.html`
- `generated/page_object_llm.txt`
- `generated/autotests_llm.txt`
- `generated/agent_trace.txt`

Частые причины:

- страница не открывается по `application.url`;
- headless Chrome не может снять rendered DOM;
- LLM не соблюдает Java-контракт;
- не хватает `llm.pageObject.maxTokens` или `llm.autotests.maxTokens`.
- долгий LLM-ответ превышает `gigachat.readTimeoutSeconds` (по умолчанию 120 секунд).

### UI-тесты не стартуют

Проверить:

- установлен ли Google Chrome;
- доступен ли headless Chrome;
- есть ли интернет-доступ к тестируемому URL;
- не блокирует ли среда запуск браузера.

### Selenium предупреждает про CDP

Selenium может писать warning вида `Unable to find CDP implementation matching ...`. Для текущего сценария это предупреждение не блокирует обычные WebDriver-действия. Если понадобится DevTools/CDP-функциональность, нужно добавить совместимую `selenium-devtools-vXX` зависимость под версию Chrome.

## Ограничения

- Это демонстрационный прототип, не production QA platform.
- Агент выполняет skills последовательно, без динамического planning loop.
- Intent задачи определяется rule-based эвристиками по текстовым маркерам.
- Качество результата зависит от чек-листа, prompt-файлов, DOM и выбранной модели.
- Bug report строится только по test log и Allure results.
- UI-тесты чувствительны к изменениям интерфейса.
- PII-защита пока покрывает только первый prompt.
- Секреты нельзя хранить в коде, README, checklist или prompt-файлах.
