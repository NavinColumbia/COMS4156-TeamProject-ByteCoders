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

### View the API documentation

Navigate to [this link](https://bytecoders-coms4156.uk.r.appspot.com/pharmaid-api-docs-ui.html) to
view API docs in Swagger. The JSON output of the docs are available
at [this link](https://bytecoders-coms4156.uk.r.appspot.com/pharmaid-api-docs). Both are defined in
`application.properties`.

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