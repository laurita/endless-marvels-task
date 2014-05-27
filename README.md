endless-marvels-task
====================

A small program that reports the comics characters' popularity changes in social media by use of Marvels API and Datasift API. Written in Scala using Akka.

## Description

This is an application that uses the [Marvel](http://marvel.com/) API to get comics characters and analyse their popularity in the social media by use of [DataSift](http://datasift.com/) API.
It is written in Scala and uses [Akka](http://akka.io/) - an actor model for building concurrent application on the JVM and [Dispatch](http://dispatch.databinder.net/Dispatch.html) - a library for asynchronous HTTP interaction.
First of all, the application requests a Marvel API for a list of characters. Then it creates a DataSift stream that filters the social interactions (in this example only Twitter tweets) that mention any of the Marvel characters. An Akka actor waits for incoming interactions, parses them and adds the smaller version of them (in JSON) to the list of already gathered interactions. The dumps of the collected interactions are written to the files named by the timestamp every minute. This is done by use of Akka scheduler. Together with the dumping, the current popularity of the characters is reported.

## Limitations

This is a very quick and dirty version of a program. The limitations include:
 1. Only the first 100 characters from the Marvel API are included. This can be extended by making subsequent requests with the first response etag;
 2. Only English Twitter tweets are filtered. The extension would include other social media provided by DataSift;
 3. There is very little error checking;
 4. No tests;
 5. Almost no analysis is done. The graphs could be drawn that show the change of the character popularity by use of the timestamped dumps, some NLP could be included to infer the mention of the character, the character names could be cleaned to find more mentions (e.g. emit the names in parenthesis and such)...

## Running the program

There are two ways for running the program.
### 1. Make package from the code yourself

1. Fork the repository
2. Clone it
3. cd to the project directory
4. run `mvn clean compile assembly:single`
5. run `java -cp endless_marvels-1.0-SNAPSHOT-jar-with-dependencies.jar EndlessMarvels <Marvel public key> <Marvel private key> <DataSift API username> <DataSift API key>`

### 2. Use a ready-to-use jar
(This is not advisable. The jar is 36.4MB, since it contains a lot of dependencies.)

1. Download the jar
2. run `java -cp endless_marvels-1.0-SNAPSHOT-jar-with-dependencies.jar EndlessMarvels <Marvel public key> <Marvel private key> <DataSift API username> <DataSift API key>`