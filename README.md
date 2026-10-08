============================================================
Company App — консольное приложение для работы с БД
============================================================

1. О ПРОЕКТЕ
------------
Простое консольное Java-приложение, которое подключается к PostgreSQL
и выводит список сотрудников компании с их должностями и департаментами.

Демонстрирует работу с реляционной БД:
- подключение через JDBC;
- SELECT с JOIN, GROUP BY и STRING_AGG;
- вывод результата в виде таблицы.


2. ТРЕБОВАНИЯ
-------------
- Java 17+
- Maven 3.8+
- PostgreSQL 14+
- JDBC-драйвер PostgreSQL 42.7.3 (подтягивается Maven'ом автоматически)


3. ПОДГОТОВКА БАЗЫ ДАННЫХ
-------------------------
1) Установите и запустите PostgreSQL.

2) Создайте базу данных:

       CREATE DATABASE dbforaxenixtest;

3) Подключитесь к базе и выполните DDL-скрипт ниже — он создаёт
   все необходимые таблицы (без данных).

   -- =========================================================
   -- Схема БД "dbforaxenixtest"
   -- Только структура, без данных.
   -- PostgreSQL. Без расширений и триггеров.
   -- =========================================================

   -- ---------- Справочник департаментов ----------
   CREATE TABLE department (
       id          UUID         PRIMARY KEY,
       code        VARCHAR(50)  NOT NULL UNIQUE,
       name        VARCHAR(255) NOT NULL,
       created_at  TIMESTAMP    NOT NULL DEFAULT NOW(),
       changed_at  TIMESTAMP    NOT NULL DEFAULT NOW()
   );

   -- ---------- Справочник должностей ----------
   CREATE TABLE position (
       id          UUID         PRIMARY KEY,
       code        VARCHAR(50)  NOT NULL UNIQUE,
       name        VARCHAR(255) NOT NULL,
       created_at  TIMESTAMP    NOT NULL DEFAULT NOW(),
       changed_at  TIMESTAMP    NOT NULL DEFAULT NOW()
   );

   -- ---------- Сотрудники ----------
   CREATE TABLE employee (
       id           UUID         PRIMARY KEY,
       first_name   VARCHAR(100) NOT NULL,
       last_name    VARCHAR(100) NOT NULL,
       surname      VARCHAR(100),
       birth_date   DATE         NOT NULL,
       email        VARCHAR(255) NOT NULL UNIQUE,
       position_id  UUID         NOT NULL REFERENCES position(id) ON DELETE RESTRICT,
       created_at   TIMESTAMP    NOT NULL DEFAULT NOW(),
       changed_at   TIMESTAMP    NOT NULL DEFAULT NOW(),
       deleted_at   TIMESTAMP
   );

   CREATE INDEX idx_employee_last_name   ON employee(last_name);
   CREATE INDEX idx_employee_position_id ON employee(position_id);

   -- ---------- M:N: сотрудник ↔ департамент ----------
   CREATE TABLE employee_department (
       employee_id   UUID NOT NULL REFERENCES employee(id)   ON DELETE CASCADE,
       department_id UUID NOT NULL REFERENCES department(id) ON DELETE RESTRICT,
       PRIMARY KEY (employee_id, department_id)
   );

   CREATE INDEX idx_emp_dept_department ON employee_department(department_id);

   -- ---------- Родительская таблица документов (JOINED) ----------
   CREATE TABLE document (
       id            UUID        PRIMARY KEY,
       employee_id   UUID        NOT NULL REFERENCES employee(id) ON DELETE RESTRICT,
       document_type VARCHAR(20) NOT NULL
                     CHECK (document_type IN ('PASSPORT', 'INN', 'SNILS')),
       created_at    TIMESTAMP   NOT NULL DEFAULT NOW(),
       changed_at    TIMESTAMP   NOT NULL DEFAULT NOW(),
       deleted_at    TIMESTAMP
   );

   CREATE INDEX idx_document_employee ON document(employee_id);
   CREATE INDEX idx_document_type     ON document(document_type);

   -- ---------- Паспорт ----------
   CREATE TABLE passport (
       id                   UUID         PRIMARY KEY REFERENCES document(id) ON DELETE CASCADE,
       series               VARCHAR(10),
       number               VARCHAR(20)  NOT NULL,
       date_of_issue        DATE         NOT NULL,
       issuing_organization VARCHAR(255) NOT NULL,
       UNIQUE (series, number)
   );

   -- ---------- ИНН ----------
   CREATE TABLE inn (
       id                   UUID         PRIMARY KEY REFERENCES document(id) ON DELETE CASCADE,
       number               VARCHAR(20)  NOT NULL UNIQUE,
       date_of_issue        DATE         NOT NULL,
       issuing_organization VARCHAR(255) NOT NULL
   );

   -- ---------- СНИЛС ----------
   CREATE TABLE snils (
       id                   UUID         PRIMARY KEY REFERENCES document(id) ON DELETE CASCADE,
       number               VARCHAR(20)  NOT NULL UNIQUE,
       date_of_issue        DATE         NOT NULL,
       issuing_organization VARCHAR(255) NOT NULL
   );


