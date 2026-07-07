# ShopSphere

ShopSphere is a single seller e-commerce backend built with Spring Boot. It covers catalog browsing, cart management, order flow, authentication, and admin reporting for a small storefront style application.

![Java 25](https://img.shields.io/badge/Java-25-orange)
![Spring Boot 4.1.0](https://img.shields.io/badge/Spring%20Boot-4.1.0-brightgreen)
![License MIT](https://img.shields.io/badge/License-MIT-yellow)
![Maven](https://img.shields.io/badge/Build-Maven-blue)
![H2](https://img.shields.io/badge/Database-H2-darkgray)

## Table of Contents

- [Features](#features)
- [Tech Stack](#tech-stack)
- [Architecture](#architecture)
- [Getting Started](#getting-started)
- [API Endpoints](#api-endpoints)
- [Authentication](#authentication)
- [Role Based Access](#role-based-access)
- [Testing](#testing)
- [Project Status](#project-status)
- [License](#license)

## Features

### Authentication and Authorization

- User registration and login
- JWT based authentication
- Role based access for Customer, Seller, and Admin
- Protected routes for account and order actions

### Product Catalog

- Create, read, update, and delete products
- Product filtering by category, price, stock, and status
- Product listing with pagination

### Shopping Cart

- Add items to cart
- Update quantities and remove items
- Batch cart updates
- Cart summary with estimated tax

### Order Management

- Create orders from the current cart
- Track order lifecycle states
- Cancel and review orders
- Order statistics for users

### Admin Dashboard

- Dashboard statistics for users, products, orders, and revenue
- Revenue reporting by date range
- Best selling products and low stock inventory views
- Administrative order browsing

### Data Seeding

- Seed users, products, carts, and orders for local development

## Tech Stack

- Java 25
- Spring Boot 4.1.0
- Spring Security
- Spring Data JPA
- JWT with JJWT 0.13.0
- H2 Database
- Lombok
- Maven
- JUnit 5
- Mockito

## Architecture

The application follows a layered structure with clear separation between concerns.

- Controller layer handles HTTP requests and responses
- Service layer contains the business logic and validation rules
- Repository layer persists entities through Spring Data JPA
- Model layer defines the domain entities
- DTO layer shapes request and response payloads
- Exception layer standardizes API errors
- Security layer handles JWT parsing and authorization

## Getting Started

### Prerequisites

- JDK 25
- Maven

### Setup

1. Clone the repository.
2. Open the project in your preferred Java IDE or terminal.
3. Set the JWT secret environment variable before running the app.

   Example on Windows PowerShell:

   ```powershell
   $env:JWT_SECRET="your-secret-key"
   ```

4. Run the application with the local profile.

   ```powershell
   .\mvnw spring-boot:run -Dspring-boot.run.profiles=local
   ```

5. The app uses H2 in memory and exposes the console at the default H2 path when running locally.

## API Endpoints

### Auth

| Method | Path            | Description                           | Access |
| ------ | --------------- | ------------------------------------- | ------ |
| POST   | /api/auth/login | Authenticate a user and receive a JWT | Public |

### Users

| Method | Path            | Description            | Access         |
| ------ | --------------- | ---------------------- | -------------- |
| POST   | /api/users      | Register a new account | Public         |
| GET    | /api/users      | List all users         | Admin          |
| GET    | /api/users/{id} | View a user profile    | Owner or Admin |
| PUT    | /api/users/{id} | Update a user profile  | Owner or Admin |
| DELETE | /api/users/{id} | Delete a user account  | Owner or Admin |

### Products

| Method | Path               | Description                               | Access |
| ------ | ------------------ | ----------------------------------------- | ------ |
| GET    | /api/products      | List products with filters and pagination | Public |
| GET    | /api/products/{id} | View a product by id                      | Public |
| POST   | /api/products      | Create a product                          | Admin  |
| PUT    | /api/products/{id} | Update a product                          | Admin  |
| DELETE | /api/products/{id} | Delete a product                          | Admin  |

### Cart

| Method | Path                         | Description                         | Access        |
| ------ | ---------------------------- | ----------------------------------- | ------------- |
| POST   | /api/carts                   | Add a product to the cart           | Authenticated |
| GET    | /api/carts                   | View the current cart               | Authenticated |
| PUT    | /api/carts/items             | Update cart item quantity           | Authenticated |
| DELETE | /api/carts/items/{productId} | Remove a cart item                  | Authenticated |
| DELETE | /api/carts                   | Clear the cart                      | Authenticated |
| POST   | /api/carts/batch             | Add several products in one request | Authenticated |
| GET    | /api/carts/summary           | View cart totals and estimated tax  | Authenticated |

### Orders

| Method | Path                    | Description                                | Access         |
| ------ | ----------------------- | ------------------------------------------ | -------------- |
| POST   | /api/orders             | Create an order from the cart              | Authenticated  |
| GET    | /api/orders/me          | List the current user's orders             | Authenticated  |
| GET    | /api/orders/me/stats    | View order statistics for the current user | Authenticated  |
| PATCH  | /api/orders/{id}/status | Update an order status                     | Admin          |
| PATCH  | /api/orders/{id}/cancel | Cancel an order                            | Owner or Admin |

### Admin

| Method | Path                     | Description                         | Access |
| ------ | ------------------------ | ----------------------------------- | ------ |
| GET    | /api/admin/dashboard     | View the admin dashboard summary    | Admin  |
| GET    | /api/admin/revenue       | View revenue metrics                | Admin  |
| GET    | /api/admin/best-selling  | View best selling products          | Admin  |
| GET    | /api/admin/low-stock     | View low stock products             | Admin  |
| GET    | /api/admin/recent-orders | View recent orders                  | Admin  |
| GET    | /api/admin/orders        | Browse orders with optional filters | Admin  |

### Seed

| Method | Path               | Description          | Access |
| ------ | ------------------ | -------------------- | ------ |
| POST   | /api/seed/users    | Seed sample users    | Public |
| POST   | /api/seed/products | Seed sample products | Public |
| POST   | /api/seed/carts    | Seed sample carts    | Public |
| POST   | /api/seed/orders   | Seed sample orders   | Public |

## Authentication

Authentication uses JWT bearer tokens. A user registers or logs in, receives a token from the auth endpoint, and includes it in the Authorization header as a Bearer token for protected requests.

## Role Based Access

The application currently supports three roles.

- Customer can browse products, manage their own cart, place orders, and review their own account data.
- Seller can manage catalog content and product availability in this single seller setup.
- Admin can oversee the full storefront, review orders, check reporting, and manage inventory and user data.

## Testing

The unit test suite covers the main service layer with JUnit 5 and Mockito. The project is set up so the core business rules can be tested independently from the web layer.

## Project Status

This project is a portfolio application rather than a production ready storefront. It is still in active development and the immediate roadmap includes Docker support, a PostgreSQL migration, Actuator monitoring, and eventually a frontend experience.

## License

This project is documented under the MIT license.
