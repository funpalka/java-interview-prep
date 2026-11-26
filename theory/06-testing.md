# Тестирование и Mockito

> Вопросы: 34, 35, 36.

## 34. Unit-тест vs интеграционный тест

<details>
<summary>Показать ответ</summary>

### Unit vs integration vs end-to-end

**Unit test** проверяет небольшую единицу поведения (часто метод/класс) в контролируемом окружении. Зависимости подменяются fake/stub/mock там, где это ускоряет и делает тест детерминированным. **Integration test** проверяет взаимодействие реальных компонентов — БД, HTTP-клиента, файловой системы, Spring-контекста, очереди. **E2E** проходит целый пользовательский сценарий через публичные границы системы.

| Критерий | Unit | Integration | E2E |
|---|---|---|---|
| Скорость | обычно высокая | ниже | самая высокая стоимость |
| Что ловит | ошибка бизнес-правил и границ | несовместимость модулей, SQL, транзакции, настройки | сбой всей цепочки |
| Зависимости | изолированные/подменяемые | реальные в нужных местах | почти production-like |
| Цена ошибок | минимальная, точная диагностика | дополнительная инфраструктура | сложнее определить причину |

**Тестовая пирамида:** много быстрых unit, меньше интеграционных, ещё меньше E2E — это эвристика, а не фиксированное соотношение. Тестируй **наблюдаемое поведение**, а не приватную реализацию.

### Минимальный Maven (Java 17, JUnit 5 + Mockito)

```xml
<properties>
  <maven.compiler.release>17</maven.compiler.release>
  <project.build.sourceEncoding>UTF-8</project.build.sourceEncoding>
</properties>
<dependencies>
  <dependency>
    <groupId>org.junit.jupiter</groupId>
    <artifactId>junit-jupiter</artifactId>
    <version>5.11.4</version>
    <scope>test</scope>
  </dependency>
  <dependency>
    <groupId>org.mockito</groupId>
    <artifactId>mockito-core</artifactId>
    <version>5.15.2</version>
    <scope>test</scope>
  </dependency>
</dependencies>
<build><plugins><plugin>
  <groupId>org.apache.maven.plugins</groupId>
  <artifactId>maven-surefire-plugin</artifactId>
  <version>3.5.2</version>
</plugin></plugins></build>
```

**Версии выше — воспроизводимый учебный пример, а не утверждение о последних релизах.** Текущий проект уже использует JUnit 5, но Mockito в его `pom.xml` не подключён: зависимость нужна только при переносе Mockito-примеров в реальные тесты. `mvn test` запускает unit-тесты через Surefire. Для integration tests часто используют отдельные профили/`maven-failsafe-plugin`, Testcontainers и реальные сервисы.

### JUnit 5: Arrange → Act → Assert

```java
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

class PriceCalculator {
    int finalPrice(int price, int discount) {
        if (price < 0 || discount < 0 || discount > price)
            throw new IllegalArgumentException("invalid price");
        return price - discount;
    }
}
class PriceCalculatorTest {
    private final PriceCalculator calc = new PriceCalculator();

    @Test void subtractsDiscount() {
        int result = calc.finalPrice(100, 20);
        assertEquals(80, result);
    }
    @Test void rejectsInvalidDiscount() {
        assertThrows(IllegalArgumentException.class,
            () -> calc.finalPrice(10, 20));
    }
}
```

**Что ещё знать:** `@BeforeEach`/`@AfterEach`, `@ParameterizedTest` с `@CsvSource`, `assertAll`, `assertTimeout`, `assertThrows`, `assertDoesNotThrow`, `@Nested`; тесты должны иметь независимые фикстуры и предсказуемый clock/random. Для async-кода используй инструменты ожидания условий с timeout, а не `Thread.sleep(500)` как «синхронизацию».

`assertThrows` возвращает пойманное исключение, поэтому нужно проверять не только тип, но при необходимости message и cause. `assertInstanceOf` одновременно проверяет runtime-тип и возвращает приведённое значение.

```java
ValidationException exception = assertThrows(
    ValidationException.class,
    () -> AgeParser.parseAge("abc")
);

assertEquals("Invalid age: abc", exception.getMessage());
NumberFormatException cause = assertInstanceOf(
    NumberFormatException.class,
    exception.getCause()
);
assertNotNull(cause.getMessage());
```

