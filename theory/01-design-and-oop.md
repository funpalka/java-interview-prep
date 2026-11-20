# ООП и принципы проектирования

> Вопросы из исходного списка: 1, 6, 7, 8, 18. Сначала ответь вслух, потом раскрой блок.

## 1. ООП, SOLID, DRY/YAGNI и trade-offs

<details>
<summary>Показать ответ</summary>

**ООП** — способ строить программу из объектов с состоянием и поведением. Четыре распространённых понятия: **инкапсуляция** (контроль доступа и инвариантов), **абстракция** (показываем существенный контракт), **полиморфизм** (один контракт — разные реализации), **наследование** (отношение *is-a*, повторное использование и подстановка). Наследование не является обязательным для хорошего ООП.

```java
interface PaymentGateway { void pay(long cents); }
class CardGateway implements PaymentGateway {
    public void pay(long cents) { /* запрос провайдеру */ }
}
class Checkout {
    private final PaymentGateway gateway;
    Checkout(PaymentGateway gateway) { this.gateway = gateway; }
    void complete(long cents) { gateway.pay(cents); }
}
```

**SOLID**:

| Принцип | Смысл | Пример нарушения / исправления |
|---|---|---|
| SRP | Одна причина измениться | `OrderService` одновременно считает скидки, пишет SQL и шлёт письма → выделить отдельные компоненты по причинам изменения |
| OCP | Расширяем поведение без постоянного редактирования стабильного кода | `switch` по 20 способам оплаты → интерфейс `PaymentGateway` и реализации, если расширения действительно часты |
| LSP | Подтип корректно подставляется вместо базового типа | `ReadOnlyList extends MutableList`, бросающая `UnsupportedOperationException` при ожидаемой записи, нарушает ожидания контракта |
| ISP | Клиент зависит только от нужного интерфейса | Не заставлять `ReportReader` реализовывать `deleteReport()` |
| DIP | Высокоуровневая бизнес-логика зависит от абстракции, а не конкретного SQL-класса | Внедрять `OrderRepository`, а не создавать `MySqlOrderRepository` внутри сервиса |

**DRY** — не дублировать *знание/правила*: два одинаковых фрагмента могут пока иметь разные причины измениться. **YAGNI** — не вводить возможности «на всякий случай». Дополнительно **KISS** — предпочитать самое простое решение, которое удовлетворяет требованиям.

**Trade-offs:** принципы не являются законами. Для маленького одноразового скрипта интерфейс с одной реализацией и пять фабрик могут только усложнить чтение (**YAGNI против абстрагирования**). Два похожих блока в разных доменах иногда лучше оставить раздельными (**намеренное дублирование против DRY**): при будущих изменениях они разойдутся. Излишне дробить классы ради SRP тоже дорого — больше переходов и связей. Критерии: вероятность изменений, стоимость тестов, ясность кода, скорость разработки и обратимость решения.

**Уточнение:** «Можно ли нарушить OCP ради простоты?» Да, например, `switch` по двум стабильным вариантам часто понятнее преждевременной иерархии. Обоснуй цену и признаки, после которых проведёшь рефакторинг.

</details>

## 6. Композиция и агрегация

<details>
<summary>Показать ответ</summary>

**Композиция** — отношение *has-a*, при котором владелец управляет жизненным циклом частей (семантическое понятие моделирования, а не специальное ключевое слово Java). **Агрегация** — более слабое *has-a*: связанные объекты могут существовать отдельно и использоваться совместно.

```java
// Композиция: корзина сама создаёт и контролирует свои позиции.
class Cart {
    private final List<CartItem> items = new ArrayList<>();
    void add(String sku, int quantity) {
        items.add(new CartItem(sku, quantity));
    }
}
record CartItem(String sku, int quantity) {}

// Агрегация: преподаватели живут независимо от курса.
class Course {
    private final List<Teacher> teachers;
    Course(List<Teacher> teachers) { this.teachers = List.copyOf(teachers); }
}
record Teacher(String name) {}
```

В обоих случаях в Java это обычные ссылки; разница в **владении и жизненном цикле**, а не в синтаксисе. `List.copyOf` создаёт независимый список ссылок (не глубокие копии `Teacher`). Композиция часто предпочтительнее наследования: она уменьшает связанность и позволяет заменять поведение через делегирование; но её не надо путать с DI.

