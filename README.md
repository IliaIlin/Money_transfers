# Money transfers

Standalone RESTful application for money transfers between accounts. It is written in Java without usage of mainstream frameworks such as Spring and Hibernate, database is in-memory.  

To create the executable jar run:

`mvn clean package -DskipTests`

After that run jar file:

`java -jar target/money-transfers-executable.jar`

Sample requests are in 

`/src/main/resources/Sample_requests_Insomnia_export.json`
