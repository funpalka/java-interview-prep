# Stream API, лямбды и Optional

> Вопросы: 21–26, 28–31, 38.

## 21. Можно ли вызвать две терминальные операции на одном Stream?

<details>
<summary>Показать ответ</summary>

Нет: stream — одноразовый конвейер. После терминальной операции (`count`, `collect`, `findFirst`, `forEach` и др.) он считается потреблённым. Повторное использование обычно приводит к `IllegalStateException`.

```java
Stream<String> stream = Stream.of("a", "b");
long count = stream.count();
// stream.findFirst(); // IllegalStateException
```

Даже если терминальная операция смогла оптимизировать вычисления, повторно использовать этот же `Stream` нельзя.

</details>

## 22. Как прогнать поток несколько раз?

<details>
<summary>Показать ответ</summary>

Создавать **новый Stream из источника** на каждый проход или материализовать данные в коллекцию, если это разумно по памяти:

```java
List<String> items = List.of("a", "bb");
long count = items.stream().count();
Optional<String> first = items.stream().findFirst();
```

Для однократного источника (файл, network stream, генератор) повторное создание может быть невозможно или дорого: иногда лучше выполнить одну агрегацию, либо предварительно буферизовать данные. `Supplier<Stream<T>>` удобен только если он действительно создаёт свежий stream.

</details>

## 23. Что делают flatMap и reduce?

<details>
<summary>Показать ответ</summary>

**`flatMap`** преобразует каждый элемент в поток (или другую «плоско объединяемую» структуру) и **склеивает вложенные потоки в один**. Типичный сценарий: `List<List<T>>`, списки заказов→позиции, строки→слова.

```java
List<List<Integer>> matrix = List.of(List.of(1, 2), List.of(3, 4));
List<Integer> flat = matrix.stream()
    .flatMap(List::stream)
    .toList(); // [1, 2, 3, 4]
```

**`reduce`** сворачивает элементы потока в единый результат при помощи **ассоциативной** операции и (в перегрузке с identity) нейтрального элемента. Терминальная операция.

```java
int sum = Stream.of(1, 2, 3).reduce(0, Integer::sum); // 6
Optional<Integer> max = Stream.of(1, 4, 2).reduce(Integer::max); // Optional[4]
String joined = Stream.of("a", "b", "c")
    .reduce("", (a, b) -> a + b); // abc; для строк лучше Collectors.joining()
```

**Почему ассоциативность важна:** параллельный stream может разбить данные и объединять частичные результаты в другом порядке. Например, `(a,b)->a-b` неассоциативна и даст неожиданный результат. Нельзя мутировать внешний `ArrayList` внутри reduce. Для изменяемого накопления обычно используй `collect` (`toList`, `groupingBy`, `joining`). **Нюанс:** `reduce(identity, accumulator, combiner)` требует согласованности accumulator/combiner и нейтральности identity.

</details>

## 24. Разница между map и flatMap

<details>
<summary>Показать ответ</summary>

`map(Function<T,R>)`: **один входной элемент → одно выходное значение**; количество элементов сохраняется (если нет других операций фильтрации). `flatMap(Function<T,Stream<R>>)` — **один элемент → 0, 1 или много элементов** и разворачивает результат в общий поток.

```java
List<List<String>> lines = List.of(List.of("a", "b"), List.of("c"));
List<List<String>> mapped = lines.stream()
    .map(x -> x.stream().map(String::toUpperCase).toList())
    .toList(); // [[A, B], [C]]
List<String> flattened = lines.stream()
    .flatMap(List::stream)
    .map(String::toUpperCase)
    .toList(); // [A, B, C]
```

`flatMap` нужен, например, чтобы получить все товары всех заказов: `orders.stream().flatMap(o -> o.items().stream())`. Он **не «быстрее map» по умолчанию**, это разная форма результата. Альтернатива для `Optional`: `map` создаёт `Optional<Optional<T>>`, `flatMap` — `Optional<T>` при корректной функции `T -> Optional<U>`.

</details>

## 25. Что такое parallelStream?

<details>
<summary>Показать ответ</summary>

`parallelStream()` (или `stream().parallel()`) задаёт **параллельное выполнение** частей pipeline. Для обычных коллекций реализация обычно использует общий `ForkJoinPool.commonPool()`; элементы могут обрабатываться в разных потоках, но **ускорение не гарантировано**.

```java
long sum = LongStream.rangeClosed(1, 1_000_000)
    .parallel()
    .sum(); // редукция с корректной ассоциативной операцией
```

**Где может помочь:** большой массив/диапазон, CPU-bound обработка каждого независимого элемента, достаточно тяжёлая работа на элемент. **Где часто навредит:** маленькие данные, блокирующий I/O и БД, операции с общей mutable state, дорогие split/merge, работа в server thread pool с сильной конкуренцией за ядра.

