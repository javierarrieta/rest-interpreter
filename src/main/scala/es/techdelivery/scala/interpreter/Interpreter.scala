package es.techdelivery.scala.interpreter

import akka.actor.{ActorRef, Actor, Props, ActorSystem}
import akka.io.IO
import spray.can.Http
import scala.tools.nsc.interpreter.IMain
import scala.tools.nsc.Settings
import scala.util.Try
import scala.concurrent.{Await, ExecutionContext}
import spray.routing.{Directives, HttpService}
import spray.http.{StatusCodes, HttpResponse}


case class Code(code: String, ret: String)

class InterpreterActor extends Actor {

  var imain: IMain = _

  override def preStart() {
    val settings = new Settings
    settings.usejavacp.value = true
    settings.deprecation.value = true
    settings.embeddedDefaults[InterpreterActor]
    imain = new IMain(settings)
  }

  def receive: Receive = {
    case Code(code, ret) => sender ! Try {
      imain.beQuietDuring {
        imain.interpret(code)
      }
      val value = imain.valueOfTerm(ret).get
      imain.reset()
      value
    }
  }
}


class InterpreterService(interpreter: ActorRef)(implicit executionContext: ExecutionContext) extends Actor with HttpService with Directives {

  implicit def actorRefFactory = context

  def receive: Receive = runRoute(InterpreterService.route(interpreter))

}

object InterpreterService extends Directives {

  def route(interpreter: ActorRef) = path("interpreter")(
    post(entity(as[String]) {
      body =>
        complete {
          import akka.pattern.ask
          import akka.util.Timeout

          import scala.concurrent.duration._

          implicit val timeout = Timeout(30 seconds)
          val f = interpreter ? Code(body, "ret")
          val v = Await.result(f, timeout.duration).asInstanceOf[Try[AnyRef]]
          val ret = if (v.isSuccess) {
            println(v.get.getClass.getCanonicalName + " = " + v.get)
            HttpResponse(status = StatusCodes.OK, entity = v.get.toString)
          }
          else
            HttpResponse(status = StatusCodes.BadRequest)
          println(ret)
          ret
        }
    }))

}

object Interpreter extends App {

  implicit lazy val system = ActorSystem("akka-spray")

  val interpreter = system.actorOf(Props(classOf[InterpreterActor]), "InterpreterActor")

  sys.addShutdownHook(system.shutdown())
  private implicit val _ = system.dispatcher

  val rootService = system.actorOf(Props(new InterpreterService(interpreter)))
  IO(Http)(system) ! Http.Bind(rootService, "0.0.0.0", port = 8080)
}