Для тестов файловой системы JUnit 5 предоставляет `@TempDir`: каждый тест получает управляемый временный каталог и не зависит от абсолютных путей или мусора предыдущего запуска.

```java
@TempDir
Path tempDir;

@Test
void readsFile() throws IOException {
    Path file = tempDir.resolve("input.txt");
    Files.writeString(file, "Java\n\nJUnit\n");

    assertEquals(2L, NonEmptyLineCounter.countNonEmptyLines(file));
}
```

```java
@org.junit.jupiter.params.ParameterizedTest
@org.junit.jupiter.params.provider.CsvSource({"100,20,80", "50,0,50"})
void parameterized(int price, int discount, int expected) {
    assertEquals(expected, calc.finalPrice(price, discount));
}
```

### Пограничные случаи

Минимальный полезный набор зависит от контракта, но обычно проверяют:

- `null`, если он разрешён или должен отклоняться;
- пустую строку/коллекцию и один элемент;
- значение ровно на границе, на единицу меньше и больше;
- отрицательные значения, ноль, переполнение;
- дубликаты и сохранение порядка;
- неизвестный ключ или отсутствующий файл;
- Unicode и locale-sensitive данные;
- исключение, его message и cause;
- defensive copy/неизменяемость возвращённой коллекции.

Например, для условия «длина строго больше `n`» обязательно нужен тест строки длиной **ровно `n`** — он отличает `>` от `>=`.

### Демонстрация кода vs автоматический тест

```java
@Test
void onlyDemonstratesOutput() {
    try (ManagedResource resource = new ManagedResource("file.txt")) {
        System.out.println("work");
    }
}
```

Такой метод проходит, если не было исключения, но не проверяет содержание или порядок вывода. Это демонстрация либо smoke-проверка «не упало», а не полноценная автоматическая проверка поведения. Настоящий тест должен иметь проверяемый oracle: ожидаемое значение, состояние, исключение или существенное взаимодействие.

Если контракт действительно требует вывод в `System.out`, его можно временно перехватить. Глобальный stream необходимо восстановить даже при падении assertion:

```java
@Test
void printsStock() {
    PrintStream original = System.out;
    ByteArrayOutputStream buffer = new ByteArrayOutputStream();

    try {
        System.setOut(new PrintStream(buffer, true, StandardCharsets.UTF_8));

        Warehouse warehouse = new Warehouse();
        warehouse.receive("Apple", 2);
        warehouse.printStock();

        assertEquals(
            "Apple: 2" + System.lineSeparator(),
            buffer.toString(StandardCharsets.UTF_8)
        );
    } finally {
        System.setOut(original);
    }
}
```

Подмена `System.out` затрагивает глобальное состояние и мешает параллельным тестам. В production-коде часто лучше внедрить `PrintStream`/writer или вернуть форматированную строку, чтобы тестировать без глобальной подмены.

**Тестирование с реальной БД:** предпочти изолированную временную БД/Testcontainers для проверки SQL, индексов, транзакций и JDBC/JPA mappings; H2 не всегда совпадает с PostgreSQL/MySQL. Транзакционный rollback подходит не всем интеграционным сценариям (например, другой поток/процесс).

</details>

## 35. Mockito: mock vs spy

<details>
<summary>Показать ответ</summary>

**`mock`** — объект-заменитель: методы по умолчанию не выполняют настоящую бизнес-логику, возвращают default values (`0`, `false`, `null`/пустые значения в поддерживаемых случаях) либо заданные `when(...).thenReturn(...)`. **`spy`** — обёртка для *частичного* подменивания: незастабленные вызовы реальных методов выполняются; может менять состояние и выбрасывать реальные исключения.

```java
import static org.mockito.Mockito.*;
import java.util.*;

List<String> mock = mock(List.class);
when(mock.size()).thenReturn(42);
System.out.println(mock.size()); // 42

List<String> spy = spy(new ArrayList<String>());
spy.add("A");
System.out.println(spy.size()); // 1: реальный метод
```

**Ключевая ловушка spy:** `when(spy.get(0)).thenReturn("x")` сначала вызывает реальный `get(0)` при настройке и может упасть с `IndexOutOfBoundsException`. Используй `doReturn("x").when(spy).get(0)`.

