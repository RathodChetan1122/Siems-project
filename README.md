# SIEMS — Smart Import-Export Management System

A full-stack enterprise-grade web application for managing import/export operations including supplier and customer management, shipment tracking with a 7-state state machine, inventory control with real-time low-stock alerts, and an analytics dashboard.

---

## Technology Stack

### Backend
- Java 17 + Spring Boot 3.3
- Spring Security + JWT (access + refresh token rotation)
- PostgreSQL 16 + Spring Data JPA / Hibernate
- Flyway database migrations
- MapStruct, Lombok, SpringDoc OpenAPI

### Frontend
- React 18 + React Router v6
- Tailwind CSS v3
- Axios (with automatic token refresh interceptor)
- Chart.js + react-chartjs-2
- react-hot-toast, lucide-react
