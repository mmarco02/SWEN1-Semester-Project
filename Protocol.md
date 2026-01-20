**Author: Marco Molnár**
# MRP Project Protocol
## Folder Structure
```
project   
│
└───org.mrp
│   │
│   └───domain
│   │
│   └───handlers
│   │   │
│   │   └──mediaEntries
│   │   └──users
│   │   └──ratings
│   │    
│   └───http
│   │    
│   └───persistence
│   │   │ 
│   │   └───implementations
│   │    
│   └───service
│   │   │ 
│   │   └───utils
│   │    
└───resources 
│   │
│   └───db  
│
└───test
    │
    └───org.mrp
        │
        └───domainTests
        │
        └───httpTests
```

## Domain Objects

#### MediaEntry
- **id**: `int`
- **title**: `String`
- **description**: `String`
- **mediaType**: `MediaType`
- **releaseYear**: `Integer`
- **genres**: `List<String>`
- **age**: `int`
- **average rating** `double`
- **createdByUserId** `int`

#### Rating
- **id**: `int`
- **userId**: `int`
- **mediaEntryId**: `int`
- **starValue**: `int` (1–5)
- **comment**: `String`
- **timeStamp**: `LocalDateTime`

#### User
- **id**: `int`
- **username**: `String`
- **password**: `String`
- **salt**: `String`

#### UserProfile
- **id**: `int`
- **userId**: `int`
- **email**: `String`
- **favoriteGenre**: `String`

#### UserToken
- **token**: `String`
- **userId**: `int`
- **createdAt**: `LocalDateTime`

### Favorite
- **id**: `int`
- **entryId**: `int`
- **userId**: `int`
- **createdAt**: `LocalDateTime`

### Like
- **id**: `int`
- **ratingId**: `int`
- **userId**: `int`

All domain classes implement the Builder Pattern
using a nested builder class and a `builder()` method,
providing a uniform way to create objects and makes them expandable.

## Service and Utility Classes
#### HashUtils <br>
- Contains functions for hashing a password (with salt) and verifying the hashed passwords
that are stored for the users when logging in
#### HttpUtils <br>
- Contains functions for sending HttpRepsonses with Status code, message etc.
#### PathUtils <br>
- Contains functions for creating Endpoint Paths using Path Templates,
so that I can use path variables (for example like Spring Controllers)
#### AuthUtils <br>
- Has Helper functions to get Bearer Token
#### UserService
- Contains functions to access CRUD operations in Repositories,
create and validate Tokens and other utility methods.
#### MediaService
- Contains functions to access MediaRepository and helper functions for Ratings
#### RatingService
- Contains functions access the RatingRepository and like ratings
#### FavoriteService
- Contains functions to access the FavoriteRepository


## Persistence
For my structure regarding persistence i created a Repository Interface
that takes 2 Generic types, the Entity Type and its ID Type.
This is implemented by a `BaseRepository` (an abstract class), which expets a `DatabaseConnection` in its constructor. <br>
The `BaseRepository` is then inherited by the specific Repositories. <br>
The Structure is strongly inspired by the Spring JPA Repositories.


## HTTP Server and Handling

### HttpServer
For the HttpServer I am using the `com.sun.net` package
In my `Server` class i have a `start()`method, which creates the Serer and the contexts.
The `run()` method, is the Method that gets called in the main method, it runs the Server on `port 8080`
and adds a shutdownHook which ends the server upon ending the program

### HTTP Responses
For sending HTTP responses from the Server I have a `HttpUtils` class which has `sendResponse()`
which gets the HttpExchange, Status code, response and content type. <br>
To send the responses with the Http Codes, I created an `HttpStatus` Enum
which contains all the Http Codes I need, with the corresponding Descriptions.
The Enum is used in the Handlers to get a better overview of which code is sent.

### Handlers
I have split my Handlers into packages for each Entity
and within that into separate classes for the various Operations.
Since the `com.sun.net` packages HttpServer doesnt have an implementation for Path Variables (e.g. /api/users/{id}/profile)
I have made my own `PathUtils` Class which has methods to create Regex Pattern like
`/api/users/(\d+)"/profile` so when creating the context in my `Server` Class i can write URLs for Endpoints like mentioned earlier.

### Token Authentication
Whenever a user that is already registered logs in successfully, a token consisting of the username and a random UUID with length 8 is created
(e.g. user1-abcd1234), this token must be sent as a HTTP Authentication Bearer for each following request.
I used a randomUUID after the "username-" because it makes more sense than having "mrpToken" due to safety issues.
For this I persist the Tokens in the Database with the creation date.
Each Token has an expiration time of 24 hours, after which the User has to log in again.
If a user already has an existing token and logs in again, the old token is overwritten by the new one.

### Json Parsing
To make my objects ready to be send I have to parse them into a Json Stirng.
For this i use my `JsonObect` Class that uses the `com.fasterxml.jackson` package.
It has a default implementation for `toJson()` which maps an Object to a Json String.
Each of my Domain Classes inherits from the `JsonObject`
The child-class can also overwrite the implementation of that method to map only the wanted fields to a Json String.


