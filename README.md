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
Click on the thumbnail below [![thumbnail_video_demo](thumbnail_image_link)](video_demo_link)
<br />
or visit video (video_demo_link)

## Note About HyperLinks:
Links to videos/images in this ReadMe , are from dropbox. <br />
'www' urls are purely for view/download purposes. <br />
Replace 'www' with 'dl' in the event that an expected file format is required for any sort of processing<br />
Say for example to import json for postman directly using the URL : <br />
&emsp;Use https://dl.dropbox.com/scl/fi/qeonw7222mvijuhdk3zyl/Postman_tests_ns3886.postman_collection.json?rlkey=0ji1msxuwpqjcy3iuxvis4q2f&st=6iclsah3&dl=0 <br />
&emsp;Instead of https://www.dropbox.com/scl/fi/qeonw7222mvijuhdk3zyl/Postman_tests_ns3886.postman_collection.json?rlkey=0ji1msxuwpqjcy3iuxvis4q2f&st=6iclsah3&dl=0 <br />



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
![current checkstyle reports 0 violation](https://dl.dropbox.com/scl/fi/5gcr3vfpt7xrgzqpknyp8/checkstyle.png?rlkey=dh4y87fqu9vd8th72r16cewet&st=la8otewt&dl=0)

# Code Documentation
Generate Javadocs
```
mvn javadoc:javadoc
```
Open in browser ./target/site/apidocs/index.html
![javadoc generated successfully](https://dl.dropbox.com/scl/fi/iafacjcyrrtpa3ub71wvh/javadoc.png?rlkey=a72r7ogn63fbui2m1xcwa7da4&st=ubd4jiui&dl=0)
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
![0 pmd violations](https://dl.dropbox.com/scl/fi/utdukkbj5u7ywxf1estht/pmd.png?rlkey=nfhcita2utj1ljfnchmxl66p0&st=0cn4qb4g&dl=0)

# Running Tests and Jacoco Report
As part of `mvn clean verify` , tests under ./src/test/java must have been executed.

&nbsp;

### Test Reports
From the previously run `mvn clean verify`, find '.txt' files under './target/surefire-reports/' to view test results <br/>
Or you can run mvn clean test and view console output <br />
![All tests ran successfully](https://dl.dropbox.com/scl/fi/3edd1mr4axj33al47mvjx/tests.png?rlkey=ucqo7kama387f0dzzihrklxm1&st=9sj7fo6m&dl=0)
&nbsp;

### Jacoco Report
From the previously run `mvn clean verify` <br />
&emsp;&emsp;Find file 'index.html' under '/target/site/jacoco/' and<br />
Open with any browser to view jacoco results
![jacoco report shows >90% coverage](https://dl.dropbox.com/scl/fi/v84lsix7x2dcwfpn3tyb8/jacoco.png?rlkey=gicmg9d1c2h428hm4tlqc11ia&st=8jeshnt6&dl=0)


# Testing EndPoint With Postman
&emsp; 1.Install Postman  (https://www.postman.com/)
&nbsp;

&emsp; 2.Download the following JSON:    
&emsp;  &emsp; [click here to download](https://www.dropbox.com/scl/fi/qeonw7222mvijuhdk3zyl/Postman_tests_ns3886.postman_collection.json?rlkey=0ji1msxuwpqjcy3iuxvis4q2f&st=x1b8bvoj&dl=0)        
&emsp;  &emsp; or visit <br/>
&emsp;&emsp; https://www.dropbox.com/scl/fi/qeonw7222mvijuhdk3zyl/Postman_tests_ns3886.postman_collection.json?rlkey=0ji1msxuwpqjcy3iuxvis4q2f&st=x1b8bvoj&dl=0
&nbsp;

&emsp; 3.Go to 'my workspace' in Postman, click 'import', and select the downloaded json.<br/>
&nbsp;

On import, tests should be available as follows:
![postman_ss](https://dl.dropbox.com/scl/fi/cw99dgetunltokmqm4vs9/postman.png?rlkey=bayfkdrthrqu3oa1r6acb4s99&st=ulae5haf&dl=0)

# Endpoints

## \[GET /, GET /index, GET /home\]
    Expected Input:
            none
    On Success:
        HTTP 200 Status Code : OK. Returns following message.
        """
        Welcome, in order to make an API call direct your browser or Postman to an endpoint 

        This can be done using the following format:

        http:127.0.0.1:8080/endpoint?arg=value
        """
    On Failure: 
        If any  exception occurs:
            Http 400 Status Code BAD_REQUEST
            with message "An Error has occurred"

## GET /retrieveCourses
    Expected Input:
            courseCode (int)
    On Success:
        HTTP 200 Status Code : OK
        Fetches information regarding all courses with particular coursecode
    On Failure: 
        If course was not found:
            HTTP 404 Status Code : NOT_FOUND
            with message No Course was Found with course code :  {{course_code}}
        or If any other exception occurs:
            Http 400 Status Code BAD_REQUEST
            with message "An Error has occurred"

## GET /retrieveDept
    Expected Input:
            deptCode (String) : department code
    On Success:
        HTTP 200 Status Code : OK
        Fetches information regarding all courses in the department
    On Failure: 
        If department was not found:
            HTTP 404 Status Code : NOT_FOUND
            with message "Department Not Found"
        or If any other exception occurs:
            Http 400 Status Code BAD_REQUEST
            with message "An Error has occurred"

## GET /getMajorCountFromDept
    Expected Input:
            deptCode (String) : department code
    On Success:
        HTTP 200 Status Code : OK
        Returns number of majors in the department
        in the format "There are: {{count}} majors in the department",
    On Failure: 
        If department was not found:
            HTTP 404 Status Code : NOT_FOUND
            with message "Department Not Found"
        or If any other exception occurs:
            Http 400 Status Code BAD_REQUEST
            with message "An Error has occurred"


## GET /idDeptChair
    Expected Input:
            deptCode (String) : department code
    On Success:
        HTTP 200 Status Code : OK
        Displays the department chair for the specified department.
        in the format "{{name}} is the department chair.",
    On Failure: 
        If department was not found:
            HTTP 404 Status Code : NOT_FOUND
            with message "Department Not Found"
        or If any other exception occurs:
            Http 400 Status Code BAD_REQUEST
            with message "An Error has occurred"

## PATCH /addMajorToDept
    Expected Input:
            deptCode (String) : department code
    On Success:
        HTTP 200 Status Code : OK
        Adds a student to the specified department.
        returns "Attribute was updated successfully"
    On Failure: 
        If department was not found:
            HTTP 404 Status Code : NOT_FOUND
            with message "Department Not Found"
        or If any other exception occurs:
            Http 400 Status Code BAD_REQUEST
            with message "An Error has occurred"

## PATCH /removeMajorFromDept
    Expected Input:
            deptCode (String) : department code
    On Success:
        HTTP 200 Status Code : OK
        Removes a student from the specified department.
        returns "Attribute was updated or is at minimum"
    On Failure: 
        If department was not found:
            HTTP 404 Status Code : NOT_FOUND
            with message "Department Not Found"
        or If any other exception occurs:
            Http 400 Status Code BAD_REQUEST
            with message "An Error has occurred"



## GET /retrieveCourse
    Expected Input:
        deptCode (String) : department code
        courseCode (int) : course Code
    On Success:
        HTTP 200 Status Code : OK
        Fetches information regarding the courses in that department
    On Failure: 
        If course was not found:
            HTTP 404 Status Code : NOT_FOUND
            with message "Course Not Found"
        or If any other exception occurs:
            Http 400 Status Code BAD_REQUEST
            with message "An Error has occurred"


## GET /isCourseFull
    Expected Input:
        deptCode (String) : department code
        courseCode (int) : course Code
    On Success:
        HTTP 200 Status Code : OK
        Returns true of course if full , false otherwise
    On Failure: 
        If course was not found:
            HTTP 404 Status Code : NOT_FOUND
            with message "Course Not Found"
        or If any other exception occurs:
            Http 400 Status Code BAD_REQUEST
            with message "An Error has occurred"

## GET /findCourseLocation
    Expected Input:
        deptCode (String) : department code
        courseCode (int) : course Code
    On Success:
        HTTP 200 Status Code : OK
        Displays the location for the specified course.
        in the format "{{location}} is where the course is located.",
    On Failure: 
        If course was not found:
            HTTP 404 Status Code : NOT_FOUND
            with message "Course Not Found"
        or If any other exception occurs:
            Http 400 Status Code BAD_REQUEST
            with message "An Error has occurred"

## GET /findCourseInstructor
    Expected Input:
        deptCode (String) : department code
        courseCode (int) : course Code
    On Success:
        HTTP 200 Status Code : OK
        Displays the instructor for the specified course.
        in the format "{{name}} is the instructor for the course.",
    On Failure: 
        If course was not found:
            HTTP 404 Status Code : NOT_FOUND
            with message "Course Not Found"
        or If any other exception occurs:
            Http 400 Status Code BAD_REQUEST
            with message "An Error has occurred"

## GET /findCourseTime
    Expected Input:
        deptCode (String) : department code
        courseCode (int) : course Code
    On Success:
        HTTP 200 Status Code : OK
        Displays the time the course meets at for the specified course.
        in the format "The course meets at: {{timeslot}}"
    On Failure: 
        If course was not found:
            HTTP 404 Status Code : NOT_FOUND
            with message "Course Not Found"
        or If any other exception occurs:
            Http 400 Status Code BAD_REQUEST
            with message "An Error has occurred"


## PATCH /dropStudentFromCourse
    Expected Input:
        deptCode (String) : department code
        courseCode (int) : course Code
    On Success:
        HTTP 200 Status Code : OK
        Drops student from course if it isn't the last student
        returns "Student has been dropped."
    On Failure: :
        If it is the last student in the course:
            HTTP 400 Status Code : BAD_REQUEST
            with message "Student has not been dropped."
        If course was not found:
            HTTP 404 Status Code : NOT_FOUND
            with message "Course Not Found"
        or If any other exception occurs:
            Http 400 Status Code BAD_REQUEST
            with message "An Error has occurred"


## PATCH /setEnrollmentCount
    Expected Input:
        deptCode (String) : department code
        courseCode (int) : course Code
        count (int) : enrollment count
    On Success:
        HTTP 200 Status Code : OK
        Sets number of students enrolled for each course.
        returns "Attributed was updated successfully."
    On Failure: :

        If course was not found:
            HTTP 404 Status Code : NOT_FOUND
            with message "Course Not Found"
        or If any other exception occurs:
            Http 400 Status Code BAD_REQUEST
            with message "An Error has occurred"



## PATCH /changeCourseTime
    Expected Input:
        deptCode (String) : department code
        courseCode (int) : course Code
        time (String) : time
    On Success:
        HTTP 200 Status Code : OK
        If the course exists, its time is updated to the provided time.
        returns "Attributed was updated successfully."
    On Failure: :
        If course was not found:
            HTTP 404 Status Code : NOT_FOUND
            with message "Course Not Found"
        or If any other exception occurs:
            Http 400 Status Code BAD_REQUEST
            with message "An Error has occurred"


## PATCH /changeCourseTeacher
    Expected Input:
        deptCode (String) : department code
        courseCode (int) : course Code
        teacher (String) : teacher
    On Success:
        HTTP 200 Status Code : OK
        If the course exists,its instructor is updated to the provided instructor.
        returns "Attributed was updated successfully."
    On Failure: :
        If course was not found:
            HTTP 404 Status Code : NOT_FOUND
            with message "Course Not Found"
        or If any other exception occurs:
            Http 400 Status Code BAD_REQUEST
            with message "An Error has occurred"


## PATCH /changeCourseLocation
    Expected Input:
        deptCode (String) : department code
        courseCode (int) : course Code
        location (String) : location
    On Success:
        HTTP 200 Status Code : OK
        Changes course location to provided location.
        returns "Attributed was updated successfully."
    On Failure: :
        If course was not found:
            HTTP 404 Status Code : NOT_FOUND
            with message "Course Not Found"
        or If any other exception occurs:
            Http 400 Status Code BAD_REQUEST
            with message "An Error has occurred"

## PATCH /enrollStudentInCourse
    Expected Input:
        deptCode (String) : department code
        courseCode (int) : course Code
    On Success:
        HTTP 200 Status Code : OK
        Enrolls student in course.
        returns "Successfully enrolled."
    On Failure: :
        if course has reached max capacity:
            Http 400 Status Code BAD_REQUEST
            with message "Unable to enroll. Course has already reached max capacity"
        If course was not found:
            HTTP 404 Status Code : NOT_FOUND
            with message "Course Not Found"
        or If any other exception occurs:
            Http 400 Status Code BAD_REQUEST
            with message "An Error has occurred"
