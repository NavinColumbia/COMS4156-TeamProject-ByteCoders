# COMS W4156 Advanced Software Engineering

### Group Project 2024

### TEAM MEMBERS

#### Navinashok Swaminathan - ns3886
#### Oleksandr Loyko - ol2260
#### Zach Cox - zsc2107
#### Orli Elisabeth Cohen - oec2109


##### Link:https://github.com/NavinColumbia/COMS4156-TeamProject-ByteCoders
##### branch: main
##### Video Demo:

https://www.dropbox.com/scl/fi/z5lcima7kjy1jpw5xwheb/Iteration-1-functionality-demo.mov?rlkey=ld4xxx19yk5ug3xetvunyk6yu&st=g5jnxxl7&dl=0
## Note About HyperLinks:
Links to videos/images in this ReadMe , are from dropbox. <br />
'www' urls are purely for view/download purposes. <br />
Replace 'www' with 'dl' in the event that an expected file format is required for any sort of processing<br />
Say for example to import json for postman directly using the URL : <br />
&emsp;Use https://dl.dropbox.com/scl/fi/f7o3rd45sq4zsiuqucnkp/ByteCoders.postman_collection.json?rlkey=gdcqv823snw3oawx6i80rbiyb&st=pjxn6cmh&dl=0 <br />
&emsp;Instead of https://www.dropbox.com/scl/fi/f7o3rd45sq4zsiuqucnkp/ByteCoders.postman_collection.json?rlkey=gdcqv823snw3oawx6i80rbiyb&st=pjxn6cmh&dl=0 <br />



## Note About Executing Commands:
Every command snippet assumes that before running the snippet,<br />
current working directory is the root directory of this project.


# Building and Running a Local Instance

### Install java( preferred: 17/21):
https://www.oracle.com/java/technologies/downloads/#java21

### Pre-Requisite

Ensure maven is installed <br />
https://maven.apache.org/install.html

Ensure postgres is installed
https://www.postgresql.org/download/

Use any db client of your preference <br />
Here is one for example : <br />
https://dbeaver.io/


### Git Clone Repo :
```
https://github.com/NavinColumbia/COMS4156-TeamProject-ByteCoders
```

### Set-Up:

Ensure to change username and password in application.properties according to database you wish to connect <br />
We have used the default postgres username <br />
Ensure that a databse with name 'pharmaid' is already created <br />
```
spring.datasource.url=jdbc:postgresql://localhost:5432/pharmaid
spring.datasource.username=postgres
spring.datasource.password=postgres
```

Install Lombok Plugin for Intellij :

https://plugins.jetbrains.com/plugin/6317-lombok


Cancel any applications running in port 8080.<br />
Refer: [killing pid](https://dev.to/osalumense/how-to-kill-a-process-occupying-a-port-on-windows-macos-and-linux-gj8#:~:text=To%20identify%20the%20process%20using,command%20in%20the%20Command%20Prompt.&text=Here%2C%201234%20is%20the%20PID%20of%20the%20process%20using%20port%205672%20.&text=To%20kill%20the%20process%2C%20use,with%20the%20PID%20obtained%20above.&text=Replace%201234%20with%20the%20actual%20PID)


### Build:
```
mvn clean verify
```

### Running The Application:
```
mvn spring-boot:run
```
Application must now be Running In: http://localhost:8080/






# Style Checking

Run checkstyle:
```
mvn checkstyle:check
```
Check console output for errors/warnings/violations.
![current checkstyle reports 0 violation](https://dl.dropbox.com/scl/fi/3xpqsf94i3vqgbj7rti9l/checkstyle_check.png?rlkey=1f60qzif8wviopr23xcl0352y&st=dc2m561y&dl=0)

# Static Code Analysis
PMD(mentioned in assignment) was the static analyzer used.
&nbsp;

&emsp;Running PMD:

```
    pmd check -d src/main/java -R rulesets/java/quickstart.xml -f text -r "pmdres.txt"
    pmd check -d src/main/java -R rulesets/java/quickstart.xml -f html -r "pmdres.html"
```
&emsp;&emsp;Find file named pmdres.html <br />
&emsp;&emsp;Open the html with any browser or check 'pmdres.txt'.<br />
&emsp;&emsp;An empty content in either of the file mean no further suggestions.
![0 pmd violations](https://dl.dropbox.com/scl/fi/e1kre98pc1p709go58lry/pmd_check.png?rlkey=70tjnwck84jgpzntnnr6ooxsp&st=086skwc9&dl=0)

# Running Tests and Jacoco Report
As part of `mvn clean verify` , tests under ./src/test/java must have been executed.

&nbsp;

### Test Reports
From the previously run `mvn clean verify`, find '.txt' files under './target/surefire-reports/' to view test results <br/>
Or you can run mvn clean test and view console output <br />
![All tests ran successfully](https://dl.dropbox.com/scl/fi/e86n0krsbt47rapbirvre/tests_pass.png?rlkey=br2zkd43adfuq2910nvrvbd92&st=4rrf5jf5&dl=0)
&nbsp;

### Jacoco Report
From the previously run `mvn clean verify` <br />
&emsp;&emsp;Find file 'index.html' under '/target/site/jacoco/' and<br />
Open with any browser to view jacoco results
![jacoco report shows >90% coverage](https://dl.dropbox.com/scl/fi/2pghifroxe70n9w81wgho/jacoco_check.png?rlkey=awerv5sflnh66vopyxq3feysk&st=wo21q0yf&dl=0)


# Testing EndPoint With Postman
&emsp; 1.Install Postman  (https://www.postman.com/)
&nbsp;

&emsp; 2.Download the following JSON:    
&emsp;  &emsp; [click here to download](https://www.dropbox.com/scl/fi/f7o3rd45sq4zsiuqucnkp/ByteCoders.postman_collection.json?rlkey=gdcqv823snw3oawx6i80rbiyb&st=pjxn6cmh&dl=0)        
&emsp;  &emsp; or visit <br/>
&emsp;&emsp; https://www.dropbox.com/scl/fi/f7o3rd45sq4zsiuqucnkp/ByteCoders.postman_collection.json?rlkey=gdcqv823snw3oawx6i80rbiyb&st=pjxn6cmh&dl=0
&nbsp;

&emsp; 3.Go to 'my workspace' in Postman, click 'import', and select the downloaded json.<br/>
&nbsp;

# Endpoints

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
    - 500 Internal Server Error: `"Unexpected error encountered during getting a list of medications"`


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

