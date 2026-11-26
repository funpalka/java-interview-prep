# JVM, коллекции и generics

> Вопросы: 9, 10, 12, 14, 16, 17, 19.

## 9. JVM: heap/stack/metaspace, GC (G1/ZGC/Shenandoah)

<details>
<summary>Показать ответ</summary>

**Модель памяти JVM (важно: области JVM Spec и конкретная реализация HotSpot не полностью совпадают):**

| Область | Назначение и жизненный цикл | Типичная проблема |
|---|---|---|
| **Heap** | общая для потоков область объектов/массивов, собирается GC | `OutOfMemoryError: Java heap space` |
| **Java stack** | собственный для каждого потока набор stack frames: локальные переменные, operand stack, возврат | `StackOverflowError` при глубокой рекурсии |
| **Metaspace** (HotSpot) | метаданные классов в native memory; классы могут выгружаться с classloader | `OutOfMemoryError: Metaspace` |
| **Code cache** | JIT-компилированный код | нехватка code cache, деградация компиляции |
| **Direct/native memory** | off-heap буферы, JNI, стеки потоков, служебные структуры GC | `Direct buffer memory`, ошибки native allocation |

Локальная переменная `User u` — ссылка в frame; сам `User` обычно живёт в heap. **Метаданные класса в Metaspace не означают, что все статические поля и `Class`-объекты находятся там.** Для понимания утечки classloader рассматривай цепочки ссылок и удерживаемые объекты.

**GC и достижимость:** сборщик идёт от **GC roots** (например, активные стеки, статические ссылки, JNI roots) по графу ссылок. Недостижимые объекты можно освободить. Это не просто счётчик ссылок: циклы тоже собираются. Типичный generational подход опирается на гипотезу о короткой жизни большинства объектов; но алгоритмы и поколенческая модель различаются.

**Сборщики и компромиссы (основной ориентир — HotSpot JDK 17):**

| GC | Как работает | Когда выбирать / ограничения |
|---|---|---|
| **G1** | heap разбит на регионы, есть young/mixed collections, часть работы конкурентная, эвакуация объектов с STW-паузами; пытается следовать `MaxGCPauseMillis` | хороший старт для большинства сервисов, баланс пауз и throughput; настройка цели не гарантирует лимит |
| **ZGC** | основная работа конкурентная с приложением, короткие паузы; в Java 17 используется non-generational режим | критична низкая tail latency на большом heap; возможен CPU/memory overhead; флаги и режим зависят от версии |
| **Shenandoah** | конкурентная маркировка/эвакуация для снижения STW | низкие паузы, доступность/вариант зависят от дистрибутива и JDK |

> **Новые JDK:** Generational ZGC появился как отдельный режим позже Java 17 (в JDK 21). Его нельзя считать возможностью целевой версии проекта.

GC не лечит утечку, если объект **достижим**, но больше не нужен бизнесу (например, бесконечно растущий `Map`, listener без `unsubscribe`, неконтролируемый cache). Для поиска: метрики, GC logs `-Xlog:gc*`, Java Flight Recorder (JFR), профилировщик, heap dump.

### Heap dump — подробно

**Heap dump** — снимок графа объектов в Java heap, обычно в формате **HPROF**. Показывает типы, экземпляры, размеры, ссылки и пути удержания. Он **не равен** thread dump (стеки потоков) или GC log (события сборки). Не гарантирует показ всей native memory: при проблеме с Metaspace/direct buffers понадобится дополнительная диагностика.

**Когда снимать:** `OutOfMemoryError`, рост used heap после GC, подозрение на удержание объектов, неожиданно огромные кэши или коллекции. Важен **момент**: снимок до/после сценария может быть информативнее одного дампа.

```bash
# 1) Найти PID и доступные команды (запускай с нужными правами)
jcmd -l
jcmd <PID> help GC.heap_dump

# 2) Снять HPROF. Обычно вызывает Full GC перед снимком:
jcmd <PID> GC.heap_dump /var/tmp/app-heap.hprof

# 3) Сохранить все объекты, включая недостижимые (без предварительного Full GC):
jcmd <PID> GC.heap_dump -all /var/tmp/app-all.hprof

# 4) Возможность более нового JDK: в JDK 21 поддерживается сжатие gzip:
jcmd <PID> GC.heap_dump -gz=1 /var/tmp/app-heap.hprof.gz
```

