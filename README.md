To run the application execute the following command
```
mvn spring-boot:run
```

Then to launch tasks to check metric collection you can send a curl request:
```
curl -X 'POST' localhost:8080/triggerTask
```
