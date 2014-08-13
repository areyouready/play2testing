package controllers

import play.api._
import play.api.libs.ws.Response
import play.api.mvc._
import play.api.data._
import play.api.data.Forms._
import play.api.libs.json._
import play.api.libs.functional.syntax._
import play.api.libs.concurrent.Execution.Implicits._
import models.Disc
import play.api.libs.iteratee.{Iteratee, Enumeratee, Enumerator}

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

  //TODO rename to list
  def discs(filter: String, nxtFilter: Option[String], prevFilter: Option[String], page: Option[Int]) = Action.async {
    implicit request =>

      val desc = prevFilter match {
        case None => false
        case _ => true
      }

      val discList: Future[Response] = Disc.couchList(filter, nxtFilter, prevFilter, desc)
      discList.map { resp =>
        val json = resp.json
        //      val jsonDiscList = scalaConvertedResult.map { m => Map( "id" -> m("id"), "title" -> m("value") )}
        //      val resultList = scalaConvertedResult.map { m => Disc.apply(m("id"), m("value"), m("key"))}

        val totalRows = (json \ "total_rows").as[Int]
        val scalaConvertedResult = json.\("rows").\\("value").map(_.as[Map[String, String]])
        // is needed because at going back in paging the list comes in reversed order
        val resultList = if (desc == false) scalaConvertedResult.map { m => Disc.apply(m("_id"), m("_rev"), m("title"))}.
          toList else scalaConvertedResult.map { m => Disc.apply(m("_id"), m("_rev"), m("title"))}.toList.reverse
        val nxtStart = if (resultList.length > 10) resultList.last.title else ""
        val lastStart = resultList.head.title
        val responsePage = page match {
          case None => 0
          case _    => page.get
        }

        Ok(views.html.index(if (resultList.length > 10) resultList.take(resultList.length - 1) else resultList,
          discForm, filter, nxtStart, lastStart, responsePage))
//        Ok(views.html.index(resultList, discForm, filter, ""))
      }

  }

//  def jsonDiscs(filter: String) = Action { implicit request =>
//  	val filteredDiscs = Disc.list(filter = ("%"+filter+"%"))
//    val filteredCouchDiscs = Disc.couchList(filter = (filter))
//  	val json = Json.toJson(filteredCouchDiscs)
//  	println(json)
//  	Ok(json)
//  }

  //TODO rename to jsonDiscs/jsonList to avoid confusion with normal discs
  def couchDiscs(filter: String) = Action.async { implicit request =>
    val filteredCouchDiscs: Future[Response] = Disc.couchList(filter, None, None, false)
    filteredCouchDiscs.map  { resp =>
      val json = resp.json
//      val scalaConvertedResult2 = (json.\("rows")).as[List[Map[String, String]]]
//      val filteredJson2 = Json.toJson(scalaConvertedResult2.map { m => Map( "id" -> m("id"), "title" -> m("value") )})
//      println(json.\("rows").\\("value"))
//      println("Test" + scalaConvertedResult)
      val totalRows = (json \ "total_rows").as[Int]
      val scalaConvertedResult = json.\("rows").\\("value").map(_.as[Map[String, String]])
//      val reducedResult = scalaConvertedResult.take(scalaConvertedResult.length - 1)

      val filteredJson = Json.toJson(scalaConvertedResult.map { m => Disc.apply(m("_id"), m("_rev"), m("title"))})
      val resultJson = Json.obj("totalRows" -> totalRows.toString(), "discs" -> filteredJson)

//      val lastElement = (
//        if (reducedResult.length > 10) scalaConvertedResult.last
//        else null
//      )

      Ok(resultJson)
    }
  }

  def newDisc = Action.async { implicit request =>
    discForm.bindFromRequest.fold(
      errors => Future.successful(BadRequest(views.html.index(Disc.all(), errors, "", "", "", 0))),
      title => {

        val futureResponse: Future[Response] = Disc.couchCreate(title)
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
//  def deleteDisc(id: String) = Action { implicit request =>
//  	Disc.couchDelete(id)
//  	Redirect(routes.Application.discs(""))
//  }

  val discForm = Form(
  		"title" -> nonEmptyText)
//TODO add mapping (validation) when automatic fetching of id for new disc is implemented or use "id" -> ignored(0L)
//  mapping(
//    (Disc.apply)(Disc.unapply))

}