```java
List<Integer> unsafe = new ArrayList<>();
IntStream.range(0, 10000).parallel().forEach(unsafe::add); // data race!
List<Integer> safe = IntStream.range(0, 10000).parallel().boxed().toList();
```

`forEach` **не сохраняет порядок** выполнения; `forEachOrdered` обеспечивает encounter order (если он есть), но может снизить выгоду от распараллеливания. `findAny` допускает произвольный элемент, `findFirst` сохраняет первый по encounter order (может быть дороже). Измеряй JMH и реальную нагрузку; `parallelStream` — не универсальная замена async/reactive API.

</details>

## 26. Какой тип возвращает промежуточная операция Stream?

<details>
<summary>Показать ответ</summary>

**Промежуточные операции** (`filter`, `map`, `flatMap`, `distinct`, `sorted`, `peek`, `limit`) обычно возвращают **новый объект-представление pipeline типа `Stream<R>`** (или примитивный `IntStream`/`LongStream`/`DoubleStream` при соответствующих операциях), **не готовую коллекцию**. Пример:

```java
Stream<Integer> base = Stream.of(1, 2, 3);
Stream<String> transformed = base.filter(x -> x > 1).map(Object::toString);
List<String> result = transformed.toList(); // [2, 3]
```

Операции **ленивы**: до терминальной операции не выполняется полноценный обход источника. Некоторые операции stateful (`sorted`, `distinct`) могут буферизовать данные, некоторые short-circuit (`limit`, `findFirst`, `anyMatch`) останавливают обработку раньше. **Поток одноразовый**: после вызова терминальной операции нельзя снова использовать тот же экземпляр. У `mapToInt` возвращается `IntStream`, у `boxed()` — `Stream<Integer>`.

</details>

## 28. К каким переменным есть доступ у лямбда-выражений?

<details>
<summary>Показать ответ</summary>

Лямбда может захватывать **параметры метода** и **локальные переменные** из окружающей области, только если они `final` или **effectively final** (после инициализации не переназначались). А вот поля объекта/статические поля можно изменять (с оговорками о потокобезопасности).

```java
int threshold = 10; // effectively final
Predicate<Integer> large = x -> x > threshold;
// threshold++; // ошибка компиляции: переменная захвачена

class Counter {
    private int value;
    Runnable step() { return () -> value++; } // поле менять можно
}
```

**Почему так:** лямбда может пережить стековый frame, где была объявлена локальная переменная; её значение захватывается, а не поддерживается как живая изменяемая стековая переменная. Объект, на который указывает локальная ссылка, **может быть mutable**:

```java
List<String> names = new ArrayList<>();
Runnable add = () -> names.add("A"); // допустимо: ссылка names не меняется
// names = new ArrayList<>();          // после такого захват уже запрещён
```

Это **не делает операции потокобезопасными**: общий изменяемый список в `parallelStream` опасен. В параметрах лямбды имена имеют свою область видимости: нельзя объявить параметр с именем уже существующей локальной переменной в той же области.

</details>

## 29. findFirst vs findAny vs forEach

<details>
<summary>Показать ответ</summary>

| Операция | Возвращает | Порядок и использование |
|---|---|---|
| `findFirst()` | `Optional<T>` | первый по **encounter order**, если порядок задан; в parallel может быть дороже |
| `findAny()` | `Optional<T>` | любой подходящий элемент; позволяет больше свободы параллелизму |
| `forEach(Consumer<T>)` | `void` | выполняет побочный эффект для каждого элемента; в parallel порядок не гарантируется |
| `forEachOrdered(Consumer<T>)` | `void` | соблюдает encounter order, когда есть |

```java
List<Integer> nums = List.of(3, 1, 2);
System.out.println(nums.stream().findFirst()); // Optional[3]
System.out.println(nums.stream().findAny());   // может быть 3, но контракт не обещает
nums.parallelStream().forEach(System.out::println); // порядок произвольный
nums.parallelStream().forEachOrdered(System.out::println); // 3, 1, 2
```

**Отличие от `filter`:** `findFirst/findAny/forEach` — терминальные операции; `filter` — промежуточная. `findFirst`/`findAny` могут **short-circuit**, `forEach` обходит все элементы (кроме прерывания ошибкой). На неупорядоченном источнике вроде `HashSet` «первый» не определяется контрактом последовательности. Для поиска объекта обычно `filter(predicate).findFirst()`.

</details>

## 30. Чем лямбда принципиально отличается от анонимного класса?

<details>
<summary>Показать ответ</summary>

Лямбда — **выражение**, представляющее реализацию **функционального интерфейса** (ровно один abstract метод; `@FunctionalInterface` помогает проверять). Анонимный класс — **объявление и создание экземпляра класса** (или реализация интерфейса), у него может быть собственное состояние/методы, initializer и своя область `this`.