`-all` **не** значит «покажи все типы памяти»; он влияет на включение недостижимых объектов и пропуск запроса Full GC. По умолчанию команда может вызвать Full GC. Варианты/точные имена флагов проверяй через `jcmd <PID> help GC.heap_dump` в своей версии JDK.

**Автоматический дамп при OOME:**

```bash
java -Xms512m -Xmx512m \
  -XX:+HeapDumpOnOutOfMemoryError \
  -XX:HeapDumpPath=/var/dumps \
  -Xlog:gc*:file=/var/log/app-gc.log:time,uptime,level,tags \
  -jar app.jar
```

Папка должна существовать, быть доступна для записи и иметь **свободное место** (дамп может занимать гигабайты). Дамп содержит персональные данные, токены и секреты из объектов: **не коммить в GitHub**, ограничивай доступ и удаляй после анализа. Снятие дампа нагружает процесс и может вызвать паузу — планируй действия осторожно на production.

**Как анализировать в Eclipse Memory Analyzer (MAT):**

1. Открыть `.hprof` (при необходимости предварительно распаковать `.gz`).
2. Посмотреть *Histogram*: какие классы занимают память по количеству/размеру.
3. Посмотреть *Dominator Tree*: **retained heap** — сколько памяти можно потенциально освободить, если убрать этот объект и всё, что удерживается исключительно через него. **Shallow heap** — размер самого объекта.
4. Запустить *Leak Suspects* как подсказку, не как доказательство.
5. Для подозрительного объекта проверить *Path to GC Roots* (желательно исключая слабые/soft/phantom refs, когда это уместно). Ищи поле/кеш/статическую ссылку, которая удерживает дерево объектов.
6. Проверить гипотезу вторым дампом или метриками до/после действия, исправить владение/лимиты/отписку и повторить измерения.

**Пример утечки:**

```java
class SessionRegistry {
    // Если сессии не удалять, GC не может их освободить.
    static final Map<String, byte[]> sessions = new HashMap<>();
    static void opened(String id) {
        sessions.put(id, new byte[1024 * 1024]);
    }
}
```

В MAT будет виден путь `GC root → SessionRegistry.sessions → HashMap → byte[]`. Решение — удаление на закрытии, TTL/ограничение кэша, корректное управление жизненным циклом, а не просто `-Xmx` побольше.

**Дополнительно:** `jcmd <PID> GC.class_histogram` даёт быстрый срез по классам без полного графа ссылок; JFR/GC logs помогают отличить высокую скорость аллокаций от **удержания**. Метасpace, количество потоков и native allocations смотрят другими средствами (например, Native Memory Tracking, если включён).

