endless-marvels-task
====================

A small program that reports the comics characters' popularity changes in social media by use of Marvels API and Datasift API. Written in Scala using Akka.

## Description

This is an application that uses the [Marvel](http://marvel.com/) API to get comics characters and analyse their popularity in the social media by use of [DataSift](http://datasift.com/) API.
It is written in Scala and uses [Akka](http://akka.io/) - an actor model for building concurrent application on the JVM, [Dispatch](http://dispatch.databinder.net/Dispatch.html) - a library for asynchronous HTTP interaction.
First of all, the application requests a Marvel API for a list of characters, the it creates a DataSift stream that filters the social interactions (in this example only Twitter tweets) that mention any of the Marvel characters. An Akka actor waits for incoming interactions, parses them and adds the smaller version of them (in JSON) to the list of already gathered interactions. The dumps of the collected interactions are written to the files named by the timestamp every minute. This is done by use of Akka scheduler. Together with the dumping, the current popularity of the characters are reported.
## Running the program

There are two ways for running the program.
### 1. Make package from the code yourself

1. Fork the repository
2. Clone it
3. cd to the project directory
4. run `mvn clean compile assembly:single`
5. run `java -cp endless_marvels-1.0-SNAPSHOT-jar-with-dependencies.jar EndlessMarvels <Marvel public key> <Marvel private key> <DataSift API username> <DataSift API key>`

### 2. Use a ready-to-use jar

1. Download the jar
2. run `java -cp endless_marvels-1.0-SNAPSHOT-jar-with-dependencies.jar EndlessMarvels <Marvel public key> <Marvel private key> <DataSift API username> <DataSift API key>`