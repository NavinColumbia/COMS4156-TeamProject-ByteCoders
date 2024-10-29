# COMS W4156 Advanced Software Engineering

## ByteCoders Group Project: PharmaId Service

### Team Members:

- Navinashok Swaminathan - ns3886
- Oleksandr Loyko - ol2260
- Zach Cox - zsc2107
- Orli Elisabeth Cohen - oec2109

### Important Assets:

- [GitHub repo](https://github.com/NavinColumbia/COMS4156-TeamProject-ByteCoders)
- [First Iteration Video Demo](https://www.dropbox.com/scl/fi/z5lcima7kjy1jpw5xwheb/Iteration-1-functionality-demo.mov?rlkey=ld4xxx19yk5ug3xetvunyk6yu&st=g5jnxxl7&dl=0)
- [First Iteration Postman Collection Logs](https://www.dropbox.com/scl/fi/f7o3rd45sq4zsiuqucnkp/ByteCoders.postman_collection.json?rlkey=gdcqv823snw3oawx6i80rbiyb&st=pjxn6cmh&dl=0)
- [JIRA Board](https://bytecoders-4156.atlassian.net/jira/software/projects/BYT/boards/1)

### Building and Running a Local Instance, all instructions correspond to a Mac M1+ machine

Ensure the following assets are installed on your local machine (or
use [Homebrew](https://brew.sh/)):

- [Java 17](https://www.oracle.com/java/technologies/javase/jdk17-archive-downloads.html)
- [Maven](https://maven.apache.org/install.html)
- If using IntelliJ, install [Lombok plugin](https://plugins.jetbrains.com/plugin/6317-lombok)
- [Postgres](https://www.postgresql.org/download/)
- Install any db client of your preference (eg [dbeaver](https://dbeaver.io/))
- Run `git clone https://github.com/NavinColumbia/COMS4156-TeamProject-ByteCoders` to pull down
  the main branch
- Set appropriate environment variables
  in [application.properties](https://github.com/NavinColumbia/COMS4156-TeamProject-ByteCoders/blob/main/src/main/resources/application.properties).
  Reach out to the team to gain the specific credentials for the db user, password, and service
  account credentials for GCP.
- Cancel any applications running in port 8080
  via [killing pid](https://dev.to/osalumense/how-to-kill-a-process-occupying-a-port-on-windows-macos-and-linux-gj8).

### Using the ByteCoders Service Account to Run the Service

- Request a `GOOGLE_APPLICATION_CREDENTIALS` key from the team in order to authenticate the
  service account
- Store the key as an environment variable in `~/.zshrc` as `export 
  GOOGLE_APPLICATION_CREDENTIALS="./path-to-key.json"`
- Create a new gcloud config based on the service account and GCP project
    - Create new config: `gcloud config configurations create [CONFIG_NAME]`
    - Active config: `gcloud config configurations activate [CONFIG_NAME]`
    - Authenticate service account:
      `gcloud auth activate-service-account --key-file=${GOOGLE_APPLICATION_CREDENTIALS}`
    - Set default project for config: `gcloud config set project bytecoders-coms4156`
- After authenticating the service account, ensure all tests pass and there are no PMD/checkstyle
  violations with `mvn clean verify`
- Run the application with `mvn spring-boot:run`. Application should now be running in at
  http://localhost:8080/

### Style Checking

Check console output for errors/warnings/violations.
![current checkstyle reports 0 violation](https://dl.dropbox.com/scl/fi/3xpqsf94i3vqgbj7rti9l/checkstyle_check.png?rlkey=1f60qzif8wviopr23xcl0352y&st=dc2m561y&dl=0)

### Running Tests and Jacoco Report

As part of `mvn clean verify`, tests under `./src/test/java` should execute.

### Test Reports

From the previously run `mvn clean verify`, find `.txt` files under `./target/surefire-reports/` to
view test results or run `mvn clean test` and view console output.

![All tests ran successfully](https://dl.dropbox.com/scl/fi/e86n0krsbt47rapbirvre/tests_pass.png?rlkey=br2zkd43adfuq2910nvrvbd92&st=4rrf5jf5&dl=0)

### Jacoco Report

After running `mvn clean verify`, view test coverage at `/target/site/jacoco/index.html` and
open with any browser to view jacoco results

![jacoco report shows >90% coverage](https://dl.dropbox.com/scl/fi/2pghifroxe70n9w81wgho/jacoco_check.png?rlkey=awerv5sflnh66vopyxq3feysk&st=wo21q0yf&dl=0)

# API Endpoint Documentation

### Testing Endpoints with Postman

1. Install Postman  (https://www.postman.com/)
2. Download the
   following [JSON file](https://www.dropbox.com/scl/fi/f7o3rd45sq4zsiuqucnkp/ByteCoders.postman_collection.json?rlkey=gdcqv823snw3oawx6i80rbiyb&e)
3. Go to `My Workspace` in Postman, click `Import`, and import the downloaded json file.

### 1. GET /hello

- **Description**: Basic hello endpoint for testing.
- **Response**:
    - 200 OK: Returns "Hello :)"

### 2. POST /register

- **Description**: Register a new user.
- **Request Body**: RegisterUserRequest (JSON)
  ```
  {
    "email": "...",
    "password": "..."
  }
  ```
- **Responses**:
    - 201 Created: Returns the created User object
      ```
      {
        "id": "...",
        "email": "..."
      }
      ```
    - 400 Bad Request:
        - If email is missing: `{"errors": ["The email address is required"]}`
        - If email is invalid: `{"errors": ["The email address is invalid"]}`
        - If password is missing: `{"errors": ["The password is required"]}`
        - If user already exists: `"User already exists for this email"`
    - 500 Internal Server Error: `"Something went wrong"`

### 3. POST /login

- **Description**: Login a user.
- **Request Body**: LoginUserRequest (JSON)
  ```
  {
    "email": "...",
    "password": "..."
  }
  ```
- **Responses**:
    - 200 OK: Returns the User object as JSON
      ```
      {
        "id": "...",
        "email": "..."
      }
      ```
    - 400 Bad Request:
        - If email is missing: `{"errors": ["The email address is required"]}`
        - If email is invalid: `{"errors": ["The email address is invalid"]}`
        - If password is missing: `{"errors": ["The password is required"]}`
    - 401 Unauthorized: `"Forbidden"`
    - 500 Internal Server Error: `"Unexpected error encountered during login"`

### 4. GET /medications

- **Description**: Get all medications.
- **Responses**:
    - 200 OK: Returns a list of Medication objects
      ```
      [
        {
          "id": "...",
          "name": "...",
          "description": "..."
        },
        {
          "id": "...",
          "name": "...",
          "description": "..."
        }
      ]
      ```
    - 500 Internal Server Error:
      `"Unexpected error encountered during getting a list of medications"`

### 5. POST /users/{userId}/prescriptions

- **Description**: Add a prescription for a user.
- **Path Variable**: userId
- **Request Body**: CreatePrescriptionRequest (JSON)
  ```
  {
    "medicationId": "...",
    "dosage": ...,
    "numOfDoses": ...,
    "startDate": "...",
    "endDate": "...",
    "isActive": ...
  }
  ```
- **Responses**:
    - 201 Created: Returns the created Prescription object
      ```
      {
        "id": "...",
        "userId": "...",
        "medicationId": "...",
        "dosage": ...,
        "numOfDoses": ...,
        "startDate": "...",
        "endDate": "...",
        "isActive": ...
      }
      ```
    - 400 Bad Request:
        - If medicationId is missing: `{"errors": ["Medication id is required"]}`
        - If dosage is not positive: `{"errors": ["Dosage must be positive"]}`
        - If numOfDoses is not positive: `{"errors": ["Number of doses must be positive"]}`
        - If startDate is missing: `{"errors": ["Start date is required"]}`
        - If isActive is missing: `{"errors": ["Active flag is required"]}`
    - 404 Not Found:
        - If user doesn't exist: `"Provided User doesn't exist"`
        - If medication doesn't exist: `"Medication doesn't exist"`
    - 500 Internal Server Error: `"Unexpected error encountered while creating a prescription"`

### 6. GET /users/{userId}/prescriptions

- **Description**: Get prescriptions for a user.
- **Path Variable**: userId
- **Responses**:
    - 200 OK: Returns a list of Prescription objects
      ```
      [
        {
          "id": "..",
          "userId": "...",
          "medicationId": "...",
          "dosage": ...,
          "numOfDoses": ...,
          "startDate": "...",
          "endDate": "...",
          "isActive": ...
        }
      ]
      ```
    - 404 Not Found: `"Provided User doesn't exist"`
    - 500 Internal Server Error: `"Unexpected error encountered while getting user prescriptions"`

# Appendix

### Connecting Local Service to Cloud SQL

The following steps assume you have completed the service account authentication process and
have the proper credentials to run commands via the service account. These steps should only be
completed upon the instantiation of the db. If the medications table is already populated,
these steps should not be run.

Steps to connect and populate the db with medications data:

- Connect to the db instance: `gcloud beta sql connect pharmaid-db-instance --user=$
{PHARMAID_DB_USER}` where `PHARMAID_DB_USER` should be stored as a local variable
- Enter the db password
- Connect to the prod db, `pharmaid-prod` with the command: `\c pharmaid-prod`
- Populate the `medications` table with seed medications with the command:
  `\i 'PATH-TO-REPO/COMS4156-TeamProject-ByteCoders/src/main/java/medications_seed.sql'` (ensure
  the proper path is input to the repo via `PATH-TO-REPO`)
- Test to ensure the medications are populated `SELECT * FROM medications;`

### Other helpful gcloud CLI commands

- View all configurations to ensure proper activation: `gcloud config configurations list`
- View a Cloud SQL instance name: `gcloud sql instances describe [DB_INSTANCE] --format="value
(connectionName)"` 

### JWT
- On Login, you receive JWT Token, use this to act on behalf of that user
- Set Authorization Header to be 'Bearer...' to act on behalf of the user on subsequent requests
