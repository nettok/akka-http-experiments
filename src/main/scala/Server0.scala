// akka-http-core: request handling

import akka.actor.ActorSystem
import akka.http.Http
import akka.http.model.{MediaTypes, HttpEntity, HttpRequest, HttpResponse, Uri}
import akka.http.model.HttpMethods._
import akka.stream.scaladsl.Flow
import akka.stream.FlowMaterializer

object Server0 extends App {
  implicit val system = ActorSystem()
  implicit val materializer = FlowMaterializer()

  val serverBinding = Http(system).bind(interface = "localhost", port = 8080)

  for (connection <- serverBinding.connections) {
    println("Accepted new connection from " + connection.remoteAddress)
    connection handleWith { Flow[HttpRequest] map requestHandler }
  }

  val requestHandler: HttpRequest ⇒ HttpResponse = {
    case HttpRequest(GET, Uri.Path("/"), _, _, _) ⇒
      HttpResponse(
        entity = HttpEntity(MediaTypes.`text/html`,
          "<html><body>Hello world!</body></html>"))

    case HttpRequest(GET, Uri.Path("/ping"), _, _, _) ⇒ HttpResponse(entity = "PONG!")
    case HttpRequest(GET, Uri.Path("/crash"), _, _, _) ⇒ sys.error("BOOM!")
    case _: HttpRequest ⇒ HttpResponse(404, entity = "Unknown resource!")
  }
}