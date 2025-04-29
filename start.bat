@echo off
set "JAVA_HOME=C:\Program Files\Java\jdk-17"
set "PATH=%JAVA_HOME%\bin;%PATH%"
set "MAVEN_OPTS=--add-opens java.base/java.lang=ALL-UNNAMED"
call mvnw.cmd clean javafx:run 