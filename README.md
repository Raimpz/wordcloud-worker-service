# WordCloud Worker Service

Spring Boot background service that consumes text chunks from RabbitMQ, performs word frequency analysis, and writes results to PostgreSQL.

This service has no REST endpoints — it runs purely as a message queue consumer.

## Prerequisites

- Java 21 (Temurin recommended)
- Running PostgreSQL and RabbitMQ (see below)

## Local Development

### 1. Start infrastructure

```bash
cd ../deployment-config
docker compose -f docker-compose.infra.yml up
```

This starts PostgreSQL on port 5432 and RabbitMQ on port 5672.

### 2. Run the service

```bash
./gradlew bootRun
```

The worker starts and begins listening for messages on the `text-processing-queue`.

## Environment Variables

| Variable                    | Default                                        | Description              |
|-----------------------------|------------------------------------------------|--------------------------|
| `SPRING_DATASOURCE_URL`    | `jdbc:postgresql://localhost:5432/wordcloud`   | PostgreSQL connection URL|
| `SPRING_DATASOURCE_USERNAME`| `user`                                        | Database username        |
| `SPRING_DATASOURCE_PASSWORD`| `password`                                    | Database password        |
| `SPRING_RABBITMQ_HOST`     | `localhost`                                    | RabbitMQ host            |
| `SPRING_RABBITMQ_PORT`     | `5672`                                         | RabbitMQ port            |

### RabbitMQ Listener Configuration

These are configured in `application.yml` and generally don't need overriding:

| Setting                | Value  | Description                          |
|------------------------|--------|--------------------------------------|
| Concurrency            | 4      | Minimum concurrent consumers         |
| Max Concurrency        | 8      | Maximum concurrent consumers         |
| Prefetch               | 10     | Messages prefetched per consumer     |
| Retry Max Attempts     | 3      | Max retry attempts on failure        |
| Retry Initial Interval | 1000ms | First retry delay                    |
| Retry Multiplier       | 2.0    | Backoff multiplier between retries   |

## Docker

```bash
docker build -t wordcloud-worker .
docker run \
  -e SPRING_DATASOURCE_URL=jdbc:postgresql://host.docker.internal:5432/wordcloud \
  -e SPRING_RABBITMQ_HOST=host.docker.internal \
  wordcloud-worker
```

## Full Stack

To run the entire application with one command, see the [deployment-config README](../deployment-config/README.md or https://github.com/Raimpz/wordcloud-deployment-config/blob/main/README.md).
