import dispatch._, Defaults._
import java.security.MessageDigest
import scala.io.Source._
import scala.util.Success
import scala.util.parsing.json._

object EndlessMarvels {

  // executes the program
  // 1. gets the characters from Marvel API
  // 2. creates DatasiftAPI object passing username and api key
  // as well as list of Marvel characters
  def main(args: Array[String]) {
    // Marvels public key used as apikey in HTTP requests
    //val MARVELS_PUBLIC = getStrFromFile("marvels_public.txt")
    val MARVELS_PUBLIC = args(0)
    // Marvels private key used for MD5 hash computation
    //val MARVELS_PRIVATE = getStrFromFile("marvels_private.txt")
    val MARVELS_PRIVATE = args(1)
    // Datasift username
    val DATASIFT_USERNAME = args(2)
    // Datasift api key
    val DATASIFT_API_KEY = args(3)
    val future = getMarvelsCharacters(MARVELS_PRIVATE, MARVELS_PUBLIC)
    future onComplete {
      case Success(value) =>
        val response = value.getResponseBody
        val json = JSON.parseFull(response)
        val data = json match {
          case Some(m: Map[String, Any]) => m("data") match {
            case d: Map[String, Any] => d("results") match {
              case r: List[Map[String, Any]] => r
            }
          }
        }
        val chars: List[String] = data.map(l => l("name").asInstanceOf[String].toLowerCase)
        val datasiftApi = new DatasiftAPI(DATASIFT_USERNAME, DATASIFT_API_KEY, chars)
        datasiftApi.run()
      //case _ => println("KO")
    }
  }

  // returns a string with file contents
  def getStrFromFile(file: String): String = {
    fromFile(file).getLines mkString "\n"
  }

  // returns a Future with Response containing first 100 Marvel characters
  def getMarvelsCharacters(privateKey: String, publicKey: String) = {
    // get current time
    val time = System.currentTimeMillis().toString
    // create hash from time, private key and public key
    val hash = calculateMD5(time, privateKey, publicKey)
    // TODO: create another Akka actor to get a full list of characters
    // create a request to get characters
    val request = url("http://gateway.marvel.com/v1/public/characters")
      .addQueryParameter("ts", time)
      .addQueryParameter("limit", "100")
      .addQueryParameter("apikey", publicKey)
      .addQueryParameter("hash", hash).GET
    Http(request OK as.Response(identity))
  }

  // calculates the MD5 hash from timestamp, private and public keys for Marvel API
  def calculateMD5(ts: String, privateKey: String, publicKey: String): String = {
    val md5 = MessageDigest.getInstance("MD5")
    val md = md5.digest((ts + privateKey + publicKey).getBytes)
    md.map("%02X".format(_).toLowerCase).mkString
  }
}