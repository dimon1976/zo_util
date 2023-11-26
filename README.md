# zoom_util

 Утилиты для обработки файлов zoomos (поддержка чтения одного листа в книге .xlsx)

### Файлы статистики для клиента Detmir.ru-2

 Из оригинального файла статистики в выходном файле оставляем только необходимые столбцы.

### ВПР в т.ч. по штрихкодам перечисленным через запятую в одном поле 

* Для правильной работы данные должны быть на одном листе
* Столбец A - Id клиента кому добавляем ссылки
* Столбец B - BAR. Все ненужные знаки игнорируются, некорректные ШК (!=13) игнорируются. 
Допускается несколько ШК через запятую в одной ячейке
* Столбец D - Bar где ищем. Все ненужные знаки игнорируются, некорректные ШК (!=13) игнорируются.
  Допускается несколько ШК через запятую в одной ячейке
* Столбец E - Ссылка

### Обработка отчетов для megatop.by

Пока работает в тестовом режиме.

### Обработка заданий от клиента lenta.com (edeadeal.ru)
**Формат столбцов в файле задания:** 

Список СКЮ - ШК - Пересчет
## вкладка список СКЮ
![]()
Товара(ID) - Наименование - Цена - Москва - Ростов - СПБ - Новосибирск - Екатеринбург - Саратов
## вкладка ШК
![]()
Товара(ID) - Наименование - EAN (BAR)
## Вкладка Пересчет
![]()
Товара(ID) - Наименование - Вес

### Обработка отчетов для simple.ru (основной)
Удаляются ссылки на скрины по сайтам, где выкачка из API


### TO-DO
1. [ ] Refactor классов домена в один product
2. [ ] добавить обработку выгрузок ecoop.ee
3. [ ] добавить обработку отчетов av.ru
4. [x] добавить выбор даты для отсечения в отчетах для ленты (едадил)
5. [x] добавить обработку файлов со ссылками для импорта (ссылки в разных столбцах)
6. [x] Сделать объединение файлов.
7. [x] Сделать multiple обработку файлов для megatop.by.
8. [x] разобраться с заголовком и пустыми столбцами при отметке чекбоксов
9. [ ] придумать способ динамического формирования заголовка
10. [ ] причесать код
11. [x] По отчетам мегатоп, присвоить номер и сохранять отчеты в базу потом выгружать одним файлом.
12. [x] Пофиксить ошибку - getoutputstream() has already been called for this response spring bo
13. [ ] Сделать загрузку справочника и обработку отчетов азбуки
14. [ ] Реализовать многопоточность обработки файлов