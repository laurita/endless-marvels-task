import akka.actor.{ActorLogging, Actor}
import com.fasterxml.jackson.databind.JsonNode
import java.io.{BufferedWriter, FileWriter, File}
import scala.util.parsing.json.JSONObject
import collection.immutable.ListMap

class DatasiftStreamActor(characters: List[String]) extends Actor with ActorLogging {
  // initially there are no interactions in state map
  def receive = initial(Map[String, List[JSONObject]]())
  
  def initial(interactions: Map[String, List[JSONObject]]): Receive = {
    // add a new interaction
    case AddInteraction(inter: JsonNode) =>
      val text = inter.get("twitter").get("text").asText().toLowerCase
      // find the character that was mentioned in interaction
      val character = characters.find(s => text.contains(s)).head
      val filteredInteraction = filterInteraction(inter)
      // if character already in map, get it's interactions
      // otherwise return empty list
      val oldInteractions =
        if (interactions.contains(character)) {
          interactions.get(character).get
        }
        else List[JSONObject]()
      // add new interaction to the old list
      val newInteractions = interactions.updated(character, filteredInteraction :: oldInteractions)
      // change the state of actor by changing receive behavior
      context.become(initial(newInteractions))

    // make a dump
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
      val counts = interactions.map(kv => (kv._1, kv._2.length))
      val sortedByCounts = ListMap(counts.toList.sortBy(-_._2):_*)
      println(s"characters ordered by mention counts at $ts")
      sortedByCounts.foreach(println)
    // report unknown message
    case m =>
      log.info("got unknown message "+ m)
  }

  // returns shrinked json object made of Datasift interaction
  def filterInteraction(inter: JsonNode): JSONObject = {
    val interaction = inter.get("interaction")
    val username = interaction.get("author").get("username").asText()
    val content = inter.get("twitter").get("text")
    JSONObject(Map("username" -> username, "text" -> content))
  }

}

// immutable message objects to exchange between actors
sealed trait Message
case class AddInteraction(inter: JsonNode) extends Message
case object Dump
