import dispatch._, Defaults._
import java.security.MessageDigest
import scala.io.Source._
import scala.util.Success
import scala.util.parsing.json._

object EndlessMarvels {

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
        println(json)

        val data = json match {
          case Some(m: Map[String, Any]) => m("data") match {
            case d: Map[String, Any] => d("results") match {
              case r: List[Map[String, Any]] => r
            }
          }
        }

        val chars: List[String] = data.map(l => l("name").asInstanceOf[String].toLowerCase)

        val datasiftStream = new DatasiftStream(DATASIFT_USERNAME, DATASIFT_API_KEY, chars)
        datasiftStream.run()

      //case _ => println("KO")
    }
  }

  def getStrFromFile(file: String): String = {
    fromFile(file).getLines mkString "\n"
  }

  def getMarvelsCharacters(privateKey: String, publicKey: String) = {
    val time = System.currentTimeMillis().toString

    val hash = calculateMD5(time, privateKey, publicKey)

    val request = url("http://gateway.marvel.com/v1/public/characters")
      .addQueryParameter("ts", time)
      .addQueryParameter("limit", "100")
      .addQueryParameter("apikey", publicKey)
      .addQueryParameter("hash", hash).GET

    Http(request OK as.Response(identity))
  }

  def calculateMD5(ts: String, privateKey: String, publicKey: String): String = {
    val md5 = MessageDigest.getInstance("MD5")
    val md = md5.digest((ts + privateKey + publicKey).getBytes)
    md.map("%02X".format(_).toLowerCase).mkString
  }
}