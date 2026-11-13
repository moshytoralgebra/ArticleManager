# Article Manager

A desktop application for importing, organizing, and managing articles from RSS feeds. Built with **Java, Java Swing, and Microsoft SQL Server**, the project combines a graphical user interface with a layered data-access structure and reusable utility modules.

The application supports article management, author and category administration, RSS feed import, image handling, XML export, and basic user-role functionality.

## Features

### RSS article import
- Import articles from a configured RSS feed (currently NASA's RSS feed).
- Parse article titles, links, descriptions, and publication dates.
- Extract image URLs from common RSS image fields and download available images locally.
- Filter duplicate article links within an imported feed batch.
- Display imported articles in the application.

### Article management
- Create, view, update, and delete article records.
- Store article titles, links, descriptions, publication dates, image paths, categories, and author associations.
- Upload local images in JPG, JPEG, and PNG formats.
- Preview article images in the editor.
- Assign an author to an article.
- Search articles by title keyword.
- Filter articles by category.
- Sort article titles alphabetically (A–Z or Z–A).
- Limit the number of displayed results.
- View article statistics, including article and author counts, shortest and longest titles, and image availability.
- Select a random article with the **Surprise me** action.

### Author and category management
- Create, edit, view, and delete authors.
- Store author names, biographies, and email addresses.
- Create, edit, view, and delete categories.
- Store category names and descriptions.

### XML export
- Export the currently loaded article collection to an XML file.
- Serialize article fields using Jakarta XML Binding (JAXB).

### User accounts and administration
- Register a user account and log in.
- Distinguish between regular users and administrators.
- Show the **Wipe All Data** action only to an administrator.
- Run the destructive database operation in a background worker and refresh the article views afterwards.

## Tech Stack

| Category | Technologies |
|---|---|
| Language | Java 20 |
| Desktop UI | Java Swing |
| UI look and feel | FlatLaf |
| Build system | Apache Maven |
| Database | Microsoft SQL Server |
| Database access | JDBC, Microsoft SQL Server JDBC Driver |
| XML processing | Jakarta XML Binding (JAXB), DOM |
| Data processing | Java Streams, Collections, Functional Interfaces |
| Background tasks | SwingWorker |

## Architecture

The repository is organized as a Maven multi-module project:

```text
ArticleManager/
│
├── pom.xml
│
├── ArticleManager/
│   ├── pom.xml
│   └── src/main/java/hr/algebra/
│       ├── ArticleManager.java
│       ├── LoginFrame.java
│       ├── LoginPanel.java
│       ├── RegisterPanel.java
│       └── view/
│           ├── UploadArticlesPanel.java
│           ├── EditArticlesPanel.java
│           ├── EditAuthorsPanel.java
│           ├── EditCategoriesPanel.java
│           └── model/
│
├── Dao/
│   ├── pom.xml
│   └── src/main/java/hr/algebra/
│       ├── dal/
│       │   ├── Repository.java
│       │   ├── RepositoryFactory.java
│       │   └── sql/
│       └── model/
│
├── Utilities/
│   ├── pom.xml
│   └── src/main/java/hr/algebra/utilities/
│       ├── ArticleService.java
│       ├── RssParser.java
│       ├── FileUtils.java
│       ├── JaxbUtils.java
│       └── ...
│
├── init.sql
└── delete_all.sql
```

### Module responsibilities

- **ArticleManager** — Desktop application entry point, login and registration screens, tabbed interface, article editing and import views, and administrative actions.
- **Dao** — Domain models, repository interfaces, SQL Server data source configuration, and JDBC implementations that communicate with the database through stored procedures.
- **Utilities** — RSS parsing, image and file operations, XML serialization, user messages, and reusable article-processing operations.

The article repository is accessed through a repository interface and a configurable repository factory. This separates the application UI from the concrete SQL Server implementation.

## Database

The application uses a SQL Server database named `ARTICLES`.

The database schema and stored procedures are defined in `init.sql`. The script creates the database and the main tables:

| Table | Purpose |
|---|---|
| `Article` | Stores article metadata, publication date, image path, and author/category references |
| `Author` | Stores author names, biographies, and email addresses |
| `Category` | Stores article categories and descriptions |
| `Users` | Stores usernames, passwords, and roles |

Articles can reference an author and a category. The database script also defines stored procedures for article, author, category, and user operations.

## Getting Started

### Prerequisites

- Java Development Kit (JDK) 20
- Apache Maven
- Microsoft SQL Server
- An SQL Server instance accessible from the machine running the application

### 1. Clone the repository

```bash
git clone https://github.com/moshytoralgebra/ArticleManager.git
cd ArticleManager
```

### 2. Create the database

Open SQL Server Management Studio (SSMS) or another SQL Server client, then execute the `init.sql` script.

The script creates the `ARTICLES` database, tables, stored procedures, and an initial administrator account for local development.

**Important:** The SQL script currently seeds the administrator with the username `admin` and password `admin`. These are development-only credentials. Do not use them in a real environment; change the seed credentials before running the script outside a local setup.

### 3. Configure the database connection

Update:

`Dao/src/main/resources/config/db.properties`

with the connection details for your SQL Server instance:

```properties
SERVER_NAME = localhost
DATABASE_NAME = ARTICLES
USER = your_sql_server_username
PASSWORD = your_sql_server_password
```

The repository currently contains local database credentials in this properties file. Replace them with your own values and do not commit real credentials to source control.

The repository implementation is selected through:

`Dao/src/main/resources/config/repository.properties`

The current configuration uses:

```properties
CLASS_NAME = hr.algebra.dal.sql.SqlRepository
```

### 4. Build the project

From the repository root, run:

```bash
mvn clean install
```

Maven builds the three modules and resolves their dependencies.

### 5. Run the application

The main class is:

`hr.algebra.ArticleManager`

You can run it from an IDE by opening the root Maven project and launching the main class, or run the packaged application with the appropriate module classpath.

## Application Workflow

1. Launch the application to open the login screen.
2. Log in with an existing account or register a new user.
3. Use the **Upload articles** tab to import articles from the configured RSS feed.
4. Open **Edit articles** to search, filter, sort, inspect, add, update, or delete articles.
5. Manage authors and categories in their respective tabs.
6. Use **Actions → Export XML** to export the currently loaded article list.
7. Administrators can use **Admin → Wipe All Data** to remove all article records from the database.

## Technical Concepts Demonstrated

- Object-oriented programming and Java class design.
- Multi-module Maven project structure and dependency management.
- Layered architecture and separation of concerns.
- Repository pattern and repository factory.
- JDBC database access and SQL Server stored procedures.
- CRUD operations and relational data modelling.
- Java Swing layouts, event handling, and table models.
- Background processing with `SwingWorker`.
- RSS feed retrieval and XML parsing with DOM.
- XML serialization with JAXB.
- File handling and local image storage.
- Java Streams, lambdas, predicates, consumers, comparators, and collectors.
- Form validation and user-role-based UI functionality.

## Implementation Notes

This is an academic desktop application and is not intended to be deployed as-is in a production environment.

- **Credentials and password storage:** The current database configuration contains local connection credentials, and the SQL setup script creates a default administrator account. The current user implementation stores and compares passwords directly rather than using a password-hashing mechanism. Replace these approaches before any real-world use.
- **Database configuration:** Connection settings are loaded from a properties file packaged with the DAO module. A production setup should keep credentials outside the repository and packaged application.
- **Article update procedure:** The current JDBC article-update call and the `updateArticle` stored procedure in `init.sql` have different parameter lists. They need to be aligned for article updates to work reliably against a database created from the included script.
- **RSS parsing:** The feed URL is currently configured directly in the upload panel. The parser supports common RSS item fields and image metadata, but RSS feeds vary in structure and not every item will contain a usable image.

## Author

**Mariia Oshytor**

[GitHub Profile](https://github.com/moshytoralgebra) · [Repository](https://github.com/moshytoralgebra/ArticleManager)
