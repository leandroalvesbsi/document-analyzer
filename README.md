# Document Analyzer

## General info
1. The database is H2.
2. Using S3 localstack for storage.

## Setup
- Configure the `.env` file with your environment variables.

## Run
- Build the project:
  ```bash
  mvn clean package
  ```

  ```bash
  docker-compose up -d --build
  ```

---

### Useful API calls


@userEmail = test@test.com
@teamId = 1
@newTeamId = 2
@documentId = 1

### create team
POST http://localhost:8080/api/team
Content-Type: application/json

{
"teamName": "My Team"
}

### create user
POST http://localhost:8080/api/user-team/team/{{teamId}}?startDate=2025-04-02 00:00:00
Content-Type: application/json

{
"email": "{{userEmail}}",
"registrationDate": "2025-04-01T00:00:00"
}

### add document
POST http://localhost:8080/api/document/{{userEmail}}
Content-Type: application/json

{
"name": "my document",
"text": "Smart AI learns fast from vast data every day. AI grows stronger with complex tasks. Engineers train AI using huge datasets often. Smart systems evolve quickly beyond simple rules."
}

### get word frequency
GET http://localhost:8080/api/document/{{documentId}}/word-frequency

### get synonyms
GET http://localhost:8080/api/document/{{documentId}}/synonym?numberOfSynonyms=2

### remove user from team
POST http://localhost:8080/api/user-team/{{userEmail}}/team/{{teamId}}

### user team history
GET http://localhost:8080/api/user-team/{{userEmail}}/history?startDate=2025-04-02 00:00:00
Content-Type: application/json

### change user team
POST http://localhost:8080/api/user-team/{{userEmail}}/from-team/{{teamId}}/to-team/{{newTeamId}}?startDate=2025-04-02 00:00:00
Content-Type: application/x-www-form-urlencoded

### remove user from team
POST http://localhost:8080/api/user-team/{{userEmail}}/team/{{newTeamId}}

### assingn existing user to another team
POST http://localhost:8080/api/user-team/user/{{userEmail}}/team/{{newTeamId}}?startDate=2025-04-02 00:00:00
Content-Type: application/x-www-form-urlencoded

### count user with no upload
GET http://localhost:8080/api/user/count-lazy-users?startDate=2023-06-01 00:00:00&endDate=2023-07-01 00:00:00

---

### Improvements points
1. **Externalize Strings**: Consider moving fixed strings (like error messages) to properties files or similar.
2. **Javadocs**: Add formal Java documentation to describe the purpose of classes and methods.
3. **More unit tests**: The tests created cover basic scenarios; expanding to edge cases (e.g. null string, S3 failure) increases robustness.
4. **Integration tests**: Tests that verify the actual integration between `DocumentService`, S3, and the database, possibly using LocalStack and Testcontainers.
5. **Anonymized IDs**: Replace sensitive identifiers (like `userEmail`) with generated IDs (e.g. UUIDs) to protect personal data.