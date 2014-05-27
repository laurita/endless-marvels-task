import akka.actor.{ActorRef, Props, ActorSystem}
import com.datasift.client.core._
import com.datasift.client.stream._
import com.datasift.client._
import java.util.concurrent.atomic.AtomicLong
import scala.concurrent.duration._

class DatasiftStream(datasiftUsername: String, datasiftApiKey: String, characters: List[String]) {

  def getCharacters: List[String] = {
    characters
  }

  def run() {
    // create datasift client
    val datasiftClient = createDatasiftClient(datasiftUsername, datasiftApiKey)
    println("datasift client created")
    // build csdl query
    val csdl = buildCsdlQuery(characters)
    println("csdl created: "+ csdl)
    // compile csdl query
    val result = compileCsdlQuery(datasiftClient, csdl)
    // check for failure
    checkForFailure(result)
    // create akka actor system for interaction logging
    val system = ActorSystem("system")
    println("created Akka system")
    val streamActor = system.actorOf(Props(new DatasiftStreamActor(characters)), name="streamActor")

    // schedule dumping after 0ms repeating every 50ms
    import system.dispatcher
    val cancellable =
      system.scheduler.schedule(60000 milliseconds,
        60000 milliseconds,
        streamActor,
        Dump)

    // start live stream
    startLiveStream(datasiftClient, result, streamActor)

  }

  def createDatasiftClient(username: String, apiKey: String): DataSiftClient = {
    val config = new DataSiftConfig(username, apiKey)
    new DataSiftClient(config)
  }

  def compileCsdlQuery(datasift: DataSiftClient, csdl: String): Stream = {
    datasift.core().compile(csdl).sync()
  }

  def checkForFailure(result: Stream) {
    if (result.isSuccessful) {
      //if true an exception may have caused the request to fail, inspect the cause if available
      if (result.failureCause() != null) { //may not be an exception
        result.failureCause().printStackTrace()
      }
    }
  }

  def startLiveStream(datasift: DataSiftClient, result: Stream, actor: ActorRef) {
    //handle exceptions that can't necessarily be linked to a specific stream
    datasift.liveStream().onError(new ErrorHandler())
    //handle delete message
    datasift.liveStream().onStreamEvent(new DeleteHandler())
    //process interactions
    datasift.liveStream().subscribe(new Subscription(Stream.fromString(result.hash()), actor, characters))
    println("stream subscribed")
  }

  def buildCsdlQuery(chars: List[String]): String = {
    // english tweets containing some of the characters
    "interaction.type == \"twitter\" AND language.tag == \"en\" AND twitter.text contains_any \""+ chars.mkString(",") +"\""
  }
}


class Subscription(stream: Stream, actor: ActorRef, chars: List[String]) extends StreamSubscription(stream) {
  println("Subscription created with hash "+ stream.hash())
  val count = new AtomicLong()

  def onDataSiftLogMessage(di: DataSiftMessage) {
    //di.isWarning() is also available
    println((if (di.isError) "Error" else if (di.isInfo) "Info" else "Warning") + ":\n" + di)
  }

  def onMessage(i: Interaction) {
    //println(" <> INTERACTION:\n" + i)
    actor ! AddInteraction(i.getData)
  }
}

class DeleteHandler extends StreamEventListener {
  def onDelete(di: DeletedInteraction) {
    //go off and delete the interaction if you have it stored. This is a strict requirement!
    //println("DELETED:\n " + di)
  }
}

class ErrorHandler extends ErrorListener {
  def exceptionCaught(t: Throwable) {
    t.printStackTrace()
    //do something useful...
  }
}