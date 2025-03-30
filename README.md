# Document Analyzer

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

### Improvements points
1. **Externalize Strings**: Consider moving fixed strings (like error messages) to properties files or similar.
2. **Javadocs**: Add formal Java documentation to describe the purpose of classes and methods.
3. **More unit tests**: The tests created cover basic scenarios; expanding to edge cases (e.g. null string, S3 failure) increases robustness.
4. **Integration tests**: Tests that verify the actual integration between `DocumentService`, S3, and the database, possibly using LocalStack and Testcontainers.
5. **Anonymized IDs**: Replace sensitive identifiers (like `userEmail`) with generated IDs (e.g. UUIDs) to protect personal data.