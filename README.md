# Chess club administration app
The local chess club wants to keep track of their members. The main thing that they want to
keep track of is the ranking of each member.

## Running the project

Vaadin frontend build

`mvn vaadin:build-frontend`

Starting the project:

`mvn spring-boot:run`


## Using the API
```
curl -X POST http://localhost:8084/api/members -H "Content-Type: application/json" -d '{"name":"John","surname":"Doe","email":"test@example.com","birthday":"1990-01-01"}' -v
```

``` 

curl -X POST  'http://localhost:8084/api/matches' \
-H 'Content-Type: application/json' \
-d '{
    "player1": {
        "id": 6
    },
    "player2": {
        "id": 1
    },
    "player1Score": 1,
    "player2Score": 0
}'
```

## Running the application - Vaadin

`http://localhost:8084/`