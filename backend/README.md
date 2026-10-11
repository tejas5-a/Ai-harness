# Altron Code - Background Worker System

This backend service provides persistent, autonomous AI coding task processing that continues running when mobile clients lock, disconnect, or close.

## Features

- **Queue & Worker System:** Processes multi-step coding jobs with configurable concurrency limits.
- **Persistent Storage:** Tasks, step-by-step logs, generated files, and statuses are persisted to disk.
- **App Termination Recovery:** Continues jobs independently of phone or browser lifecycle.
- **Authentication:** Protected REST endpoints using `Authorization: Bearer <ALTRON_AUTH_TOKEN>`.
- **Destructive Operations Safety:** State transitions to `WAITING_APPROVAL` before applying file overwrites or sensitive commands.
- **Cancellation & Retries:** Allows safe task cancellation without project corruption and safe retries with retry limits.

## Setup & Deployment

1. Install dependencies:
```bash
cd backend
npm install
```

2. Configure environment:
```bash
cp .env.example .env
# Edit .env with your desired PORT and AUTH_TOKEN
```

3. Run worker:
```bash
npm start
```

## REST API Endpoints

- `GET /health`: Health and worker status.
- `GET /api/tasks`: List all tasks (authenticated).
- `POST /api/tasks`: Submit new coding task (authenticated).
- `GET /api/tasks/:id`: Get task progress, logs, and changed files.
- `POST /api/tasks/:id/cancel`: Cancel running task.
- `POST /api/tasks/:id/retry`: Retry failed task.
- `POST /api/tasks/:id/approve`: Approve pending changes.