```java
interface Task { void run(); }
class Demo {
    private String name = "outer";
    Task lambda = () -> System.out.println(this.name);
    Task anonymous = new Task() {
        private final String name = "inner";
        @Override public void run() { System.out.println(this.name); }
    };
}
```

**Отличия:**

| Вопрос | Лямбда | Анонимный класс |
|---|---|---|
| `this`/`super` | из внешнего лексического контекста | сам анонимный экземпляр / его superclass |
| Отдельные поля и методы | нельзя объявить собственные instance members | можно |
| Область имён | лексически общая с окружающим блоком | отдельная область класса |
| Захват локальных | `final`/effectively final | тоже `final`/effectively final |
| Реализация | JVM может использовать `invokedynamic`/`LambdaMetafactory`; нельзя обещать конкретный класс | экземпляр анонимного класса |
| Интерфейс | только functional interface | интерфейс/класс, необязательно функциональный |

```java
Runnable r = () -> System.out.println("run");
Comparator<String> byLength = (a, b) -> Integer.compare(a.length(), b.length());
// Метод-ссылка — компактная запись функционального поведения:
Function<String, Integer> length = String::length;
```

**Важно:** лямбда не «синтаксический сахар» *обязательно* превращающийся в `new AnonymousClass()`: спецификация не требует конкретной стратегии реализации. Нельзя рассчитывать на идентичность экземпляров лямбд или на их класс; `getClass()`/сериализация — плохая база для бизнес-логики. Отдельный нюанс: `return` в лямбде возвращает из **тела лямбды**, а не из окружающего метода.

**Почему выбираем:** для короткой stateless функции — лямбда; для нетривиального состояния/нескольких методов — именованный класс зачастую понятнее анонимного.

</details>

## 31. На что ссылается this в лямбде и анонимном классе?

<details>
<summary>Показать ответ</summary>

В **лямбде** `this` означает `this` **окружающего экземпляра**, если такой есть. В **анонимном классе** `this` означает сам объект анонимного класса. Аналогично `super` в лямбде относится к внешнему контексту, а в анонимном классе — к его родителю.

```java
class Greeter {
    private final String label = "outer";

    Runnable lambda() {
        return () -> System.out.println(this.label); // Greeter.this
    }

    Runnable anonymous() {
        return new Runnable() {
            private final String label = "inner";
            @Override public void run() {
                System.out.println(this.label);         // inner
                System.out.println(Greeter.this.label); // outer
            }
        };
    }

    void demo() {
        lambda().run();    // outer
        anonymous().run(); // inner, затем outer
    }
}
```

В **static context** нет `this`, и написать `this` в лямбде из static метода нельзя. Лямбда не вводит новую `this`-область: `this` — лексический. Это влияет на доступ к полям, вызов методов внешнего класса, теневые имена и debugging. В анонимном классе внешний экземпляр можно явно указать `Outer.this`.

**Не путать захват:** при `() -> this.doWork()` лямбда использует внешний экземпляр; при `() -> x + local` она захватывает effectively-final локальную переменную. Если лямбду сохранять в долгоживущем объекте, она может **удерживать внешний экземпляр в памяти** — диагностируется по GC roots в heap dump.

</details>

## 38. Optional.of(null) vs Optional.ofNullable(null)

<details>
<summary>Показать ответ</summary>

`Optional.of(value)` **требует не-null**: `Optional.of(null)` немедленно бросает `NullPointerException`. `Optional.ofNullable(value)` допускает `null`: в таком случае вернёт **`Optional.empty()`**.

```java
// Optional.of(null);                     // NullPointerException
Optional<String> a = Optional.ofNullable(null); // Optional.empty()
Optional<String> b = Optional.of("Hi");         // Optional[Hi]
System.out.println(a.orElse("default"));       // default
```

**Практическое применение:** return type для *возможно отсутствующего результата*, например, `repository.findById(id)`. Обычно не стоит использовать Optional как поле JPA-сущности или параметр каждого метода.

```java
String readName() { return "fallback"; }
String x = b.orElse(readName());              // readName() вызовется ВСЕГДА
String y = b.orElseGet(() -> readName());     // только если Optional пустой
String z = a.orElseThrow(() -> new IllegalStateException("Missing"));
```

`map` преобразует значение и превращает `null`-результат в empty; `flatMap` ожидает функцию, возвращающую `Optional`, и **не принимает null Optional**. `get()` на пустом бросает `NoSuchElementException`; предпочтительнее `orElseThrow` с контекстом, `ifPresent`, `map` и `flatMap`. `Optional` — value-based class: не синхронизируйся на его экземпляре и не проверяй через `==`.

</details>
