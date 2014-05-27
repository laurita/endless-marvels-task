endless-marvels-task
====================

A small program that reports the comics characters' popularity changes in social media by use of Marvels API and Datasift API. Written in Scala using Akka.


## Running the program

There are two ways for running the program.
### 1

1. Fork the repository
2. Clone it
3. cd to the project directory
4. run `mvn clean compile assembly:single`
5. run `java -cp endless_marvels-1.0-SNAPSHOT-jar-with-dependencies.jar EndlessMarvels <Marvel public key> <Marvel private key> <DataSift API username> <DataSift API key>`

### 2

1. Download the jar
2. run `java -cp endless_marvels-1.0-SNAPSHOT-jar-with-dependencies.jar EndlessMarvels <Marvel public key> <Marvel private key> <DataSift API username> <DataSift API key>`