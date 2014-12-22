// akka-http: routing

import akka.actor.ActorSystem
import akka.http.Http
import akka.http.server.Route
import akka.http.server.Directives._
import akka.stream.FlowMaterializer
import akka.stream.scaladsl.MaterializedMap

import scala.util.Random

import java.util.Base64

object Server1 extends App {
  implicit val system = ActorSystem()
  implicit val materializer = FlowMaterializer()
  implicit val executionContext = system.dispatcher

  val binding = Http().bind("localhost", 8080)

  val materializedMap: MaterializedMap = binding startHandlingWith Route.handlerFlow {
    path("") {
      get {
        complete("http-exp0 Server1")
      }
    } ~
    path("ping") {
      get {
        complete("pong")
      }
    } ~
    path("echo") {
      post {
        entity(as[String]) {complete(_)}
      }
    } ~
    pathPrefix("command")
    {
      post{
        path("crash") {
          throw new Exception("Crash!!!")
        } ~
        path("shutdown") {
          binding.unbind(materializedMap).onComplete(_ => system.shutdown())
          complete("Shuting down...")
        }
      }
    } ~
    pathPrefix("random") {
      path("int") {
        get {
          complete(Random.nextInt().toString)
        }
      } ~
      path("string" / IntNumber) { length =>
        get {
          complete(Random.alphanumeric.take(length).mkString)
        }
      } ~
      path("bytes" / IntNumber) { length =>
        get {
          complete(Rnd.getBytes(length))
        }
      } ~
      path("bytes_as_base64" / IntNumber) { length =>
        get {
          complete(Rnd.getBytesAsBase64(length))
        }
      }
    }
  }
}

object Rnd {
  def getBytes(length: Int): Array[Byte] = {
    val bytes = new Array[Byte](length)
    Random.nextBytes(bytes)
    bytes
  }

  def getBytesAsBase64(length: Int) =
    Base64.getEncoder.encodeToString(getBytes(length))
}
