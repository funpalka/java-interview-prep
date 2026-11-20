# Исключения Java

> Вопросы: 27, 32, 33, 37, 39.

## 27. Иерархия исключений

<details>
<summary>Показать ответ</summary>

Иерархия Java (упрощённо):

```text
java.lang.Object
 └── java.lang.Throwable
      ├── java.lang.Error                 (unchecked)
      │    ├── VirtualMachineError
      │    │    ├── OutOfMemoryError
      │    │    └── StackOverflowError
      │    ├── AssertionError
      │    └── LinkageError
      │         └── NoClassDefFoundError
      └── java.lang.Exception
           ├── IOException                 (checked)
           ├── SQLException                (checked)
           ├── InterruptedException        (checked)
           └── RuntimeException            (unchecked)
                ├── NullPointerException
                ├── IllegalArgumentException
                ├── IndexOutOfBoundsException
                └── IllegalStateException
```

**Checked**: потомки `Exception`, кроме `RuntimeException` и её подтипов — компилятор требует обработать или объявить `throws`. **Unchecked**: `RuntimeException`, `Error` и их потомки; нет обязательной декларации `throws`. `Throwable` — общий родитель, содержит message/cause/stack trace/suppressed.

**Как получить распространённые исключения на практике:**

```java
String s = null;
// s.length();                         // NullPointerException
// List.of(1).get(5);                 // IndexOutOfBoundsException
// Integer.parseInt("NaN");           // NumberFormatException
// throw new IllegalArgumentException("bad input");
// throw new IllegalStateException("wrong phase");

try {
    throw new java.io.IOException("disk unavailable"); // checked
} catch (java.io.IOException ex) {
    System.out.println(ex.getMessage());
}
```

**Как получить `Error`:** `throw new AssertionError("broken invariant")` позволяет безопасно *демонстрировать тип*; `assert false` бросит `AssertionError` **только при включённом `-ea`**. `StackOverflowError` может появиться при бесконечной рекурсии, `OutOfMemoryError` — при нехватке памяти, но **не нужно намеренно исчерпывать память/стек** на рабочем компьютере или сервере. Не все `Error` возникают только из-за фатального сбоя JVM (например, `AssertionError`), однако многие сигнализируют о тяжёлых проблемах, от которых приложение не способно надёжно восстановиться.

```java
throw new AssertionError("unexpected invariant violation"); // unchecked Error
```

**Правильная обработка:** ловить конкретный тип на границе, где можешь восстановить работу, добавить контекст и сохранить cause:

```java
try { loadFile(); }
catch (IOException e) {
    throw new ConfigLoadException("Unable to read application config", e);
}
```

`try-with-resources` автоматически закрывает `AutoCloseable` и прикрепляет ошибки при закрытии как **suppressed exceptions** к первичной ошибке. `finally` обычно выполняется, но не всегда (вопрос 32). `InterruptedException`: обычно восстановить interrupt flag (`Thread.currentThread().interrupt()`) и завершить/передать выполнение, если не пробрасываешь исключение.

</details>

## 32. Когда finally НЕ выполнится?

<details>
<summary>Показать ответ</summary>

`finally` выполняется при обычном выходе из `try`/`catch`, в том числе при `return`, `break`, `continue` и исключениях. Однако может **не выполниться**, если процесс/JVM прекращает работу **до достижения блока**: `System.exit` с фактическим завершением процесса, `Runtime.halt`, принудительное убийство процесса, аварийный сбой JVM/ОС, выключение питания.

Ещё ситуация: поток не выходит из `try` (бесконечный цикл, вечная блокировка), следовательно, до `finally` не доходит. Исключение или `return` внутри `finally` способны **замаскировать** исходное исключение/возврат — это антипаттерн.

</details>

## 33. Можно ли переприсвоить переменную исключения в catch?

<details>
<summary>Показать ответ</summary>

**Да, в обычном `catch (Exception e)` параметру можно присвоить другую ссылку** (если совместим тип). Это **не меняет уже пойманный объект**: переменная `e` просто теперь ссылается на другой объект. На практике так делать не рекомендуется, потому что теряется контекст оригинальной ошибки.

