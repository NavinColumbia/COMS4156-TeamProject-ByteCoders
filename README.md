
    Advanced Software Engineering Group Project

    Team : ByteCoders

    Team Members:
    
        Navinashok Swaminathan - ns3886
        
        Oleksandr Loyko - ol2260
        
        Zach Cox - zsc2107
    
        Orli Elisabeth Cohen - oec2109


# JWT Exploration (Authentication/Aurthorization)


## Demo 
 https://www.dropbox.com/scl/fi/4x4bx1qvsalskxtodyvxz/jwt_demo.mov?rlkey=5zo3krm3whysn5ythtfkysye7&st=j6hfs90x&dl=0

    
    
    
    
    Step 1: Register User A
    Request
    
    Method: POST
    
    URL: http://localhost:8080/api/auth/register
    
    Headers:
    
    Content-Type: application/json
    Body:
    
    json
    Copy code
    {
    "email": "usera@example.com",
    "password": "Password123!",
    }
    
    In Postman
    
    Create a new request.
    Set the method to POST.
    Enter the URL: http://localhost:8080/api/auth/register.
    
    Go to the Headers tab:
    Add Content-Type with value application/json.
    
    Go to the Body tab:
    Select raw and choose JSON from the dropdown.
    
    Paste the JSON body above.
    
    Click Send.
    Expected Response
    
    Status Code: 200 OK
    
    Body:
    
    json
    Copy code
    "User registered successfully!"
    
    
    
    
    
    
    Step 2: Register User B
    Repeat the same steps as above, but with the following body:
    
    json
    Copy code
    {
    "email": "userb@example.com",
    "password": "Password123!",
    "userType": "USER"
    }
    
    
    
    
    
    
    
    Step 3: Authenticate User A
    Request
    
    Method: POST
    
    URL: http://localhost:8080/api/auth/login
    
    Headers:
    
    Content-Type: application/json
    Body:
    
    json
    Copy code
    {
    "email": "usera@example.com",
    "password": "Password123!"
    }
    In Postman
    
    Create a new request.
    Set the method to POST.
    Enter the URL: http://localhost:8080/api/auth/login.
    Set headers and body as in previous steps.
    Click Send.
    Expected Response
    
    Status Code: 200 OK
    
    Body:
    
    json
    Copy code
    {
    "accessToken": "<UserA_Token>",..
    }
    Action
    
    Copy <UserA_Token> for future use.
    
    
    
    
    
    
    Step 4: Authenticate User B
    Repeat the authentication steps for User B, using userb@example.com and Password123!.
    
    Action
    
    Copy <UserB_Token> for future use.
    
    
    
    
    
    
    
    Step 5: Retrieve User IDs
    Your API endpoints require user_id in the URL. To get the user_id, you can decode the JWT token.
    
    Method to Decode JWT Token
    
    Go to jwt.io.
    
    Paste <UserA_Token> into the Encoded field.
    
    Look at the Decoded payload; you should see something like:
    
    json
    Copy code
    {
    "sub": "<UserA_ID>",
    "email": "usera@example.com",
    "iat": ...,
    "exp": ...
    }
    Copy <UserA_ID>.
    
    Repeat for <UserB_Token> to get <UserB_ID>.
    
    
    
    
    
    
    Step 6: User A Adds a Prescription
    Request
    
    Method: POST
    
    URL: http://localhost:8080/api/users/<UserA_ID>/records/prescriptions
    
    Headers:
    
    Content-Type: application/json
    Authorization: Bearer <UserA_Token>
    Body:
    
    json
    Copy code
    {
    "medicationName": "Amoxicillin",
    "dosage": "500mg",
    "frequency": "3 times a day",
    "startDate": "2023-10-20",
    "endDate": "2023-10-30"
    }
    In Postman
    
    Create a new request.
    Set the method to POST.
    Enter the URL, replacing <UserA_ID> with the actual user ID.
    Go to the Headers tab:
    Add Content-Type: application/json.
    Add Authorization: Bearer <UserA_Token>.
    Go to the Body tab:
    Select raw and choose JSON.
    Paste the JSON body.
    Click Send.
    Expected Response
    
    Status Code: 200 OK
    
    Body:
    
    json
    Copy code
    {
    "id": "<Prescription_ID>",
    "medicationName": "Amoxicillin",
    "dosage": "500mg",
    "frequency": "3 times a day",
    "startDate": "2023-10-20",
    "endDate": "2023-10-30",
    "user": {
    "id": "<UserA_ID>",
    "email": "usera@example.com"
    }
    }
    Action
    
    Copy <Prescription_ID> for future use.
    
    
    
    
    
    
    
    
    
    Step 7: User B Attempts to Access User A's Prescriptions
    Request
    
    Method: GET
    URL: http://localhost:8080/api/users/<UserA_ID>/records
    Headers:
    Authorization: Bearer <UserB_Token>
    In Postman
    
    Create a new request.
    Set the method to GET.
    Enter the URL, replacing <UserA_ID>.
    Go to the Headers tab:
    Add Authorization: Bearer <UserB_Token>.
    Click Send.
    Expected Response
    
    Status Code: 403 Forbidden
    
    Body:
    
    json
    Copy code
    {
    "timestamp": "...",
    "status": 403,
    "error": "Forbidden",
    "message": "You are not authorized to access these records.",
    "path": "/api/users/<UserA_ID>/records"
    }
    
    
    
    
    
    
    
    
    Step 8: User B Requests Access to User A's Prescriptions
    Request
    
    Method: POST
    
    URL: http://localhost:8080/api/users/<UserA_ID>/records/request
    
    Headers:
    
    Content-Type: application/json
    Authorization: Bearer <UserB_Token>
    Body:
    
    json
    Copy code
    {
    "permissionType": "VIEW"
    }
    In Postman
    
    Create a new request.
    Set the method to POST.
    Enter the URL, replacing <UserA_ID>.
    Set headers and body as before.
    Click Send.
    Expected Response
    
    Status Code: 200 OK
    
    Body:
    
    json
    Copy code
    "<Request_ID>"
    Action
    
    Copy <Request_ID> for future use.
    
    
    
    
    
    
    
    
    
    
    
    
    
    
    Step 9: User A Accepts the Request from User B
    Request
    
    Method: POST
    URL: http://localhost:8080/api/users/<UserA_ID>/records/<Request_ID>/accept
    Headers:
    Authorization: Bearer <UserA_Token>
    In Postman
    
    Create a new request.
    Set the method to POST.
    Enter the URL, replacing <UserA_ID> and <Request_ID>.
    Go to the Headers tab:
    Add Authorization: Bearer <UserA_Token>.
    Click Send.
    Expected Response
    
    Status Code: 200 OK
    Body: Empty or confirmation message.
    
    
    
    
    
    
    
    Step 10: User B Accesses User A's Prescriptions Again
    Repeat Step 7, but now it should succeed.
    
    Expected Response
    
    Status Code: 200 OK
    
    Body:
    
    json
    Copy code
    [
    {
    "id": "<Prescription_ID>",
    "medicationName": "Amoxicillin",
    "dosage": "500mg",
    "frequency": "3 times a day",
    "startDate": "2023-10-20",
    "endDate": "2023-10-30",
    "user": {
    "id": "<UserA_ID>",
    "email": "usera@example.com"
    }
    }
    ]
    
    
    
    
    
    
    Step 11: User B Attempts to Add a Prescription to User A's Account
    Request
    
    Method: POST
    
    URL: http://localhost:8080/api/users/<UserA_ID>/records/prescriptions
    
    Headers:
    
    Content-Type: application/json
    Authorization: Bearer <UserB_Token>
    Body:
    
    json
    Copy code
    {
    "medicationName": "Ibuprofen",
    "dosage": "200mg",
    "frequency": "As needed",
    "startDate": "2023-11-01",
    "endDate": "2023-11-10"
    }
    In Postman
    
    Set up the request as before, but using <UserB_Token>.
    Expected Response
    
    Status Code: 403 Forbidden
    
    Body:
    
    json
    Copy code
    {
    "timestamp": "...",
    "status": 403,
    "error": "Forbidden",
    "message": "You are not authorized to add prescriptions for this user.",
    "path": "/api/users/<UserA_ID>/records/prescriptions"
    }
    
    
    
    
    
    Step 12: User B Requests Edit Permission from User A
    Request
    
    Method: POST
    
    URL: http://localhost:8080/api/users/<UserA_ID>/records/request
    
    Headers:
    
    Content-Type: application/json
    Authorization: Bearer <UserB_Token>
    Body:
    
    json
    Copy code
    {
    "permissionType": "EDIT"
    }
    In Postman
    
    Set up the request as in Step 8, but change permissionType to "EDIT".
    Expected Response
    
    Status Code: 200 OK
    
    Body:
    
    json
    Copy code
    "<Edit_Request_ID>"
    Action
    
    Copy <Edit_Request_ID> for future use.
    
    
    
    
    
    
    Step 13: User A Accepts the Edit Request
    Repeat Step 9, but use <Edit_Request_ID>.
    
    
    
    
    
    
    
    Step 14: User B Adds a Prescription to User A's Account
    Repeat Step 11, but now it should succeed.
    
    Expected Response
    
    Status Code: 200 OK
    
    Body:
    
    json
    Copy code
    {
    "id": "<Prescription_ID_2>",
    "medicationName": "Ibuprofen",
    "dosage": "200mg",
    "frequency": "As needed",
    "startDate": "2023-11-01",
    "endDate": "2023-11-10",
    "user": {
    "id": "<UserA_ID>",
    "email": "usera@example.com"
    }
    }
    Action
    
    Copy <Prescription_ID_2> if needed.
    
    
    
    
    
    
    Step 15: User A Revokes User B's Access
    Request
    
    Method: POST
    URL: http://localhost:8080/api/users/<UserA_ID>/records/<Edit_Request_ID>/revoke
    Headers:
    Authorization: Bearer <UserA_Token>
    In Postman
    
    Set up the request as in Step 9, but replace accept with revoke in the URL.
    Expected Response
    
    Status Code: 200 OK
    Body: Empty or confirmation message.
    Step 16: User B Attempts to Access User A's Prescriptions Again
    Repeat Step 7.
    
    Expected Response
    
    Status Code: 403 Forbidden
    
    Body:
    
    json
    Copy code
    {
    "timestamp": "...",
    "status": 403,
    "error": "Forbidden",
    "message": "You are not authorized to access these records.",
    "path": "/api/users/<UserA_ID>/records"
    }
    Additional Notes
    
    Setting Headers in Postman:
    
    Content-Type: Always set to application/json when sending JSON bodies.
    Authorization: Add a header with key Authorization and value Bearer <Your_Token>.
    Using Variables in Postman:
    
    You can use Postman's environment variables to store tokens and IDs.
    
    To set a variable:
    
    After receiving a response, go to the Tests tab.
    
    Add a script like:
    
    javascript
    Copy code
    var jsonData = JSON.parse(responseBody);
    postman.setEnvironmentVariable("UserA_Token", jsonData.accessToken);
    To use a variable in a request:
    
    Use {{UserA_Token}} in the header value.
    Decoding JWT Tokens:
    
    JWT tokens are Base64-encoded; you can decode them to get the payload.
    The sub claim typically contains the user_id.



