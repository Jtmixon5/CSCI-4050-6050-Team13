# Cinema E-Booking System

A web-based cinema booking application built with:

- **Frontend:** React + TypeScript + Vite
- **Backend:** Java 21 + Spring Boot
- **Database:** MySQL 8.4
- **Database management:** Docker Compose
- **Build tool:** Maven Wrapper


## Prerequisites

Install the following before running the project:

- Java JDK 21
- Node.js LTS
- Docker Desktop

## 1. Create the Environment File

Copy the example environment file:

### Windows PowerShell

```powershell
Copy-Item .env.example .env
```

### macOS or Linux

```bash
cp .env.example .env
```

Open `.env` and confirm that it contains values similar to:

```env
MYSQL_DATABASE=cinema_ebooking
MYSQL_USER=cinema_user
MYSQL_PASSWORD=cinema_password
MYSQL_ROOT_PASSWORD=cinema_root_password
```

The `.env` file should not be committed to GitHub.

## 2. Start MySQL

Make sure Docker Desktop is running.

From the repository root, run:

```powershell
docker compose up -d mysql
```

Check the container status:

```powershell
docker compose ps
```

The MySQL service should show as running or healthy.

To view its logs:

```powershell
docker compose logs -f mysql
```

Press `Ctrl+C` to stop viewing logs. This does not stop the database.

## 3. Run the Spring Boot Backend

Open a new terminal and move into the backend folder:

```powershell
cd backend
```

Run the backend:

### Windows PowerShell

```powershell
.\mvnw.cmd clean spring-boot:run
```

### macOS or Linux

```bash
./mvnw clean spring-boot:run
```

The backend should start on:

```text
http://localhost:8080
```

Test the movie endpoint in a browser or Postman:

```text
http://localhost:8080/api/movies
```

An empty database should return:

```json
[]
```

Keep this terminal open while using the application.

## 4. Run the React Frontend

Open another terminal from the repository root:

```powershell
cd frontend
npm install
npm run dev
```

The frontend should start on:

```text
http://localhost:5173
```

Open that address in a browser.

The frontend development server proxies requests beginning with `/api` to the Spring Boot backend at port `8080`.

## 6. Add a Test Movie

Send a `POST` request using Postman:

```text
POST http://localhost:8080/api/movies
Content-Type: application/json
```

Request body:

```json
{
  "title": "The Matrix",
  "category": "Science Fiction",
  "synopsis": "A computer hacker discovers the nature of reality.",
  "status": "CURRENTLY_PLAYING"
}
```

Then reload:

```text
http://localhost:5173
```

The new movie should appear on the page.

## 7. Stop the Project

Stop the backend and frontend by pressing:

```text
Ctrl+C
```

in each terminal.

Stop the MySQL container from the repository root:

```powershell
docker compose down
```

This preserves the MySQL data because the database uses a Docker volume.

To remove the database volume and permanently delete local database data:

```powershell
docker compose down -v
```

Only use `-v` when you intentionally want to reset the database.

## Starting the Project Again

For later development sessions:

1. Start Docker Desktop.
2. From the repository root, start MySQL:

```powershell
docker compose up -d mysql
```

3. Start the backend:

```powershell
cd backend
.\mvnw.cmd spring-boot:run
```

4. Start the frontend in another terminal:

```powershell
cd frontend
npm run dev
```
