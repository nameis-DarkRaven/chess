# ♕ BYU CS 240 Chess

This project demonstrates mastery of proper software design, client/server architecture, networking using HTTP and WebSocket, database persistence, unit testing, serialization, and security.

## 10k Architecture Overview

The application implements a multiplayer chess server and a command line chess client.

[![Sequence Diagram](10k-architecture.png)](https://sequencediagram.org/index.html#initialData=C4S2BsFMAIGEAtIGckCh0AcCGAnUBjEbAO2DnBElIEZVs8RCSzYKrgAmO3AorU6AGVIOAG4jUAEyzAsAIyxIYAERnzFkdKgrFIuaKlaUa0ALQA+ISPE4AXNABWAexDFoAcywBbTcLEizS1VZBSVbbVc9HGgnADNYiN19QzZSDkCrfztHFzdPH1Q-Gwzg9TDEqJj4iuSjdmoMopF7LywAaxgvJ3FC6wCLaFLQyHCdSriEseSm6NMBurT7AFcMaWAYOSdcSRTjTka+7NaO6C6emZK1YdHI-Qma6N6ss3nU4Gpl1ZkNrZwdhfeByy9hwyBA7mIT2KAyGGhuSWi9wuc0sAI49nyMG6ElQQA)

## Modules

The application has three modules.

- **Client**: The command line program used to play a game of chess over the network.
- **Server**: The command line program that listens for network requests from the client and manages users and games.
- **Shared**: Code that is used by both the client and the server. This includes the rules of chess and tracking the state of a game.

## Starter Code

As you create your chess application you will move through specific phases of development. This starts with implementing the moves of chess and finishes with sending game moves over the network between your client and server. You will start each phase by copying course provided [starter-code](starter-code/) for that phase into the source code of the project. Do not copy a phases' starter code before you are ready to begin work on that phase.

## IntelliJ Support

Open the project directory in IntelliJ in order to develop, run, and debug your code using an IDE.

## Maven Support

You can use the following commands to build, test, package, and run your code.

| Command                    | Description                                     |
| -------------------------- | ----------------------------------------------- |
| `mvn compile`              | Builds the code                                 |
| `mvn package`              | Run the tests and build an Uber jar file        |
| `mvn package -DskipTests`  | Build an Uber jar file                          |
| `mvn install`              | Installs the packages into the local repository |
| `mvn test`                 | Run all the tests                               |
| `mvn -pl shared test`      | Run all the shared tests                        |
| `mvn -pl client exec:java` | Build and run the client `Main`                 |
| `mvn -pl server exec:java` | Build and run the server `Main`                 |

These commands are configured by the `pom.xml` (Project Object Model) files. There is a POM file in the root of the project, and one in each of the modules. The root POM defines any global dependencies and references the module POM files.

## Running the program using Java

Once you have compiled your project into an uber jar, you can execute it with the following command.

## Phase 2 Sequence Diagram

[Sequence Diagram URL](
https://sequencediagram.org/index.html?presentationMode=readOnly#initialData=IYYwLg9gTgBAwgGwJYFMB2YBQAHYUxIhK4YwDKKUAbpTngUSWDABLBoAmCtu+hx7ZhWqEUdPo0EwAIsDDAAgiBAoAzqswc5wAEbBVKGBx2ZM6MFACeq3ETQBzGAAYAdAE5M9qBACu2GADEaMBUljAASij2SKoWckgQaIEA7gAWSGBiiKikALQAfOSUNFAAXDAA2gAKAPJkACoAujAA9D4GUAA6aADeAETtlMEAtih9pX0wfQA0U7jqydAc45MzUyjDwEgIK1MAvpjCJTAFrOxclOX9g1AjYxNTs33zqotQyw9rfRtbO58HbE43FgpyOonKUCiMUyUAAFJForFKJEAI4+NRgACUh2KohOhVk8iUKnU5XsKDAAFUOrCbndsYTFMo1Kp8UYdKUAGJITgwamURkwHRhOnAUaYRnElknUG4lTlNA+BAIHEiFRsyXM0kgSFyFD8uE3RkM7RS9Rs4ylBQcDh8jqM1VUPGnTUk1SlHUoPUKHxgVKw4C+1LGiWmrWs06W622n1+h1g9W5U6Ai5lCJQpFQSKqJVYFPAmWFI6XGDXDp3SblVZPQN++oQADW6ErU32jsohfgyHM5QATE4nN0y0MxWMYFXHlNa6l6020C3Vgd0BxTF5fP4AtB2OSYAAZCDRJIBNIZLLdvJF4ol6p1JqtAzqBJoIcDcuj3ZfF5vD6L9sgwr5iWw63O+nxPF+SwfgC5wFrKaooOUCAHjysL7oeqLorE2IJoYLphm6ZIUgatJvqMJpEuGFoctyvIGoKwowKK4quiySaXvB5R0dojqiAULHarqmSxv604hvxEaFFGNowMJ8ZyrhyYwSWaE8tmuaYIBIJwSUVyviOowLpOfTTrOzbjq2fQHMWWmFNkPYwP2g69HpIEGeZ1ZTkGpnzu5bbLqu3h+IEXgoOge4Hr4zDHukmSYHZF5FNQ17SAAoruKX1ClzQtA+qhPi+JmNs2-x-opQJAcZXlFfOJWaQUOGIRFvqoU1YAYRi2HyXx+EsqU5JgMJAZVXO5FMm6VFcjyMZBvRYTCd5oYUW6bENTJM3aDAsJoBAzCKsqnXwd1S29UYKDcEJQZDXW1WjWaqgFJaMhnRShiyRtDHzdVPEqGxmnlGhkVqQgeZKVAK1XmmPQwFZENsfFYClI53SltDZicAF66BJCtq7tCMAAOKjqy0WnnF57MNp174xl2X2KOBXDcVv7WWVqalP0hVzlBGmg-V8mlMgsSE6MqiodCwtqO1WHfSgR1jSd-WDZz6C3ZRkbUVNa1xu9c2M2gi3y+o4McVrwYbVtO0wHtCAHU6P35OJiHi0TsKq+N6vlDRMbKgTo6Cgbd3G3bCG+6MjJQ3sMAy2xZzlWUuNC0TQMg3HQeXFDfR0yL4yVP0WcoAAktIH5PCemQGhWYFTDoCCgA2FegR5mejgAcqO-wwI0MNJWDBTw4jA7Ixn+eqDnFR56ORcl1MZf6qR9xNzXdcNwZVfN6MbejB3XdoyuniBRu2A+FA2DcPAgmGBLKQxWeOQU+xOmVLUDS0-TznKzVi4vvnm8L22LP5D+hzPWUFv6t0bm2Oq+RVqej1BLWEcAL4SylliaODseqkkVpdD+btpQey9qbWapsFriSDuCQh5ttq7SVDbNBjsYCwMyPA-OYkMESXZOUaMocUD+xwr9UG5REFeiYaOZOPNU58x7uzdehdi4lRZn3cmA8nKlhkVPEq-l96YwCJYM6SFkgwAAFIQB5NwwIS8QANjJnfNk1lyjVEpHeFo+cGbXS5v8F8p9gC6KgHACASEoAlzUXI5msNAECOAW4pm+xPG1x8X4gJQT87qN-FAhqAArExaB4HGJ5MglAaIOp0LYX1CkSs9a4PNPgzWb15BCl1lE-WpDJHB04etOpFtqH7WKcdTBFJmGT2kJU9hloCESyIck6QAdwxkPlOMyhltra214ug3p7pGLYC0CI0YsIWHaGGQ9DkfJNl6m4RMwZ0cCh-SMVk-JOZgbiNTGnMoEdSqKLvsooeqNd4YyCgELw3iuxelgMAbAp9CDxESNfUm8NbEQ3saldKmVsrGFKuEuOpRHnAhaeQkA3A8AIPxVmApmFUF8NWYbd0eLgWu2mctfIlpEDApkj7Li8gemUo9ES2lpCGUciZXgFlCAKHsvJY7alBKDl8tKAK2ACgfbzNFV1K5AigV4DEXVFpQFUYKPyP3JGSQI5R38kAA)

```sh
java -jar client/target/client-jar-with-dependencies.jar

♕ 240 Chess Client: chess.ChessPiece@7852e922
```