Источники для целевой версии: [Oracle JDK 17 `jcmd`](https://docs.oracle.com/en/java/javase/17/docs/specs/man/jcmd.html), [G1 tuning](https://docs.oracle.com/en/java/javase/17/gctuning/garbage-first-g1-garbage-collector1.html). Флаг `-gz` выше относится к более новому JDK 21.

</details>

## 10. Коллекции, equals/hashCode, generics/type erasure/PECS

<details>
<summary>Показать ответ</summary>

### Иерархия и выбор коллекции

`Iterable → Collection → List / Set / Queue`; `Deque extends Queue`. `Map<K,V>` **не наследует** `Collection`. `List` допускает дубликаты и индексы; `Set` хранит уникальные элементы; `Queue/Deque` управляют порядком обработки; `Map` сопоставляет ключи и значения.

| Запрос | Типичный выбор | Почему |
|---|---|---|
| частый индексный доступ/итерация | `ArrayList` | компактный массив, хорошая локальность |
| уникальность с быстрым поиском | `HashSet` | hash-таблица, без порядка |
| сохранить порядок добавления уникальных | `LinkedHashSet` | hash + linked order |
| отсортированные уникальные значения | `TreeSet` | дерево, O(log n) |
| ключ → значение | `HashMap` | среднее O(1) |
| порядок вставки ключей | `LinkedHashMap` | предсказуемая итерация |
| отсортированные ключи | `TreeMap` | O(log n) |
| стек/очередь | `ArrayDeque` | обычно эффективнее `Stack`/`LinkedList` |

`equals/hashCode`: две равные сущности должны иметь одинаковый хеш; иначе hash-структуры перестают находить элементы. `TreeSet/TreeMap` определяют эквивалентность через `compareTo`/`Comparator` (`compare(...) == 0`), а не по hash. Подробнее — вопросы 14, 16, 17, 19.

### Практические шаблоны выбора

```java
// Удалить дубликаты и сохранить порядок первого появления:
List<Integer> unique = new ArrayList<>(new LinkedHashSet<>(input));

// Несколько уникальных значений на ключ с сохранением порядка:
Map<String, LinkedHashSet<String>> phoneBook = new HashMap<>();
phoneBook.computeIfAbsent("Alex", ignored -> new LinkedHashSet<>())
         .add("111");

// Сохранить вычисленный порядок записей результата:
Map<String, Integer> top = new LinkedHashMap<>();

// Всегда обходить ключи в отсортированном порядке:
Map<String, Integer> stock = new TreeMap<>();
```

`LinkedHashMap` и `LinkedHashSet` платят дополнительной памятью за связный порядок. `TreeMap` поддерживает сортировку при каждом обновлении за O(log n); если сортированный результат нужен только один раз, иногда дешевле собрать данные в `HashMap`, а затем отдельно отсортировать записи.

### Generics: зачем нужны и как работают

**Параметризация типов** даёт проверку во время компиляции и убирает небезопасные явные cast:

```java
List<String> names = new ArrayList<>();
names.add("Ada");
String first = names.get(0); // компилятор знает тип
// names.add(42);            // ошибка компиляции
```

`List<String>` **не является** подтипом `List<Object>` — generics **инвариантны**. Иначе можно было бы вставить `Integer` в список строк:

```java
List<String> strings = new ArrayList<>();
// List<Object> objects = strings; // запрещено
// objects.add(123);               // испортило бы strings
```

**Bounds:** `<T extends Number>` ограничивает тип сверху (ключевое слово `extends` используется и для интерфейсов); несколько ограничений: `<T extends Number & Comparable<T>>`. В методах type parameter объявляют перед return type:

```java
static <T> T choose(T left, T right) { return left; }
static <T extends Number> double twice(T value) { return value.doubleValue() * 2; }
```

### Type erasure (стирание типов)

Java generics реализованы преимущественно **на уровне компилятора**: после компиляции типовые параметры стираются до их верхней границы (или `Object`), компилятор при необходимости вставляет casts и создаёт **bridge methods** для сохранения полиморфизма после стирания. JVM не хранит отдельный runtime-класс `ArrayList<String>` vs `ArrayList<Integer>`. Сведения о generic-сигнатурах могут оставаться в metadata для reflection, но это **не reified runtime-проверка элементов**.

```java
class Box<T> {
    T value;
    T get() { return value; }
}
// По смыслу после erasure T без bound → Object,
// вызов Box<String>.get() компилятор может сопроводить cast к String.
```

Следствия:

```java
// new T();                        // нельзя создать T
// T.class;                        // нельзя получить T.class
// new List<String>[5];            // нельзя создать массив параметризованного типа
// if (obj instanceof List<String>) // нельзя проверить аргумент типа в runtime
if (obj instanceof List<?> list) { System.out.println(list.size()); } // можно
```

Нельзя перегружать методы `process(List<String>)` и `process(List<Integer>)`: после стирания сигнатуры сталкиваются. `List<Integer>.class` тоже не существует; для runtime-типизации применяют `Class<T>`, `Type`/`TypeToken` (для сложных generic-типов), явную проверку входа.

**Массивы ковариантны, но reified:** `Object[] arr = new String[1]; arr[0] = 1;` → `ArrayStoreException`. С generic-массивами ограничения защищают от такого конфликта; raw types и unchecked casts могут привести к `ClassCastException` позже.

### Wildcards и PECS — подробно

**PECS = Producer Extends, Consumer Super**. Смотри не на то, «что хранится в списке», а на **как метод использует аргумент**.

- `List<? extends Number>` — производитель `Number`: безопасно **читать** как `Number`, но нельзя добавлять `Number`/`Integer` (неизвестно, список это `Integer` или `Double`), кроме `null`.
- `List<? super Integer>` — потребитель `Integer`: можно **добавлять** `Integer` (и его подтипы), но получить безопасно можно лишь `Object`.
- `List<?>` — список неизвестного типа: удобно читать как `Object`, добавлять можно только `null`.
- `List<T>` — когда и читаем, и записываем именно один согласованный тип `T`.

```java
static <T> void copy(List<? extends T> source, List<? super T> target) {
    for (T value : source) target.add(value);
}
List<Integer> ints = List.of(1, 2, 3);
List<Number> numbers = new ArrayList<>();
copy(ints, numbers); // источник Integer, приёмник Number
```

```java
static double sum(List<? extends Number> source) {
    double result = 0;
    for (Number n : source) result += n.doubleValue();
    return result;
}
static void addDefaults(List<? super Integer> target) {
    target.add(10);
    target.add(20);
}
```

**Важный нюанс:** `? extends T` — не «список, в который можно положить любого наследника T». Он может оказаться списком конкретного более узкого типа. А `? super Integer` не обещает, что чтение вернёт `Integer`. В `Collections.copy(dest, src)` те же идеи.

**Уточнения:** wildcard *capture* (helper-метод `<T>` может связать неизвестный тип), **heap pollution** через raw/varargs, почему `List<Object>` не принимает `List<String>`, отличие `<T>` (связывает тип в нескольких местах) от `?` (неизвестный тип). Помни, что generics не поддерживают примитивные типы: `List<int>` нельзя, используют `List<Integer>` (или `IntStream`).

</details>

## 12. Сложность операций в коллекциях

<details>
<summary>Показать ответ</summary>

**Оценки — для типичных реализаций Java и без конкурентной модификации.**

| Операция | `ArrayList` | `LinkedList` |
|---|---|---|
| `get(i)`/`set(i)` | **O(1)** | **O(n)**: проход от ближайшего конца |
| `add(x)` в конец | амортизированно **O(1)**, при расширении O(n) | **O(1)** |
| `add(i,x)` в середину | **O(n)**: сдвиг элементов | **O(n)**: сначала дойти до индекса; вставка уже найденного узла O(1) |
| `remove(i)` | обычно **O(n)**: сдвиг (с конца O(1)) | **O(n)** по индексу; удаление уже найденного узла O(1) |
| `contains(x)` | **O(n)** | **O(n)** |
| обход | **O(n)**, обычно быстрее на CPU | **O(n)**, переходы по узлам и аллокации |
| память | массив ссылок и резерв ёмкости | каждый элемент — отдельный узел с `prev/next` |

```java
List<Integer> a = new ArrayList<>();
List<Integer> l = new LinkedList<>();
for (int i = 0; i < 100_000; i++) { a.add(i); l.add(i); }
// 100 000 раз l.get(i) => суммарно O(n^2)!
for (int x : l) { /* O(n) через Iterator */ }
```

`LinkedList` удобен при вставках/удалениях через **уже имеющийся `ListIterator`**, но в обычных задачах `ArrayList` чаще лучше из-за cache locality, меньших накладных расходов и быстрого индексного доступа. Для очереди обычно используй `ArrayDeque`, а не `LinkedList`.

| Коллекция | `get`/`contains` | вставка/удаление | Порядок |
|---|---|---|---|
| `HashMap` | среднее O(1) по ключу | среднее O(1) | не гарантирован |
| `LinkedHashMap` | среднее O(1) | среднее O(1) | вставки или access-order |
| `TreeMap` | O(log n) | O(log n) | сортировка ключей |
| `HashSet` | среднее O(1) | среднее O(1) | не гарантирован |
| `TreeSet` | O(log n) | O(log n) | сортировка элементов |
| `ArrayDeque` | `contains`: O(n) | концы: амортизированно O(1) | очередь/дек |

У hash-коллекций сложность зависит от распределения `hashCode`, загрузки и коллизий. При плохих хешах гарантировать O(1) нельзя. Сравнение по Big-O не учитывает затраты на память, GC, CPU cache; бенчмарки производительности делай через **JMH**, а не по единичному `currentTimeMillis`.

</details>

## 14. Контракт equals() и hashCode()

<details>
<summary>Показать ответ</summary>

`equals`: **рефлексивность**, **симметричность**, **транзитивность**, **согласованность** при неизменных значимых полях; `x.equals(null)` всегда `false` для ненулевого `x`.

`hashCode`: если `a.equals(b) == true`, то `a.hashCode() == b.hashCode()` обязательно. **Обратное неверно**: одинаковый hash не означает равенство. Пока значимые поля не меняются, hashCode должен быть согласованным.

Переопределяя `equals`, почти всегда нужно переопределять `hashCode`. Для value objects удобно использовать `record` (с учётом семантики полей). Не меняй поля, участвующие в `equals/hashCode`, пока объект служит ключом `HashMap` или лежит в `HashSet`.

</details>

## 16. Как устроена и работает HashMap?

<details>
<summary>Показать ответ</summary>

**HashMap (упрощённая модель реализации OpenJDK 17):** массив `Node<K,V>[] table` длиной обычно степень двойки. У `Node` есть hash, key, value, next. Для `put` вычисляется hash (`hashCode` + смешивание старших битов), индекс корзины (`(n - 1) & hash`). В пустую корзину — новый узел; при коллизии сравниваются hash и `equals`; существующее значение заменяется либо создаётся новый узел.

При множестве коллизий связанный список в корзине **может стать красно-чёрным деревом** (типичные пороги OpenJDK: treeify при 8 узлах и capacity >= 64; untreeify около 6). Порог resize ≈ `capacity * loadFactor` (часто `0.75`). При расширении таблица обычно удваивается, элементы перераспределяются. Это **детали реализации**, а не гарантии API.

```java
Map<String, Integer> counts = new HashMap<>();
counts.put("a", 1);
counts.merge("a", 1, Integer::sum); // 2
System.out.println(counts.getOrDefault("b", 0)); // 0
```

Три похожих приёма решают разные задачи:

```java
// Получить fallback, не изменяя map:
int current = counts.getOrDefault("b", 0);

// Счётчик или объединение старого и нового значения:
counts.merge("b", 1, Integer::sum);

// Лениво создать коллекцию значений для ключа:
Map<String, List<String>> groups = new HashMap<>();
groups.computeIfAbsent("team", ignored -> new ArrayList<>()).add("Alex");
```

У `merge` remapping-функция вызывается для существующего non-null значения; у `computeIfAbsent` mapping-функция вызывается при отсутствии значения. Эти функции должны быть короткими, без тяжёлого I/O и рекурсивного изменения той же карты.

- В среднем `get/put/remove` — O(1), худшие случаи хуже; хороший `hashCode` важен.
- `null`-ключ допустим один; `null`-значений сколько угодно. Итерационный порядок не гарантирован.
- `HashMap` **не потокобезопасен**; при общей записи нужна внешняя синхронизация или специализированная структура.

### ConcurrentHashMap и синхронизация

`ConcurrentHashMap` позволяет многим потокам читать и изменять таблицу без **одного глобального lock для всей map**. В современных OpenJDK используются volatile/CAS и при необходимости синхронизация на уровне корзины для некоторых обновлений; resize может помогаться несколькими потоками. Это **не старый `Segment[]`-дизайн Java 7**, который иногда ошибочно приводят на интервью.

- `get` обычно не блокирует чтение; операции обновления отдельного ключа имеют гарантии атомарности для своей операции (`putIfAbsent`, `compute`, `merge`). Итераторы **weakly consistent**: не бросают `ConcurrentModificationException` из-за параллельных изменений, но не являются единым снимком.
- **Не допускает `null` ключи и значения**: `null` от `get` однозначно означает отсутствие значения в конкурентной ситуации.
- `size()` при конкурентных изменениях может отражать переходное состояние.
- `map.get(k); map.put(k, updated)` — **не атомарный** составной протокол даже на `ConcurrentHashMap`.

```java
ConcurrentHashMap<String, Integer> map = new ConcurrentHashMap<>();
map.merge("clicks", 1, Integer::sum); // атомарный update для ключа
map.computeIfAbsent("views", k -> 0); // ленивое создание

// Высококонкурентный счётчик:
ConcurrentHashMap<String, LongAdder> counters = new ConcurrentHashMap<>();
counters.computeIfAbsent("pageA", k -> new LongAdder()).increment();

// Уникальные посетители по дате: и внешняя map, и внутренний set concurrent.
ConcurrentHashMap<LocalDate, Set<String>> visitors = new ConcurrentHashMap<>();
visitors.computeIfAbsent(
    LocalDate.now(),
    ignored -> ConcurrentHashMap.newKeySet()
).add("user-42");
```

**Что насчёт синхронизации?** `Collections.synchronizedMap(new HashMap<>())` оборачивает операции одним монитором; при итерации всё равно **нужно вручную синхронизироваться** на map. `ConcurrentHashMap` масштабируется лучше при конкурентных доступах, но сложный инвариант между *несколькими ключами* может требовать отдельной блокировки/транзакции. Атомарный `computeIfAbsent` не делает автоматически безопасным возвращённый mutable-объект: `ConcurrentHashMap<K, HashSet<V>>` всё ещё содержит непотокобезопасный `HashSet`. Mapping-функции `compute` делай короткими и без рекурсивного изменения той же карты.

Источник: [ConcurrentHashMap Java 17 API](https://docs.oracle.com/en/java/javase/17/docs/api/java.base/java/util/concurrent/ConcurrentHashMap.html).

</details>

## 17. Как устроен и работает HashSet?

<details>
<summary>Показать ответ</summary>

**`HashSet<E>`** реализует множество на базе `HashMap<E,Object>`: элементы хранятся как ключи, значения — общий служебный маркер. `add` фактически означает `map.put(element, PRESENT)`; `contains` — `map.containsKey(element)`. Поэтому уникальность определяется парой `equals/hashCode`, а не оператором `==`.

```java
Set<String> ids = new HashSet<>();
ids.add("a"); ids.add("a");
System.out.println(ids.size()); // 1
```

| Set | Порядок/уникальность | Стоимость | Особенности |
|---|---|---|---|
| **HashSet** | без гарантированного порядка; `equals/hashCode` | среднее O(1) | допускает `null`, нет синхронизации |
| **LinkedHashSet** | порядок вставки, уникальность по `equals/hashCode` | среднее O(1) | немного больше памяти, удобно для удаления дубликатов с сохранением порядка |
| **TreeSet** | сортировка `Comparable`/`Comparator`, уникальность по `compare(...) == 0` | O(log n) | `NavigableSet`: `floor/ceiling/lower/higher`, диапазоны |
| **EnumSet** | естественный порядок enum | очень быстро | компактное битовое представление; только одна enum-типизация, `null` нельзя |
| **CopyOnWriteArraySet** | порядок вставки при итерации | изменения O(n) | snapshot-итераторы, удобно для небольших редко изменяемых наборов слушателей |
| **ConcurrentSkipListSet** | отсортированный concurrent set | ожидаемое O(log n) | параллельная работа, weakly consistent итераторы |

```java
enum Role { USER, ADMIN, SUPPORT }
Set<Role> roles = EnumSet.of(Role.USER, Role.ADMIN);
Set<String> inOrder = new LinkedHashSet<>(List.of("b", "a", "b")); // List.of здесь допускает дубликаты
NavigableSet<Integer> ranks = new TreeSet<>(List.of(5, 1, 3));
System.out.println(ranks.floor(4)); // 3
```

`Set.of(...)` — неизменяемый набор, не принимает `null` и дубликаты **при создании** (`IllegalArgumentException` при дубликатах); порядок итерации не обещан. `Collections.unmodifiableSet(...)` — **обёртка-представление** (может меняться вместе с исходным набором), а `Set.copyOf(...)` создаёт неизменяемую коллекцию с оговорками по элементам.

**Потокобезопасный hash-set**: `ConcurrentHashMap.newKeySet()`; выбирай его для конкурентных обновлений без сортировки. **Подводный камень TreeSet:** comparator, который сравнивает только `age`, посчитает двух разных людей одного возраста дубликатами; для согласованности логики сравнения добавь tie-breaker `id`.

</details>

## 19. Почему нарушение equals/hashCode ломает HashMap?

<details>
<summary>Показать ответ</summary>

Поиск начинается с hash ключа, чтобы попасть в нужную корзину, а затем использует `equals`. Если два равных объекта дают **разные хеши**, поиск может пойти в другую корзину и вернуть `null`, хотя логически равный ключ уже добавлен.

```java
// Если equals сравнивает id, а hashCode — не связан с id,
// map.get(new Key(sameId)) может не найти запись.
```

Ещё одна ловушка — изменение значимого поля ключа **после** `put`: hash/equals меняются, а узел остаётся в прежней корзине. Решение: согласованная реализация двух методов и желательно неизменяемые ключи.

Контракт `equals` требует рефлексивности, симметричности, транзитивности, согласованности и `x.equals(null) == false`. Из равенства обязан следовать одинаковый `hashCode`; одинаковый hash сам по себе равенства не означает — коллизии допустимы.

```java
record UserKey(long id, String tenant) {}

Map<UserKey, String> users = new HashMap<>();
users.put(new UserKey(42, "eu"), "Alex");
System.out.println(users.get(new UserKey(42, "eu"))); // Alex
```

`record` удобен как неизменяемый value key, если семантика всех его компонентов действительно участвует в равенстве. У `TreeMap` другая важная грань: два ключа считаются эквивалентными для карты, если comparator возвращает `0`, даже если их `equals` возвращает `false`.

</details>
