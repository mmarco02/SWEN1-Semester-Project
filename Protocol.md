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
│   │   │
│   │   └──users
│   │    
│   └───http
│   │    
│   └───persistence
│   │    
│   └───service
│       
└───resources 
    │
    └───db  
```

## Domain Objects

#### MediaEntry
- **id**: `int`
- **title**: `String`
- **description**: `String`
- **mediaType**: `MediaType`
- **releaseYear**: `Year`
- **genres**: `List<String>`
- **age**: `int`

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
- **createdAt**: `Timestamp`

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
#### UserService
- Contains Functions to access CRUD operations in Repositories,
create and validate Tokens and other utility methods. 

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
For sending HTTP responses from the Server I have a `HttpUtils` class which has `sendMethod()`
which gets the HttpExchange, Status code, response and content type. <br>
To send the responses with the Http Codes, I created an `HttpStatus` Enum
which contains all the Http Codes I need, with the corresponding Descriptions.
The Enum is used in the Handlers to get a better overview of which code is sent.

### Handlers
I have split my Handlers into packages for each Entity
and within that into separate classes for the various Operations.
Since the `com.sun.net` packages HttpServer doesnt have an implementation for Path Variables (e.g. /api/users/{userId}/profile)
I have made my own `PathUtils` Class which has methods to create Regex Pattern like
`/api/users/(\d+)"/profile` so when creating the context in my `Server` Class i can write URLs for Endpoints like mentioned earlier.

### Json Parsing
To make my objects ready to be send I have to parse them into a Json Stirng.
For this i use my `JsonObect` Class that uses the `com.fasterxml.jackson` package.
It has a default implementation for `toJson()` which maps an Object to a Json String.
Each of my Domain Classes inherits from the `JsonObject`
The child-class can also overwrite the implementation of that method to map only the wanted fields to a Json String.


## Dependencies
`com.sun.net` package <br>
`com.fasterxml.jackson` package<br>
`java.io` and `java.sql` packages<br>
`java.util` packages for Collections, Regex, etc.<br>


