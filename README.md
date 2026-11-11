# Smart College Canteen Monitoring System

A Java 17 Servlet/JSP MVC application backed by MySQL. It uses Jakarta Servlet 6, JSP/JSTL, plain JDBC DAOs, and a responsive HTML/CSS/JavaScript interface. No Spring or ORM is used.

## Requirements

- JDK 17+
- Maven 3.8+
- MySQL 8+
- A Jakarta EE 10-compatible servlet container, such as Tomcat 10.1+

## Database setup

Run these scripts in order from a MySQL client:

```sql
SOURCE db/schema.sql;
SOURCE db/sample_data.sql;
```

The scripts create `college_canteen_db`, schema, sample menu items, and demo accounts. The default local connection is `root` / `root`; override it in the runtime environment if needed:

- `CANTEEN_DB_URL` (defaults to `jdbc:mysql://localhost:3306/college_canteen_db?useSSL=false&allowPublicKeyRetrieval=true&serverTimezone=UTC`)
- `CANTEEN_DB_USER` (defaults to `root`)
- `CANTEEN_DB_PASSWORD` (defaults to `root`)

## Build and run

```powershell
mvn clean package
```

Deploy `target/college-canteen.war` to Tomcat 10.1+ and open `http://localhost:8080/college-canteen/`. The MySQL Connector/J driver is packaged with the WAR; the servlet API is provided by Tomcat.

## Demo accounts

- Canteen administrator: `admin` / `admin123`
- Student: `student` / `student123`

Seed passwords are upgraded to salted PBKDF2 hashes after the first successful login. New student registrations are hashed before persistence.

## Features

- Student registration and login, menu search/category filtering, cart, checkout, order tracking, and cancellation of pending/accepted orders.
- Admin dashboard, order status workflow, menu/stock CRUD, low-stock alerts, top sellers, and recent sales analytics.
- Order placement validates current inventory and records order, line items, payment state, and stock updates in one database transaction.
- `DEMO_ONLINE` is explicitly a simulated payment option; it does not connect to a payment provider or collect payment details. Cash on pickup is also available.
