@echo off
echo Starting Agrilink Application...
echo.

set "JAVA_HOME=C:\Program Files\Java\jdk-17"
set "PATH=%JAVA_HOME%\bin;%PATH%"
set "CLASSPATH=target/classes;Drivers/mysql-connector-j-9.3.0/mysql-connector-j-9.3.0.jar"
set "JAVAFX_HOME=C:\javafx\javafx-sdk-17.0.14"

echo Using Java from: %JAVA_HOME%
echo Using JavaFX from: %JAVAFX_HOME%
echo.

java --module-path "%JAVAFX_HOME%\lib" --add-modules javafx.controls,javafx.fxml -cp "%CLASSPATH%" Application.main

pause 