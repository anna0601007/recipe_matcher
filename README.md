# RecipeMatcher

RecipeMatcher is a full-stack web application for saving recipes and finding recipe recommendations based on ingredients the user already has.

Users can create, edit, delete and filter recipes. The recommendation page shows matching recipes together with matched and missing ingredients.

## Technologies

- Java 21
- Spring Boot
- PostgreSQL
- Angular
- TypeScript
- JUnit 5


## Features

- Recipe CRUD
- Recipe filtering by category, difficulty and cooking time
- Recipe recommendations from available ingredients
- Backend validation
- Backend unit tests
- Code quality analysis with SonarQube



## How to Run

### Backend

From the project root:

```powershell
.\mvnw.cmd spring-boot:run
```

Backend will run on:
```aiignore
http://localhost:8080
```

Example backend endpoint:
```
http://localhost:8080/api/recipes
```
### Frontend

Open terminal in the frontend folder:
```powershell
cd frontend
npm install
ng serve -o
```

Frontend will run on:
```
http://localhost:4200
```
### Tests

Run backend unit tests from the project root:
```powershell
.\mvnw.cmd test
```

Run tests with coverage:
```powershell
.\mvnw.cmd clean verify
```