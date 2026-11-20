# Java Interview Prep — теория

39 вопросов со скрытыми ответами. Сначала сформулируй ответ вслух, затем раскрой `Показать ответ` и сравни с конспектом. Номера соответствуют исходному списку.

| Раздел | Вопросы |
|---|---|
| [ООП и проектирование](01-design-and-oop.md) | 1, 6, 7, 8, 18 |
| [Типы и строки](02-java-basics-and-strings.md) | 2, 3, 4, 5, 11, 13, 15, 20 |
| [JVM и коллекции](03-jvm-and-collections.md) | 9, 10, 12, 14, 16, 17, 19 |
| [Stream API и лямбды](04-streams-and-lambdas.md) | 21–26, 28–31, 38 |
| [Исключения](05-exceptions.md) | 27, 32, 33, 37, 39 |
| [Тестирование](06-testing.md) | 34–36 |

## Как повторять

1. Открой вопрос, не раскрывая ответ.
2. Дай ответ вслух за 1–2 минуты и приведи пример.
3. Раскрой блок и отметь, что пропустил.
4. Повтори сложные вопросы через 1, 3 и 7 дней.

**Платформа:** GitHub отображает `<details>`/`<summary>` как раскрывающийся блок. В Notion Markdown можно импортировать, но HTML `details` может не преобразоваться в нативный toggle; для Notion может потребоваться конвертация.

## Первоисточники

- [GitHub: collapsed sections](https://docs.github.com/en/get-started/writing-on-github/working-with-advanced-formatting/organizing-information-with-collapsed-sections)
- [Java SE 21 — JLS](https://docs.oracle.com/javase/specs/jls/se21/html/index.html)
- [Java SE 21 — API](https://docs.oracle.com/en/java/javase/21/docs/api/index.html)
- [Java SE 21 — Garbage Collection](https://docs.oracle.com/en/java/javase/21/gctuning/)
- [Notion: importing data](https://www.notion.com/help/import-data-into-notion)

## Примечания к расширенной версии

- Вопросы сохранены с исходными номерами; подробные ответы находятся внутри раскрывающихся блоков.
- Примеры ориентированы на Java 21. Фрагменты, иллюстрирующие отдельный принцип, иногда приведены как части класса/теста, а не самостоятельные запускаемые программы.
- Материалы **про JVM, GC и коллекции** различают гарантии Java API и детали реализации HotSpot/OpenJDK: детали могут меняться между версиями.
- Зависимости JUnit/Mockito в примере зафиксированы как учебный вариант; перед созданием реального проекта можно выбрать подходящие поддерживаемые версии.
- Файлы с heap dump могут содержать секреты и персональные данные — не загружай их в GitHub.
