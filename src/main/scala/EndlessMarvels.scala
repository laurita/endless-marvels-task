import dispatch._, Defaults._
import java.security.MessageDigest
import scala.io.Source._
import scala.util.Success
import scala.util.parsing.json._

object EndlessMarvels {

  def main(args: Array[String]) {
    val source = fromFile("private.txt")

    // public key used as apikey in HTTP requests
    val PUBLIC = "ee45f5431cb5912ca20ee048eefa282d"
    // private key used for MD5 hash computation
    val PRIVATE = source.getLines mkString "\n"

    println("calculating MD5...")

    val time = System.currentTimeMillis().toString
    println(s"time: $time")

    val hash = calculateMD5(time, PRIVATE, PUBLIC)
    println(s"md5:$hash")

    val request = url("http://gateway.marvel.com/v1/public/characters")
      .addQueryParameter("ts", time)
      .addQueryParameter("apikey", PUBLIC)
      .addQueryParameter("hash", hash).GET

    val responseFuture = Http(request OK as.Response(identity))

    responseFuture onComplete{
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
        println(data)

        val characters = data.map(l => l("name"))
        println(characters)

      case _ => println("KO")
    }

  }

  def calculateMD5(ts: String, privateKey: String, publicKey: String): String = {
    val md5 = MessageDigest.getInstance("MD5")
    val md = md5.digest((ts + privateKey + publicKey).getBytes)
    md.map("%02X".format(_).toLowerCase).mkString
  }
}