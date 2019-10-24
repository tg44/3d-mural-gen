package xyz.tg44.pipeline.server

import akka.http.scaladsl.model.{ContentTypes, HttpEntity, HttpResponse}
import akka.http.scaladsl.server.RejectionHandler

/**
 * mostly based on
 * https://doc.akka.io/docs/akka-http/current/routing-dsl/rejections.html#customising-rejection-http-responses
 */
object JsonBasedRejectionHandler {

  implicit def jsonRejectionHandler: RejectionHandler =
    RejectionHandler.default
      .mapRejectionResponse {
        case res @ HttpResponse(_, _, ent: HttpEntity.Strict, _) =>
          // since all Akka default rejection responses are Strict this will handle all rejections
          val message = ent.data.utf8String.replaceAll("\"", """\"""")

          // we copy the response in order to keep all headers and status code, wrapping the message as hand rolled JSON
          // you could the entity using your favourite marshalling library (e.g. spray json or anything else)
          res.copy(entity = HttpEntity(ContentTypes.`application/json`, s"""{"error": "$message"}"""))

        case x => x // pass through all other types of responses
      }
}
