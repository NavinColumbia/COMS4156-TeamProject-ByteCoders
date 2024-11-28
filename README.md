# COMS W4156 Advanced Software Engineering

## ByteCoders Group Project: PharmaId Service

### Team Members:

- Navinashok Swaminathan - ns3886
- Oleksandr Loyko - ol2260
- Zach Cox - zsc2107
- Orli Elisabeth Cohen - oec2109

## Repos
- **Client Backend** : https://github.com/zcox10/COMS4156-TeamProject-ClientBackend-ByteCoders
- **Client Frontend** : https://github.com/AlexLoyko/COMS4156-TeamProjectClient-ByteCoders/tree/main

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
- Run `git clone https://github.com/NavinColumbia/COMS4156-TeamProject-ByteCoders` to pull down
  the main branch
- Request a `GOOGLE_APPLICATION_CREDENTIALS` key from the team in order to authenticate the
  service account, and store the key as an environment variable in `~/.zshrc
```
mkdir -p ~/.config/gcloud
mv path-to-downloaded-key.json ~/.config/gcloud/pharmaid-service-account.json
sudo chown {{your_user_name}} ~/.zshrc
echo 'export GOOGLE_APPLICATION_CREDENTIALS="$HOME/.config/gcloud/pharmaid-service-account.json"' >> ~/.zshrc
source ~/.zshrc
```
- Configure Google Cloud CLI
```
gcloud config configurations create pharmaid-local

gcloud config configurations activate pharmaid-local

gcloud auth activate-service-account --key-file=$GOOGLE_APPLICATION_CREDENTIALS

gcloud config set project bytecoders-coms4156
```
- After authenticating the service account, ensure all tests pass and there are no PMD/checkstyle
  violations with `./mvnw clean verify`
- Cancel any applications running in port 8080
  via [killing pid](https://dev.to/osalumense/how-to-kill-a-process-occupying-a-port-on-windows-macos-and-linux-gj8).
- Run the app
```
./mvnw spring-boot:run -Dspring-boot.run.profiles=prod
```
- Application should now be running in at http://localhost:8080/


### Style Checking
Check console output for errors/warnings/violations or run 
```
./mvnw checkstyle:check
```
![current checkstyle reports 0 violation](https://dl.dropboxusercontent.com/scl/fi/zdo1bdzuokfo8tsupx6j0/Screenshot-2024-11-27-at-7.05.36-PM.png?rlkey=55njntawll28b1scrw5ig3q0g&st=vktboq0u&dl=0)
### Running Tests and Jacoco Report

As part of `./mvnw clean verify`, tests under `./src/test/java` should execute.

### Test Reports

From the previously run `./mvnw clean verify`, find `.txt` files under `./target/surefire-reports/` to
view test results or run `./mvnw clean test` and view console output.

![All tests ran successfully](https://dl.dropbox.com/scl/fi/p0mt8pspkhw8nu5r0b41u/Screenshot-2024-11-27-at-2.29.00-PM.png?rlkey=oo1aluaoxnwd3z10mmakeilds&st=x5r653qx&raw=1)

### Jacoco Report

After running `./mvnw clean verify`, view test coverage at `/target/site/jacoco/index.html` and
open with any browser to view jacoco results

![jacoco report shows >90% coverage](https://dl.dropbox.com/scl/fi/fk071xnw7vxbav1kutkie/Screenshot-2024-11-27-at-2.39.45-PM.png?rlkey=0ogcg5ro6um1rsuva5parxv7h&st=7mcjwix9&raw=1)

# API Endpoint Documentation

### Testing Endpoints with Postman

1. Install Postman  (https://www.postman.com/)
2. Download the
   following [JSON file](https://www.dropbox.com/scl/fi/zctqgeog5iame635xrxas/Pharmaid_tests_nov_27_postman_final.postman_collection.json?rlkey=oelk8nlqx8vs748i7i1111bcg&st=8gzwykv4&dl=0)
3. Go to `My Workspace` in Postman, click `Import`, and import the downloaded json file.
4. You can store any of the fields of response json as variables in `Scripts` for each request.
```
pm.collectionVariables.set("patientToken", jsonData.token);
```
5. You can modify any of the other variables using the 'Variables' tab in postman, that shows upon clicking on the collection's title.
6. When Creating Prescription, make sure to use an existing medicationId as in `resources/sql/medication_seed.sql`.

### View the API documentation

Navigate to [this link](https://pharmaid-prod-app-dot-bytecoders-coms4156.uk.r.appspot.com/pharmaid-api-docs-ui.html) to
view API docs in Swagger. The JSON output of the docs are available
at [this link](https://pharmaid-prod-app-dot-bytecoders-coms4156.uk.r.appspot.com/pharmaid-api-docs). Both are defined in
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