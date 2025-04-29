@echo off
set JAVA_HOME=C:\Program Files\Java\jdk-17
set PATH=%JAVA_HOME%\bin;%PATH%
set JAVAFX_HOME=C:\Program Files\Java\javafx-sdk-17.0.6

java --module-path "%JAVAFX_HOME%\lib" --add-modules javafx.controls,javafx.fxml -cp "target/classes;Drivers/mysql-connector-j-9.3.0/mysql-connector-j-9.3.0.jar" Application.main 