### Dependencies
`com.sun.net` package <br>
`com.fasterxml.jackson` package<br>
`java.io` and `java.sql` packages<br>
`java.util` packages for Collections, Regex, etc.<br>


## Database
As a Database I use PostgreSQL, which runs in a Docker container,
that is created by the `docker-compose.yaml`

#### docker-compose config
- Image: postres:latest

- Port: 5332 (mapped to 5432)

#### Database intialization
The `DatabaseConnection` class handles database setup.
It Uses JDBC to connect to `jdbc:postgresql://localhost:5332/postgres`,
then Reads the SQL commands from `/resources/db/init.sql`
First all the old tables are Dropped to clean the Databse,
then the tables are created on application start
The User, Password and BaseURL for initializing the connection are laoded
from `resources/application.properties`, the variables are then gotten from the `pom.xml`
to make it safer

#### Schema
The schema is visible in the `init.sql` but i will shortly 
explain some important details

Usernames are unique, since we are only logging in with a username and password
it makes more sense to keep them unique

Tokens have the Token (String) itself as the primary key. <br>  
I chose to do this because since the usernames are unique + there is a generated UUID
, it makes the Tokens unique to each user, so it can just be used as the Primary key. <br>
My implementation of the Repositories, which resembles the JPA Repositories makes it easily possible
to set the Type of the ID for an Entity in my Repositories. <br>
To Save the Genres for MediaEntries i use another Table calles MediaGenres which has a composite Primary key from the Entry Id and Genre

## Testing
I wrote about 60 Unit Tests to test the Media, Ratings, Users ans Utility/Service classes, all the main functionalities
are being tested like creating/deleting/assigning etc. <br>
I also wrote some Integration Tests for my Endpoints, which test the API and with that also the Repositories etc. 
The TestSetup is there to handle the HTTP Requests<br>
With integration tests there is a total of 96 Tests.
Test Coverage is: <br>
at about 90% for domain classes
almost 100% for the Handlers
at about 60 for most repositories
and at 100% for utils and like 80% for Service classes


## Version Control (git)
I used git for version control and Github to upload the project, i have 2 branches master and dev, the dev branch is my working branch in which i develop new features <br>
when a feature is done, i commit and push it, then i merge the dev branch into the master branch.
This way i always have my code backed up, i can ammend commits to go back to an earlier version and i dont lose progress.
The Link to the Github repo is in the README.md

## SOLID Principles
S (Single Responisibilty): <br>
I have seperated my Classes into their respective functionalities, every type of dependencies or functionality that needs Utility/Helper functions has its own Class in /service/utils
The Service classes are also seperated into their usage, so are the Repositories.
The Handlers are only handling a few routes with a Main handler for every route that then splits that main route into the seperate ones
<br> for example: main route /api/users; sub-route /api/users/{id}/profile; <br>
Each Handler is named according to exactly what kind of endpoint it is.

O (Open-Closed): <br>
All Domain classes have builders, this way it is easy to add new Properties.
The JsonObject class allows for more serialization options and apply it to every Class easily
<br> With the Repository Interface and the BaseRepository it is easy to add new Reporitory types without having to modify anything

L (Liskov-Subsitution): <br>
All concrete Repositories could be replaced with the Interface they implement `Repository<T, ID> `
The domain objects implement JsonObject and can be treated as one

I (Interface Segregation): <br>
For example Repository interface is simple and does not force the implementation of any functions that wouldnt be used



## Time Estimation

Intermediate Version:

| Task                         | Estimated Hours |                                                                     |
|------------------------------|-----------------|---------------------------------------------------------------------|
| **Project Setup**            | 2               | Setup of project structure, dependencies, and docker.               |
| **Domain**                   | 1               | domain classes, JSON serialization some helper functions.           |
| **Database**                 | 1.5             | Creating `init.sql`, defining tables, relationships etc..           |
| **Repositories**             | 3.5             | designing structure for `BaseRepository`and Repository interface.   |
| **HTTP Server and Handlers** | about 10        | implementing custom routing, request/response handling and endpoints. |
| **Authentication**           | 2               | token generation, validation, etc. and saving user credentials.     |
| **Services and Utils**       | 2.5             | writing util and Service classes                                    |
| **Testing**                  | 3.5             | writing tests, fixing and refactoring tests.                        |
| **Protocol**                 | 2.5             | writing the protocol                       |

Final Version:

| Task                         | Estimated Hours |                                                                          |
|------------------------------|-----------------|--------------------------------------------------------------------------|
| **Domain**                   | 2               | Adding new Domain Classes, Changing from Timestamp to LocalDateTime      |
| **Database**                 | 2               | Writing new Tables and adding Indexes                                    |
| **Repositories**             | 2               | Writing new Repositories                                                 |
| **HTTP Server and Handlers** | about 8         | Writing all the new Handlers and writing Service and Util classes for it |
| **Services and Utils**       | 4               | writing util and Service classes                                         |
| **Testing**                  | 7               | writing tests, fixing and refactoring tests.                             |
| **Protocol**                 | 1.25            | writing the protocol                                                     |