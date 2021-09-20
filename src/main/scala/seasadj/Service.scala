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
  import org.cmhh.seasadj._
  
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

  object routes {
    val version = path("version") {
      complete(HttpEntity(ContentTypes.`application/json`, "[\"0.2.2\"]"))
    }

    val validateSPC = path("validateSPC") {
      entity(as[String]){ entity =>
        complete(HttpEntity(
          ContentTypes.`application/json`,
          {
            Specification.fromString("", entity) match {
              case Success(r) => """{"result":"passed"}"""
              case Failure(e) => s"""{"result":"failed", "error":"${e.getMessage()}"}"""
            }
          }
        ))
      }
    }

    val validateJSON = path("validateJSON") {
      entity(as[String]){ entity =>
        complete(HttpEntity(
          ContentTypes.`application/json`,
          {
            Specifications.fromJSONString(entity) match {
              case Success(r) => """{"result":"passed"}"""
              case Failure(e) => s"""{"result":"failed", "error":"${e.getMessage()}"}"""
            }
          }
        ))
      }
    }

    val toJSON = (path("toJSON" / Segment)) { (name) =>
      entity(as[String]){ entity =>
        Specification.fromString(name, entity) match {
          case Success(r) =>
            val res = HttpEntity(ContentTypes.`application/json`, r.toJSON)
            complete(res)
          case Failure(e) =>
            val res = HttpEntity(
              ContentTypes.`application/json`,
              s"""{"result":"failed", "error":"${e.getMessage()}"}"""
            )
            complete(StatusCodes.UnprocessableEntity -> res)
        }
      }
    }

    val toSPC = path("toSPC") {
      entity(as[String]){ entity =>
        Specifications.fromJSONString(entity) match {
          case Success(r) =>
            val res = HttpEntity(
              ContentTypes.`application/json`, 
              "{" + r.map(s => {s""""${s.name}":"${StringUtils.escape(s.toString)}""""}).mkString(",") + "}"
            )
            complete(res)
          case Failure(e) =>
            val res = HttpEntity(
              ContentTypes.`application/json`,
              s"""{"result":"failed", "error":"${e.getMessage()}"}"""
            )
            complete(StatusCodes.UnprocessableEntity -> res)
        }
      }
    }

    val adjust = path("adjust") {
      parameters(Symbol("save").?, Symbol("allDates").as[Boolean].?){ (save, allDates) =>
        val save_ = save match {
          case None => Vector[String]()
          case Some(x) => x.split(',').toVector
        }
        val allDates_ = allDates match {
          case None => true
          case Some(x) => x
        }
        entity(as[String]){ entity => 
          Specifications.fromJSONString(entity) match {
            case Success(x) => 
              Adjustor.adjust(x, save_) match {
                case Success(y) => 
                  val res = HttpEntity(
                    ContentTypes.`application/json`,
                    y.toJSON(allDates_)
                  )
                  encodeResponseWith(Gzip)(complete(res))
                case Failure(e) => 
                  val res = HttpEntity(
                    ContentTypes.`application/json`,
                    s"""{"result":"failed", "error":"${e.getMessage()}"}"""
                  )
                  complete(StatusCodes.InternalServerError -> res)
              }
            case Failure(e) => 
              val res = HttpEntity(
                ContentTypes.`application/json`,
                s"""{"result":"failed", "error":"${e.getMessage()}"}"""
              )
              complete(StatusCodes.UnprocessableEntity -> res)           
          }
        }
      }
    }
  }

  val route = 
    pathPrefix("seasadj") { 
      corsHandler(
        post { 
          routes.adjust ~ 
          pathPrefix("spec")(
            routes.validateJSON ~ routes.validateSPC ~ routes.toJSON ~ routes.toSPC
          )
        } ~
        get {
          routes.version
        })
    }

  val bindingFuture = Http().bindAndHandle(route, "0.0.0.0", 9001)

  println(s"Server online at http://localhost:9001/seasadj\nPress ENTER to stop...")
  StdIn.readLine() 
  bindingFuture
    .flatMap(_.unbind()) 
    .onComplete(_ => system.terminate()) 
}