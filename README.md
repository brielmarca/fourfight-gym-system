<h1 align="center">4Four Fight Academy</h1>
<h3 align="center">Production-ready academy management platform built with Java, Spring Boot, React and TypeScript</h3>

<p align="center">
  <a href="https://4fourfight.com/"><img src="https://img.shields.io/badge/Live%20Application-View%20Project-7C3AED?style=for-the-badge&logo=cloudflare&logoColor=white" alt="View the live 4Four Fight Academy application" /></a>
  <a href="https://github.com/brielmarca"><img src="https://img.shields.io/badge/Developer-Gabriel%20Marca-181717?style=for-the-badge&logo=github&logoColor=white" alt="Gabriel Marca on GitHub" /></a>
</p>

> A complete full-stack platform developed for a real martial arts academy, centralizing student management, classes, memberships, payments and administrative operations.

---

## Demonstration

![4Four Fight Academy home page](./assets/home-page.svg)

## Project Overview

4Four Fight Academy replaces fragmented manual processes with a centralized digital environment for administrators and students. The application was designed as a real production system, covering the complete lifecycle from authentication and business rules to deployment, security and ongoing maintenance.

## Main Features

- [x] Secure authentication with protected routes and role-based access control
- [x] Student profiles, memberships and academy management
- [x] Class schedules, enrollment and attendance workflows
- [x] Administrative dashboards and operational records
- [x] Payment workflows and transaction history
- [x] Responsive interfaces for desktop, tablet and mobile devices

## Technology Stack

### Backend

<p>
  <img src="https://img.shields.io/badge/Java-21-ED8B00?style=for-the-badge&logo=openjdk&logoColor=white" alt="Java 21" />
  <img src="https://img.shields.io/badge/Spring%20Boot-3.x-6DB33F?style=for-the-badge&logo=springboot&logoColor=white" alt="Spring Boot" />
  <img src="https://img.shields.io/badge/Spring%20Security-Security-6DB33F?style=for-the-badge&logo=springsecurity&logoColor=white" alt="Spring Security" />
  <img src="https://img.shields.io/badge/JPA-Hibernate-59666C?style=for-the-badge&logo=hibernate&logoColor=white" alt="JPA and Hibernate" />
  <img src="https://img.shields.io/badge/Maven-Build-C71A36?style=for-the-badge&logo=apachemaven&logoColor=white" alt="Maven" />
</p>

### Frontend

<p>
  <img src="https://img.shields.io/badge/React-Frontend-61DAFB?style=for-the-badge&logo=react&logoColor=black" alt="React" />
  <img src="https://img.shields.io/badge/TypeScript-Language-3178C6?style=for-the-badge&logo=typescript&logoColor=white" alt="TypeScript" />
  <img src="https://img.shields.io/badge/Vite-Build%20Tool-646CFF?style=for-the-badge&logo=vite&logoColor=white" alt="Vite" />
  <img src="https://img.shields.io/badge/Tailwind%20CSS-UI-06B6D4?style=for-the-badge&logo=tailwindcss&logoColor=white" alt="Tailwind CSS" />
</p>

### Database and Infrastructure

<p>
  <img src="https://img.shields.io/badge/PostgreSQL-Database-4169E1?style=for-the-badge&logo=postgresql&logoColor=white" alt="PostgreSQL" />
  <img src="https://img.shields.io/badge/Redis-Cache-DC382D?style=for-the-badge&logo=redis&logoColor=white" alt="Redis" />
  <img src="https://img.shields.io/badge/Docker-Containers-2496ED?style=for-the-badge&logo=docker&logoColor=white" alt="Docker" />
  <img src="https://img.shields.io/badge/Linux-Server-FCC624?style=for-the-badge&logo=linux&logoColor=black" alt="Linux" />
  <img src="https://img.shields.io/badge/Nginx-Reverse%20Proxy-009639?style=for-the-badge&logo=nginx&logoColor=white" alt="Nginx" />
  <img src="https://img.shields.io/badge/Cloudflare-DNS%20%26%20Security-F38020?style=for-the-badge&logo=cloudflare&logoColor=white" alt="Cloudflare" />
</p>

### Security and Integrations

<p>
  <img src="https://img.shields.io/badge/Auth-JWT-000000?style=for-the-badge&logo=jsonwebtokens&logoColor=white" alt="JWT authentication" />
  <img src="https://img.shields.io/badge/Authorization-RBAC-5C2D91?style=for-the-badge" alt="Role-based access control" />
  <img src="https://img.shields.io/badge/Payments-Stripe-635BFF?style=for-the-badge&logo=stripe&logoColor=white" alt="Stripe" />
  <img src="https://img.shields.io/badge/TLS-Secure-00A98F?style=for-the-badge&logo=letsencrypt&logoColor=white" alt="TLS" />
</p>

## Architecture

```text
Browser
   |
   v
React + TypeScript
   | REST API / JWT
   v
Spring Boot API
   |
   +--> PostgreSQL
   +--> Redis
   +--> Stripe
```

The production environment is containerized with Docker and served through Nginx and Cloudflare on a Linux VPS.

## How to Run the Project

### Requirements

- Java 21
- Node.js 20 or later
- npm
- PostgreSQL
- Docker and Docker Compose, recommended

### Installation

```bash
git clone https://github.com/brielmarca/fourfight-gym-system.git
cd fourfight-gym-system
```

Configure the local environment variables using the examples provided in the repository. Never commit production credentials, API keys or real customer data.

Install the dependencies and start the development environment:

```bash
npm install
npm run dev:full
```

## Deployment

The application is deployed in a production environment using Docker containers, Linux, Nginx and Cloudflare. Database migrations, authentication flows and role permissions should be validated before every production release.

## Project Status

Active production project. Features and infrastructure continue to evolve according to the academy's operational needs.

## License

This project is under the MIT License. See the `LICENSE` file for more details.

## Contact

Developed and maintained by **Gabriel Marca**.

[Portfolio](https://brielmarca-portfolio.pages.dev) | [LinkedIn](https://www.linkedin.com/in/gabrielmarca/) | [GitHub](https://github.com/brielmarca) | [Email](mailto:brielmarcacontact@gmail.com)
