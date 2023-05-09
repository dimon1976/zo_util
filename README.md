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
#### To-do
* Сделать объединение файлов.

### Обработка заданий от клиента lenta.com (edeadeal.ru)
**Формат столбцов в файле задания:** 

Список СКЮ - ШК - Пересчет
## вкладка список СКЮ
![](D:\Job\Скриншоты\2023-05-09_14-29-36.png)
Товра(ID) - Наименование - Цена - Москва - Ростов - СПБ - Новосибирск - Екатеринбург - Саратов
## вкладка ШК
![](D:\Job\Скриншоты\2023-05-09_14-31-47.png)
Товра(ID) - Наименование - EAN (BAR)
## вкладка Пересчет
![](D:\Job\Скриншоты\2023-05-09_14-32-41.png)
Товра(ID) - Наименование - Вес
