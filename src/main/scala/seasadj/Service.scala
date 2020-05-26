package org.cmhh.seasadj

import akka.actor.ActorSystem
import akka.http.scaladsl.Http
import akka.http.scaladsl.model._
import akka.http.scaladsl.model.HttpMethods._
import akka.http.scaladsl.model.headers._
import akka.http.scaladsl.server.{ Route, Directive0 }
import akka.http.scaladsl.coding.Gzip
import akka.http.scaladsl.server.Directives._
import akka.http.scaladsl.server.ExceptionHandler
import akka.stream.ActorMaterializer
import scala.io.StdIn
import scala.util.{Failure, Success, Try}

/**
 * CORS handler... just in case.
 */
trait CORSHandler{
  private val corsResponseHeaders = List(
    headers.`Access-Control-Allow-Origin`.*,
    headers.`Access-Control-Allow-Credentials`(true),
    headers.`Access-Control-Allow-Headers`(
      "Authorization", "Content-Type", "X-Requested-With"
    )
  )
  
  private def addAccessControlHeaders: Directive0 = {
    respondWithHeaders(corsResponseHeaders)
  }
  
  private def preflightRequestHandler: Route = options {
    complete(HttpResponse(StatusCodes.OK).
      withHeaders(`Access-Control-Allow-Methods`(OPTIONS, POST, PUT, GET, DELETE)))
  }
  
  def corsHandler(r: Route): Route = addAccessControlHeaders {
    preflightRequestHandler ~ r
  }
  
  def addCORSHeaders(response: HttpResponse):HttpResponse =
    response.withHeaders(corsResponseHeaders)
}

object Service extends App with CORSHandler {
  implicit val system = ActorSystem("seasadj")
  implicit val executionContext = system.dispatcher

  final case class ImportFailedException(message: String, cause: Throwable) extends Exception(message, cause)
  final case class AdjustmentFailedException(message: String, cause: Throwable) extends Exception(message, cause)

  implicit def exceptionsHandler: ExceptionHandler = ExceptionHandler {
    case e: ImportFailedException =>
      complete(HttpResponse(StatusCodes.BadRequest, entity = e.message))
    case e: AdjustmentFailedException =>
      complete(HttpResponse(StatusCodes.BadRequest, entity = e.cause.getStackTrace.mkString("\n")))
  }

  val route = 
    pathPrefix("seasadj") { 
      post { 
        path("adjust") { corsHandler(
          parameters(Symbol("save").?){ save =>
            val save_ = save match {
              case None => Vector[String]()
              case Some(x) => x.split(',').toVector
            }
            entity(as[String]){ entity => 
              encodeResponseWith(Gzip){ complete(
                HttpEntity(
                  ContentTypes.`application/json`,
                  {
                    Specifications.fromJSONString(entity) match {
                      case Success(x) => {
                        Adjustor.adjust(x, save_) match {
                          case Success(y) => y.toJSON
                          case Failure(e) => throw AdjustmentFailedException("Adjustment failed.", e)
                        }
                      }
                      case Failure(e) => throw ImportFailedException("Invalid input.", e)
                    }
                  }
                )
              )}
            }
          }
        )}
      }
    }

  val bindingFuture = Http().bindAndHandle(route, "0.0.0.0", 9001)

  println(s"Server online at http://localhost:9001/seasadj\nPress RETURN to stop...")
  StdIn.readLine() 
  bindingFuture
    .flatMap(_.unbind()) 
    .onComplete(_ => system.terminate()) 
}