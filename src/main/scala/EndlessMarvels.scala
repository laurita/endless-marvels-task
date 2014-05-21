import java.security.MessageDigest
import scala.io.Source._

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

  }

  def calculateMD5(ts: String, privateKey: String, publicKey: String): String = {
    val md5 = MessageDigest.getInstance("MD5")
    val md = md5.digest((ts + privateKey + publicKey).getBytes)
    md.map("%02X".format(_).toLowerCase).mkString
  }
}