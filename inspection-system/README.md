# Inspection System

A system for managing processes, KPIs, and goals in a hierarchical inspection organization.

## Structure
- **backend/**: Spring Boot project for REST APIs and database interaction.
- **frontend/**: React project for the user interface.
- **database/**: PostgreSQL setup and SQL scripts.

## Setup
1. **Backend**:
   - Navigate to \`backend/\` and run \`mvn spring-boot:run\`.
   - Configure database in \`application.properties\`.
   - You may need to generate a new \`pom.xml\` using Spring Initializr as per the original script if the provided one is not sufficient.

2. **Frontend**:
   - Navigate to \`frontend/\` and run \`npm start\`.
   - You may need to initialize the project with \`npx create-react-app . --template typescript\` and install dependencies like \`npm install axios @mui/material @emotion/react @emotion/styled react-chartjs-2 chart.js\` as per the original script if the provided `package.json` is not sufficient.

3. **Database**:
   - Run \`docker-compose up\` in the \`database/\` directory to start PostgreSQL.

## Requirements
- JDK 17
- Node.js 16+
- PostgreSQL 13+
- Maven
- Docker (optional)

## Notes
- Update the database password in \`backend/src/main/resources/application.properties\` and \`database/docker-compose.yml\`.
- Ensure PostgreSQL is running before starting the backend.
