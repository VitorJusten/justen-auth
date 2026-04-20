# Justen Auth

**Justen Auth** is an authentication and authorization server built with **Spring Boot**.
It provides secure login, OAuth2 authorization, JWT token generation, and optional 2FA support via email.

The goal of this project is to provide a **ready-to-use authentication service** that can be integrated into web applications, APIs, and microservice architectures.

---

# Features

* 🔐 Secure authentication with **Spring Security**
* 🔑 **OAuth2 Authorization Server**
* 🪪 **JWT token generation and validation**
* 🧑‍💻 Custom **login interface using Thymeleaf**
* 🗄️ **PostgreSQL database support**
* 📦 Database migrations using **Liquibase**
* 📧 Optional **2FA via email**
* 📊 **Actuator monitoring and observability**
* 📚 **Swagger / OpenAPI documentation**
* ✅ Input validation with **Bean Validation**
* ⚡ Development support with **Spring DevTools**

---

# Tech Stack

* **Java 25**
* **Spring Boot 3.5**
* **Spring Security**
* **Spring Authorization Server (OAuth2)**
* **Spring Data JPA**
* **PostgreSQL**
* **Liquibase**
* **Thymeleaf**
* **Spring Mail**
* **Spring Actuator**
* **Swagger / OpenAPI**
* **Lombok**
* **Apache HttpClient 5**

---

# Architecture Overview

Justen Auth works as a **central authentication server**.

Applications authenticate users through this service and receive **JWT tokens** that can be used to access protected APIs.

Typical flow:

1. User opens the login page
2. Credentials are validated
3. Optional 2FA verification (email)
4. Authorization server issues a **JWT access token**
5. Client application uses the token to access protected APIs

---

# Requirements

* **Java 25**
* **Maven**
* **PostgreSQL**

---

# Installation

Clone the repository:

```bash
git clone https://github.com/VitorJusten/justen-auth.git
cd justen-auth
```

Configure your database in `application.yml` or `application.properties`.

Example:

```properties
spring.datasource.url=jdbc:postgresql://localhost:5432/authdb
spring.datasource.username=postgres
spring.datasource.password=yourpassword
```

Run the project:

```bash
mvn spring-boot:run
```

---

## API Endpoints

#### GET `/login`

Returns the **login page** rendered with **Thymeleaf**.

Used by the authentication flow to allow users to enter credentials.

---

# User Endpoints

Base path: `/user`

### POST `/user`

**User self-registration**

Creates a new user account using authentication input data.

Used for **public sign-up**.

---

### POST `/user/create`

**Admin user creation**

Creates a new user using full user input data.

Typically used by **administrators or system services**.

---

### GET `/user`

Returns a **paginated list of users**.

Access restriction:

* `ADM`
* `DEV`

Pagination parameters are supported via Spring `Pageable`.

---

### GET `/user/{id}`

Returns user details by **UUID**.

---

### GET `/user/username/{username}`

Returns a user by **username**.

Used internally by the authentication process.

---

### PUT `/user/{id}`

Updates user information.

Permission rules:

* Users can **update their own account**
* `ADM` or `DEV` roles can update **any user**

Passwords are automatically **encoded before saving**.

---

### DELETE `/user/{id}`

Deletes a user.

Permission rules:

* Users can **delete their own account**
* `ADM` or `DEV` can delete **any user**

---

### PATCH `/user/{id}/password`

Updates the user password.

Permission rules:

* Users can change **their own password**
* `ADM` or `DEV` can change **any password**

Passwords are **securely encoded** using Spring Security.

Parameter:

```
password
```

---

### PATCH `/user/{id}/lock`

Locks a user account.

Permission rules:

* User themselves
* `ADM` or `DEV`

Optional parameter:

```
until (OffsetDateTime)
```

If provided, the account remains locked **until the specified date**.

---

### PATCH `/user/{id}/unlock`

Unlocks a previously locked account.

Permission rules:

* User themselves
* `ADM` or `DEV`

---

# Role Endpoints

Base path: `/role`

These endpoints manage **system roles**.

Most operations require:

* `ADM`
* `DEV`

---

### GET `/role`

Returns all roles.

Access restriction:

* `ADM`
* `DEV`

---

### GET `/role/{id}`

Returns a role by **UUID**.

---

### POST `/role`

Creates a new role.

Access restriction:

* `ADM`
* `DEV`

---

### PUT `/role/{id}`

Updates an existing role.

Access restriction:

* `ADM`
* `DEV`

---

### DELETE `/role/{id}`

Deletes a role.

Access restriction:

* `ADM`
* `DEV`

---

# Security Notes

* Passwords are **encoded using Spring Security PasswordEncoder**
* Role validation is enforced internally using `SecurityUtils`
* Some operations allow users to manage **their own accounts**
* Administrative operations require **ADM or DEV roles**

---

# Database Migrations

Database migrations are handled automatically using **Liquibase**.

Changelog files are located in:

```
src/main/resources/db/changelog
```

---

# Login Interface

The project includes a **Thymeleaf-based login page** that integrates with the authentication system.

Any application using this project must **maintain the author credit visible in the login screen** as defined in the license.

---

# License

This project is free to use, modify, and distribute.

However, **credit to the original author must be preserved**, including visible credit in the login screen.

Author:
GitHub: https://github.com/VitorJusten

See `license.txt` for full details.

---

# Author

**Vitor Justen**

GitHub:
https://github.com/VitorJusten

---

# Contributions

Contributions, issues, and feature requests are welcome.
