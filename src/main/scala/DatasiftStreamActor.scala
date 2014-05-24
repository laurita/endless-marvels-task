import akka.actor.{ActorLogging, Actor}
import com.fasterxml.jackson.databind.JsonNode
import java.io.{BufferedWriter, FileWriter, File}
import scala.util.parsing.json.JSONObject

class DatasiftStreamActor extends Actor with ActorLogging {
  println("creating DatasiftStreamActor")

  // initially there are no interactions in state map
  def receive = initial(Map[String, List[JSONObject]]())
  
  def initial(interactions: Map[String, List[JSONObject]]): Receive = {
    case AddInteraction(character: String, inter: JsonNode) =>
      val filteredInteraction = filterInteraction(inter)
      val oldInteractions =
        if (interactions.contains(character)) {

          val characterInteractions = interactions.get(character).get
          characterInteractions
        }
        else List[JSONObject]()
      val newInteractions = interactions.updated(character, filteredInteraction :: oldInteractions)
      context.become(initial(newInteractions))

    case Dump =>
      // restore the interactions to empty map
      context become initial(Map[String, List[JSONObject]]())
      // write json to file named by timestamp
      val ts = System.currentTimeMillis().toString
      val file = new File(ts)
      val bw = new BufferedWriter(new FileWriter(file))
      bw.write(JSONObject(interactions).toString())
      bw.close()
      // perform some reporting

    case m =>
      log.info("got unknown message "+ m)
  }

  def filterInteraction(inter: JsonNode): JSONObject = {
    val interaction = inter.get("interaction")
    val username = interaction.get("author").get("username").asText()
    val content = inter.get("twitter").get("text")
    JSONObject(Map("username" -> username, "text" -> content))
  }

}

sealed trait Message
case class AddInteraction(character: String, inter: JsonNode) extends Message
case object Dump
