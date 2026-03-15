# Home Budget Tracker

A simple and modern desktop application for tracking personal income and expenses. Built using **JavaFX** for the frontend and **Oracle Database** for the backend.

## 🚀 Features
- **Dashboard**: Overview of total income, expenses, and current balance.
- **Transactions**: Add, view, and delete transactions.
- **Analysis**: Visual reports including Pie, Bar, and Line charts.
- **Settings**: Change user password.
- **Security**: User login and signup system.

---

## 💻 Commands to Run the Application

Follow these steps to run the application directly from your terminal (**Command Prompt** or **PowerShell**).

### 1. Prerequisite: Database Setup
Ensure your Oracle Database XE is running and you have created the schema. You can run the schema script using SQL*Plus:
```cmd
sqlplus SYSTEM/joyal111@localhost:1521/xe @schema.sql
```

### 2. Run with Maven (Recommended)
If you have Maven installed, navigate to the project root directory and run:
```cmd
mvn javafx:run
```

### 3. Run using the bundled Maven (IntelliJ)
If you don't have Maven in your system PATH but have IntelliJ IDEA installed, you can use its bundled maven:
```cmd
"C:\Program Files\JetBrains\IntelliJ IDEA 2025.1.4.1\plugins\maven\lib\maven3\bin\mvn.cmd" javafx:run
```

---

## 🗄️ Database Commands (Terminal)

To view your data directly in the terminal using SQL*Plus:

### Login to SQL*Plus
```cmd
sqlplus SYSTEM/joyal111@localhost:1521/xe
```

### View all users
```sql
SELECT * FROM USERS;
```

### View all transactions
```sql
SELECT * FROM TRANSACTIONS;
```

### Reset/Update a user's password manually
```sql
UPDATE USERS SET PASSWORD = 'new_password' WHERE USERNAME = 'admin';
COMMIT;
```

---

## 🛠️ Technology Stack
- **Language**: Java 17+
- **UI Framework**: JavaFX 17
- **Database**: Oracle DB (XE)
- **Build Tool**: Maven
- **Styling**: Vanilla CSS (included in `src/main/resources/com/mybudg/style.css`)
