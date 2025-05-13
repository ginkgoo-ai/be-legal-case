# Ginkgoo Legal Case Service

## Features

### Completed ✅

* Legal Case Management (supports multiple profiles per user, flexible JSON structure)
* RESTful API: `/legalcases/**`
* Multiple profile types (e.g., P60, passport, referee, utility bill, etc.)

### In Progress 🚧

* Legal Case review and status workflow
* Business extension for profile association

## Tech Stack

* Java 21
* Spring Boot 3.x
* Spring Security
* PostgreSQL
* JWT

## Getting Started

```bash
git clone https://github.com/ginkgoo-ai/be-legal-case.git
cd be-legal-case
mvn clean install
mvn spring-boot:run
```

## Health Check

```bash
GET /health

# Response:
{
    "status": "UP"
}
```

## Configuration

```yaml
spring:
  security:
    oauth2:
      resourceserver:
        jwt:
          jwk-set-uri: ${AUTH_SERVER}/oauth2/jwks
          issuer-uri: ${AUTH_SERVER}

  datasource:
    url: ${POSTGRES_URL}
    username: ${POSTGRES_USER}
    password: ${POSTGRES_PASSWORD}
```

## Requirements

* JDK 21+
* PostgreSQL 14+
* Maven 3.8+

## License

This project is available under a **dual licensing model**:

- **Open Source License (AGPL-3.0)**  
  See the **[LICENSE](./LICENSE)** file for details.
- **Commercial License**  
  For proprietary/commercial use, contact **[license@ginkgoo.ai](mailto:license@ginkgoo.ai)**.

## Contributing

We welcome contributions!  
Please read our **[Contributing Guide](./CONTRIBUTING.md)** before submitting issues or pull requests.

## Code of Conduct

To foster a welcoming and inclusive environment, we adhere to our **[Code of Conduct](./CODE_OF_CONDUCT.md)**.

## Contributor License Agreement (CLA)

Before contributing, you must agree to our **[CLA](./CLA.md)**.

If you have any questions, contact **[license@ginkgoo.ai](mailto:license@ginkgoo.ai)**.

---

© 2025 Ginkgo Innovations. All rights reserved.