4. НАСТРОЙКА ПОДКЛЮЧЕНИЯ
------------------------
Параметры подключения лежат в файле:

       src/main/resources/application.properties

Пример содержимого:

       db.url=jdbc:postgresql://localhost:5432/dbforaxenixtest
       db.user=postgres
       db.password=postgres

При необходимости отредактируйте под своё окружение.
Если файл отсутствует — приложение использует значения по умолчанию
(см. класс com.javarush.bogomolov.axenix.config.DbConfig).


5. КАК ЗАПУСТИТЬ
----------------
Способ 1 — через Maven (рекомендуется):

       mvn clean package
       java -jar target/company-app-1.0.0.jar

Способ 2 — из IDE:

       Открыть проект как Maven-проект
       Запустить класс com.javarush.bogomolov.axenix.App

Способ 3 — только тесты:

       mvn test


6. КАКОЙ ЗАПРОС ВЫПОЛНЯЕТСЯ
---------------------------
Текст SQL-запроса хранится в классе:

       com.javarush.bogomolov.axenix.util.SQLcommands

При необходимости запрос можно изменить прямо там — приложение
подхватит новую версию при следующем запуске.

Текущий запрос выбирает неуволенных сотрудников и для каждого выводит:
- ФИО;
- должность (JOIN position);
- список департаментов, склеенный через запятую
  (M:N через employee_department → department, STRING_AGG);
- email.

Результат сортируется по фамилии и имени сотрудника.


7. СТРУКТУРА ПРОЕКТА
--------------------
src/
└── main/
    ├── java/
    │   └── com/javarush/bogomolov/axenix/
    │       ├── App.java                        — точка входа, вывод таблицы
    │       ├── config/
    │       │   └── DbConfig.java               — чтение application.properties
    │       ├── repository/
    │       │   ├── EmployeeDto.java            — DTO строки результата
    │       │   └── EmployeeRepository.java     — выполнение SQL через JDBC
    │       └── util/
    │           └── SQLcommands.java            — текст SQL-запроса
    └── resources/
        └── application.properties              — параметры подключения


8. ОБРАБОТКА ОШИБОК
-------------------
- application.properties отсутствует → используются значения по умолчанию.
- БД недоступна или неверные креды → сообщение в stderr, код выхода 1.
- Соединение, PreparedStatement и ResultSet закрываются автоматически
  (try-with-resources).


9. ПРИМЕР ВЫВОДА
----------------
Сотрудники: должности и департаменты

+---------------------+----------------------+----------------------+----------------------+
| ФИО                 | Должность            | Департаменты         | Email                |
+---------------------+----------------------+----------------------+----------------------+
| Аннова Анна Серг... | Тестировщик          | Разработка           | annova@company.com   |
| Артёмов Артём Пет...| Инженер-разработчик  | Разработка           | artemov@company.com  |
| Дмитриев Дмитрий ...| Менеджер проектов    | Маркетинг, Разработка| dmitriev@company.com |
| ...                 | ...                  | ...                  | ...                  |
+---------------------+----------------------+----------------------+----------------------+

Всего строк: 19

Авторство:
Богомолов Кирилл Владимирович