```java
try {
    throw new IllegalArgumentException("old");
} catch (Exception e) {
    e = new IllegalStateException("new"); // корректно
    System.out.println(e.getMessage());    // new
}
```

**Но:** параметр в **multi-catch** (`catch (IOException | SQLException e)`) неявно `final`, поэтому переназначать его **нельзя**.

```java
try { risky(); }
catch (IOException | SQLException e) {
    // e = new RuntimeException(); // ошибка компиляции
    throw new RuntimeException("Operation failed", e); // сохраняем cause
}
```

Если вручную объявить `catch (final Exception e)`, присваивание также запрещено. Помни: `catch` ловит исключения по совместимости типа; обработчики располагаются от конкретных к общим — иначе последующие блоки становятся недостижимыми.

</details>

## 37. Доменное исключение: Exception или RuntimeException?

<details>
<summary>Показать ответ</summary>

**Выбор зависит от контракта API и возможности восстановления.**

- `extends RuntimeException` (unchecked) — обычно подходит для **нарушенного бизнес-инварианта**, некорректного состояния/вызова, когда вызывающая сторона не должна *обязательно* разбирать ошибку на каждом уровне.
- `extends Exception` (checked) — подходит для **ожидаемого recoverable-сценария**, когда контракт явно требует от вызывающего обработать или пробросить ситуацию. Но навязывание checked исключений всем слоям может привести к шумным `throws` и бессмысленным `catch`.

```java
class InsufficientStockException extends RuntimeException {
    InsufficientStockException(String sku, int quantity) {
        super("Not enough stock for " + sku + ", qty=" + quantity);
    }
}

class ImportFailedException extends Exception {
    ImportFailedException(String file, Throwable cause) {
        super("Cannot import " + file, cause);
    }
}
```

Для пользовательского отказа вроде «товар закончился» **не всегда нужно исключение**: если ситуация штатная и частая, подойдёт результат `boolean`, `Result`, sealed hierarchy (`Success/OutOfStock`). Исключения лучше для нештатного потока управления. На границе REST: доменную ошибку переводят в HTTP-статус и безопасное сообщение, не отдают stack trace клиенту. Для retry важна *причина*: validation error не ретраим, временный сетевой сбой иногда ретраим с backoff.

**Уточнение:** `Error` не следует наследовать для обычных доменных ошибок. Давай исключениям осмысленные имена, храни cause через конструктор, не глотай ошибки и не включай секреты в message.

</details>

## 39. Ловить Throwable в главном цикле, чтобы приложение никогда не падало?

<details>
<summary>Показать ответ</summary>

**Нет, `catch (Throwable t)` не гарантирует, что приложение «никогда не упадёт».** `Throwable` объединяет `Exception` и **`Error`**, включая `OutOfMemoryError`, `StackOverflowError`, сбои линковки и прочие состояния, при которых продолжение работы может быть небезопасно. JVM может завершиться из-за `Runtime.halt`, `System.exit`, сигнала ОС, native crash, завершения процесса контейнером — без возможности выполнения `catch`.

```java
while (running) {
    try {
        processMessage();
    } catch (Throwable t) { // антипример
        log.error("failed", t); // при OOME даже логирование может не сработать
        // состояние приложения может быть уже повреждено
    }
}
```

**Что делать лучше:** ловить **ожидаемые конкретные ошибки** на подходящей границе; разделять transient/неисправимые состояния, применять retry с limit/backoff и dead-letter queue при обработке сообщений, правильно освобождать ресурсы; неожиданные ошибки фиксировать и позволять процессу корректно завершиться под контролем supervisor/Kubernetes с health checks и restart policy.

```java
try {
    processMessage();
} catch (InvalidMessageException e) {
    deadLetter(e); // можно обработать определённый контрактом кейс
} catch (Exception e) {
    log.error("Unhandled message processing failure", e);
    throw e; // сохранить сигнал о неисправности
}
```

**Допустимое исключение:** на самой верхней границе framework иногда ловит `Throwable` **для записи диагностик/cleanup**, но это не обещание продолжения нормальной работы; `Error` часто нужно пробрасывать дальше. Отдельно: `Thread.UncaughtExceptionHandler` логирует завершение потока, не «лечит» отказ. `InterruptedException` требует бережной обработки interrupt status.

</details>
