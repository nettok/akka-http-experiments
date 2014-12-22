// akka-http: json API

import akka.actor.ActorSystem
import akka.http.Http
import akka.http.server.Directives._
import akka.http.server.Route
import akka.stream.FlowMaterializer
import akka.stream.scaladsl.MaterializedMap
import spray.json.DefaultJsonProtocol

case class State(value: Int)

object JsonProtocol extends DefaultJsonProtocol {
  implicit val stateFormat = jsonFormat1(State)
}

object Server2 extends App with DefaultJsonProtocol {
  implicit val system = ActorSystem()
  implicit val materializer = FlowMaterializer()
  implicit val executionContext = system.dispatcher

  import akka.http.marshallers.sprayjson.SprayJsonSupport._
  import JsonProtocol._

  val binding = Http().bind("localhost", 8080)

  val materializedMap: MaterializedMap = binding startHandlingWith Route.handlerFlow {
    path("") {
      get {
        complete("http-exp0 Server2")
      }
    } ~
    path("state") {
      get {
        complete(State(0))
      }
    }
  }
}
