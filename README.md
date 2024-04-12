# OneTeamAnywhere Coding Challenge

## Prerequisites

1. Download and extract [Java SDK](https://download.java.net/java/GA/jdk22/830ec9fcccef480bb3e73fb7ecafe059/36/GPL/openjdk-22_windows-x64_bin.zip).
2. Set the `JAVA_HOME` environment variable.
3. Add `$JAVA_HOME/bin` in `PATH` environment variable.
4. Test if the Java SDK installed successfully by executing the following command:
  
  ```
  > java -version
  
  openjdk version "22" 2024-03-19
  OpenJDK Runtime Environment (build 22+36-2370)
  OpenJDK 64-Bit Server VM (build 22+36-2370, mixed mode, sharing)
  ```

## Start Application

1. Go to the project directory.
2. Execute the following command:

  ```
  > gradlew bootRun
  ```
  
  Wait for the following log output to appear:
  
  ```
  ... Started NotesApplication in 2.444 seconds (process running for 2.808)
  ```

## Developer Notes

* The base URL of the application is `http://localhost:8080`.
* Swagger UI is available at `http://localhost:8080/webjars/swagger-ui/index.html`.
* A collection of postman scripts is available in the `api/` directory. Import the file in Postman.