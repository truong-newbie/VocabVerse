# ARCHITECTURE_LOCK.md

# Architecture

Current Architecture:

Modular Monolith

---

# Backend

Spring Boot

Modules:

* auth
* user
* collection
* vocabulary
* learning
* review
* notification
* shadowing
* roleplay
* ai
* export

---

# Database

PostgreSQL

Main database.

---

# Cache

Redis

Used for:

* Dictionary cache
* Rate limiting
* Dashboard cache

---

# Async Processing

RabbitMQ

Used for:

* AI normalize
* PDF export
* Email
* Shadowing processing

---

# AI Providers

Primary:

Groq

Fallback:

Gemini

---

# Forbidden Architecture Changes

Do NOT introduce:

* Microservices
* MongoDB
* GraphQL
* Kafka
* Elasticsearch
* Next.js Backend

without explicit approval.

---

# API Style

REST API

Base URL:

/api/v1

---

# Authentication

JWT

Access Token

Refresh Token

---

# Database Rules

Use Flyway migrations.

Never modify schema manually.

Never generate tables automatically in production.

---

# Storage

Cloudflare R2

Used for:

* Videos
* PDFs
* Images
* Audio