```java
List<String> spyList = spy(new ArrayList<String>());
doReturn("x").when(spyList).get(0);
System.out.println(spyList.get(0)); // x
```

**Зачем выбирать:** mock — для внешних зависимостей: repository, HTTP client, email sender. Spy — только когда нужно частично подменить поведение существующего объекта, часто это сигнал к пересмотру архитектуры. Не мокай простые value objects и собственный бизнес-код без причины. Mockito spy может хранить копию состояния объекта при создании, а не вести себя как прозрачный делегат на все последующие внешние изменения исходного экземпляра.

**Термины:** dummy — фиктивный аргумент; stub — контролирует ответ; fake — упрощённая рабочая реализация (in-memory repo); mock — проверяет взаимодействия; spy — частичная реализация/наблюдение. В Mockito они пересекаются как техники, но различие полезно на собеседовании.

</details>

## 36. Что делают verify и ArgumentCaptor?

<details>
<summary>Показать ответ</summary>

`verify` проверяет, **какие взаимодействия произошли** с mock/spy; `ArgumentCaptor` **захватывает переданные аргументы**, чтобы проверить их поля. Это особенно полезно для `void` методов и побочных эффектов (`sendEmail`, `repository.save`).

```java
interface EmailSender { void send(String to, String body); }
class NotificationService {
    private final EmailSender sender;
    NotificationService(EmailSender sender) { this.sender = sender; }
    void welcome(String email) { sender.send(email, "Welcome!"); }
}
```

```java
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class NotificationServiceTest {
    @Test void sendsWelcome() {
        EmailSender sender = mock(EmailSender.class);
        NotificationService service = new NotificationService(sender);

        service.welcome("test@example.com");

        verify(sender, times(1)).send(eq("test@example.com"), eq("Welcome!"));
        verifyNoMoreInteractions(sender);
    }

    @Test void capturesMessage() {
        EmailSender sender = mock(EmailSender.class);
        NotificationService service = new NotificationService(sender);
        service.welcome("test@example.com");

        ArgumentCaptor<String> body = ArgumentCaptor.forClass(String.class);
        verify(sender).send(eq("test@example.com"), body.capture());
        assertEquals("Welcome!", body.getValue());
    }
}
```

**Обязательно знать:**

- `verify(mock)` по умолчанию означает `times(1)`; также `never()`, `atLeastOnce()`, `times(n)`.
- `verifyNoInteractions(mock)` — не было вызовов; `verifyNoMoreInteractions(mock)` — не осталось **непроверенных** вызовов. Не злоупотребляй последним: хрупкие тесты часто привязываются к реализации.
- `inOrder(mock1, mock2)` проверяет существенный порядок; не проверяй порядок, если контракт его не требует.
- `ArgumentCaptor` обычно используют при **verify**, а не при stubbing; для простого аргумента удобнее `eq(...)`, для условия — `argThat(...)`.
- При использовании matchers в одном вызове Mockito **все аргументы должны быть matchers** (`eq`, `any` и т. д.). `any()` не всегда совпадает с `null`: используй `isNull()`/`nullable(...)`, если нужно.

### Полный unit-тест с заглушкой repository

```java
interface OrderRepository { Optional<String> findStatus(long id); }
class OrderService {
    private final OrderRepository repo;
    OrderService(OrderRepository repo) { this.repo = repo; }
    boolean isPaid(long id) {
        return repo.findStatus(id).map("PAID"::equals).orElse(false);
    }
}

@Test void checksPaidStatus() {
    OrderRepository repo = mock(OrderRepository.class);
    when(repo.findStatus(12L)).thenReturn(Optional.of("PAID"));
    OrderService service = new OrderService(repo);

    assertTrue(service.isPaid(12L));
    verify(repo).findStatus(12L);
}
```

**Частая ошибка:** тест `verify(repository).findById(...)` без проверки результата сервиса доказывает лишь факт вызова, но не корректность бизнес-поведения. Сначала проверяй результат/состояние, взаимодействия проверяй там, где они действительно часть контракта. Используй параметризованные тесты для границ, отдельные тесты на ошибки/пустые результаты, интеграционные — для реального SQL/DI/транзакций.

</details>
