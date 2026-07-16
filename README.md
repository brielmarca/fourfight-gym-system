# 4Four Fight — Academy Management Platform

Production full-stack platform for 4Four Fight Academy. It centralizes student management, classes, memberships, payments and administrative routines in a responsive web application.

**Live:** [4fourfight.com](https://4fourfight.com/)

## Product overview

The platform replaces fragmented manual processes with a single environment for administrators and students.

- Role-based authentication and authorization
- Student profiles and membership management
- Class schedules and enrollment workflows
- Administrative dashboard and operational records
- Payment integration and transaction history
- Audit-oriented logging and protected routes
- Responsive interfaces for desktop and mobile

## Architecture

    Browser
       │
       ▼
    React + TypeScript
       │ REST / JWT
       ▼
    Spring Boot API
       │
       ▼
    PostgreSQL

The production environment is containerized with Docker and served through Nginx and Cloudflare on a Linux VPS.

## Technology

- **Back end:** Java 21, Spring Boot, Spring Security, JPA/Hibernate
- **Front end:** React, Vite, TypeScript
- **Database:** PostgreSQL
- **Infrastructure:** Docker, Docker Compose, Nginx, Linux and Cloudflare
- **Integrations:** Stripe payment workflows

## Local development

### Requirements

- Java 21
- Node.js 20+
- PostgreSQL
- Docker and Docker Compose (recommended)

### Setup

1. Clone the repository.
2. Copy the provided environment example and configure local values.
3. Start the database and supporting services.
4. Install front-end dependencies.
5. Run the API and web application.

    npm install
    npm run dev:full

Never commit production credentials or real customer data. Use separate secrets for each environment.

## Quality and documentation

The repository includes dedicated documentation for deployment, payments and operational setup. Before a production change, validate both applications, database migrations and the authentication flows for every role.

## Status

Active production project. Features and infrastructure continue to evolve according to the academy's operational needs.

## Author

Developed and maintained by [Gabriel Marca](https://github.com/brielmarca).