</details>

## 7. Переопределение vs перегрузка методов

<details>
<summary>Показать ответ</summary>

**Overloading (перегрузка)** — одно имя, разные списки параметров; выбор сигнатуры обычно делается **компилятором** на основе статических типов аргументов. **Overriding (переопределение)** — подкласс даёт новую реализацию унаследованного экземплярного метода; при вызове используется **динамическая диспетчеризация** по реальному типу объекта.

```java
class Animal {
    String sound() { return "?"; }
}
class Dog extends Animal {
    @Override String sound() { return "woof"; }
}
class Printer {
    void print(Animal x) { System.out.println("animal"); }
    void print(Dog x) { System.out.println("dog"); }
}
Animal pet = new Dog();
System.out.println(pet.sound()); // woof: overriding
new Printer().print(pet);        // animal: overloading по типу Animal
```

| Правило | Перегрузка | Переопределение |
|---|---|---|
| Параметры | должны отличаться | одинаковая сигнатура (с правилами subsignature) |
| Возвращаемый тип | одного его изменения недостаточно | может быть ковариантным |
| Видимость | независима | нельзя уменьшать |
| Checked exceptions | независимы | нельзя расширять набор checked-исключений по контракту |
| `private`/`static` | перегружать можно | `private` не наследуется; `static` скрывается, не переопределяется |
| `final` | можно перегрузить | нельзя переопределить |

Подводный камень: `print(null)` может выбрать наиболее специфичную перегрузку или оказаться неоднозначным; `@Override` помогает обнаружить ошибку в сигнатуре.

</details>

## 8. Может ли статический метод быть переопределён или перегружен?

<details>
<summary>Показать ответ</summary>

**Статический метод можно перегрузить**, но **нельзя полиморфно переопределить**. Если подкласс объявляет статический метод с той же сигнатурой, это *method hiding* (скрытие). Выбор идёт по **типу выражения**, а не по реальному объекту.

```java
class Parent {
    static String name() { return "parent"; }
    static void log(String msg) {}
    static void log(int code) {} // overload
}
class Child extends Parent {
    static String name() { return "child"; } // hiding
}
Parent p = new Child();
System.out.println(p.name());     // parent (хотя объект Child)
System.out.println(Child.name()); // child
```

Писать `p.name()` разрешается, но вводит в заблуждение: для static предпочтительно `Parent.name()`. Попытка пометить `Child.name()` аннотацией `@Override` не скомпилируется. Экземплярный и статический методы нельзя смешать для одной унаследованной сигнатуры.

</details>

## 18. OrderService создаёт MySqlOrderRepository напрямую. Что нарушено?

<details>
<summary>Показать ответ</summary>

```java
class OrderService {
    private final MySqlOrderRepository repo = new MySqlOrderRepository();
}
```

Здесь нарушается прежде всего **DIP** (бизнес-логика зависит от конкретной инфраструктуры), а также затрудняется *dependency injection*. Сервис сам выбирает и создаёт зависимость, значит сложнее подменить её на fake/mock, протестировать ошибку БД, заменить источник данных и управлять жизненным циклом ресурсов. **Не любое `new` — нарушение DIP**: создавать простой `Money` или DTO внутри метода обычно нормально.

```java
interface OrderRepository {
    Optional<Order> findById(long id);
}
record Order(long id) {}
class OrderService {
    private final OrderRepository repo;
    OrderService(OrderRepository repo) { this.repo = Objects.requireNonNull(repo); }
    boolean exists(long id) { return repo.findById(id).isPresent(); }
}

// Unit-тест без БД и Spring:
OrderRepository fake = id -> Optional.of(new Order(id));
OrderService service = new OrderService(fake);
assert service.exists(42);
```

В последнем примере `OrderRepository` — функциональный интерфейс (один абстрактный метод), поэтому его можно реализовать лямбдой. В production composition root (например, Spring Configuration) создаёт `MySqlOrderRepository` и передаёт его сервису. **Уточнение:** интерфейсы тоже не обязательны для каждого класса; важно отделить бизнес-правила от дорогой/нестабильной зависимости.

</details>
