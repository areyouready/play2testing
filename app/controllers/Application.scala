package controllers

import play.api._
import play.api.http.MediaRange
import play.api.libs.ws.Response
import play.api.mvc._
import play.api.data._
import play.api.data.Forms._
import play.api.libs.json._
import play.api.libs.functional.syntax._
import play.api.libs.concurrent.Execution.Implicits._
import models.Disc
import play.api.libs.iteratee.{Iteratee, Enumeratee, Enumerator}
import play.mvc.Http

import scala.collection.mutable
import scala.concurrent.Future

object Application extends Controller {

  implicit val discWrites: Writes[Disc] = (
    (JsPath \ "id").write[String] and
    (JsPath \ "rev").write[String] and
    (JsPath \ "title").write[String]
  )(unlift(Disc.unapply))

  def compDiscIgCase(e1: Disc, e2: Disc) = (e1.title compareToIgnoreCase e2.title) < 0

  def index = Action {
    Redirect(routes.Application.discs())
  }

//  def discs(filter: String) = Action { implicit request =>
//  	 // Ok(views.html.index(Disc.all(), discForm, ""))
//  	 println("angekommen " + filter)
//     val discList = Disc.list(filter = ("%"+filter+"%"))
//  	 // Ok(views.html.index(Disc.list(filter = ("%"+filter+"%")), discForm, filter))
//     // Ok(views.html.index(discList.sortBy(_.label), discForm, filter))
//     // Ok(views.html.index(discList, discForm, filter))
//     Ok(views.html.index(discList.sortWith(compDiscIgCase), discForm, filter))
//  }

  def discs(filter: String, nxtFilter: Option[String], prevFilter: Option[String], page: Option[Int]) = Action.async {
    implicit request =>

      val accept = request.acceptedTypes.exists(mt => mt.mediaType == "application" && mt.mediaSubType == "json") ||
        request.contentType.exists(mt => mt.contains("application/json"))

      if (accept) {
        val filteredCouchDiscs: Future[Response] = Disc.couchList(filter, None, None, false)
        filteredCouchDiscs.map  { resp =>
          val json = resp.json
          val filteredJson = Json.toJson(discsFromJson(json))
          val resultJson = Json.obj("totalRows" -> totalRowsFromJson(json).toString(), "discs" -> filteredJson)

          Ok(resultJson)
        }
      } else {

        val desc = prevFilter match {
          case None => false
          case _ => true
        }

        val discList: Future[Response] = Disc.couchList(filter, nxtFilter, prevFilter, desc)
        discList.map { resp =>
          val json = resp.json
          //      val resultList = scalaConvertedResult.map { m => Disc.apply(m("id"), m("value"), m("key"))}

          // is needed because at going back in paging the list comes in reversed order
          val resultList = if (desc == false) discsFromJson(json).toList else discsFromJson(json).toList.reverse
          val nxtStart = if (resultList.length > 10) resultList.last.title else ""
          val lastStart = resultList.head.title
          val responsePage = page match {
            case None => 0
            case _ => page.get
          }

          Ok(views.html.index(if (resultList.length > 10) resultList.take(resultList.length - 1) else resultList, totalRowsFromJson(json),
            discForm, filter, nxtStart, lastStart, responsePage, new Disc("", "", "")))
        }
      }
  }

  def newDisc = Action.async { implicit request =>
    discForm.bindFromRequest.fold(
      errors => Future.successful(BadRequest(views.html.index(Disc.all(), 0, errors, "", "", "", 0, new Disc("", "", "")))),
      title => {

        val futureResponse: Future[Response] = Disc.couchCreate(title.title)
        futureResponse.map ( resp =>
          resp.getAHCResponse.getStatusCode match {
            case 201 => {
              Redirect(routes.Application.discs("")).flashing(
                "success" -> "The item has been created")
            }
            case _ => {
              Redirect(routes.Application.discs("")).flashing(
                "error" -> "The item has not been created")
            }
          })
      }
    )
  }

  def couchDeleteDisc(id: String, rev: String) = Action.async { implicit request =>
    val futureResponse: Future[Response] = Disc.couchDelete(id, rev)
    futureResponse.map { resp =>
      resp.getAHCResponse.getStatusCode match {
        case 200 =>
          Redirect(routes.Application.discs("")).flashing(
            "success" -> "The item has been deleted")
        case _ =>
          Redirect(routes.Application.discs("")).flashing(
            "error" -> "The item has not been deleted")
      }
    }
  }

  def editDisc(id: String, rev: String, title: String) = Action { implicit request =>
    val editDisc: Disc = Disc.apply(id, rev, title)

//    val editDisc: Disc = edithelp(id,rev,title)
//    Ok(views.html.editDisc(id, rev, title, discForm.fill(title)))
    Ok(views.html.index(List[Disc](), 0, discForm.fill(editDisc), "", "", "", 0, editDisc))
  }

  def updateDisc() = Action.async { implicit request =>
//    val id = request.getQueryString("id").get
//    val title = request.getQueryString("title").get
//    val rev = request.getQueryString("rev").get
    discForm.bindFromRequest().fold(
      formWithErrors => Future.successful(BadRequest(views.html.index(Disc.all(), 0, formWithErrors, "", "", "", 0, new Disc
      ("", "",
        "")
      ))),
      values => {
        val futureResponse: Future[Response] = Disc.update(values.id, values.rev, values.title)
        futureResponse.map(resp =>
          resp.getAHCResponse.getStatusCode match {
            case 201 =>
              Redirect(routes.Application.discs("")).flashing(
                "success" -> "The item has been updated")
            case _ =>
              Redirect(routes.Application.discs("")).flashing(
                "error" -> "The item could not be updated")
          })
      }
    )
  }

  val totalRowsFromJson = (json: JsValue) => (json \ "total_rows").as[Int]

  val discsFromJson = (json: JsValue) => {
    val scalaMap = (json.\("rows").\\("value").map(_.as[Map[String, String]]))
    scalaMap.map { m => Disc.apply(m("_id"), m("_rev"), m("title"))}
  }

  val discForm = Form(
    mapping(
      "id" -> text,
      "rev" -> text,
      "title" -> nonEmptyText)
      (Disc.apply)(Disc.unapply))

}
