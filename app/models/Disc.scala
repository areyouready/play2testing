package models

import java.io._

import anorm._
import anorm.SqlParser._

import play.api.db._
import play.api.Play.current
import play.api.http
import play.api.libs.iteratee.{Done, Input, Cont, Iteratee}
import play.api.libs.ws.Response
import play.api.libs.ws.{WS, Response}
import play.api.libs.json.{JsNull,Json,JsString,JsValue}
import play.api.libs.ws.WS.WSRequestHolder
import play.api.libs.concurrent.Execution.Implicits._
import utils.Global

import scala.concurrent.Future

case class Disc(id: String, rev: String, title: String)

object Disc {

  implicit val laserClient: WSRequestHolder = Global.getRequestHolderByHost("")

  val disc = {
		get[String]("id") ~
		get[String]("rev") ~
		get[String]("title") map {
			case id~title~rev => Disc(id, rev, title)
		}
	}

	def all(): List[Disc] = DB.withConnection { implicit c =>
		SQL("select * from disc").as(disc *)
	}


  //TODO extend with ids to avoid wrong paging through same key name
  def couchList(filter: String = "%", nxtStartFilter: Option[String], prevFilter: Option[String], desc: Boolean): Future[Response] = {
    val startFilter: String = nxtStartFilter match {
      case None => (prevFilter match {
                      case None => "\"" + filter + "\""
                      case _    => "\"" + prevFilter.get + "\""})
      case _    => "\"" + nxtStartFilter.get + "\""
    }

    val endFilter: String = prevFilter match {
      case None => "\""+ filter + "\\u9999" + "\""
      case _    => "\"" + filter + "\""
    }

    val laserRequest = Global.getRequestHolderByHost("/_design/discs/_view/titles").
      withHeaders("Content-Type" -> "application/json").
      withHeaders("Accept" -> "application/json").withQueryString("startkey" -> startFilter).
      withQueryString("endkey" -> endFilter).withQueryString("limit" -> "11").withQueryString("descending" -> desc.toString)

    laserRequest.get()
	}

  def couchCreate(title: String): Future[Response] = {
    val data = Json.obj(
      "title" -> title
    )
    laserClient.post(data)
  }

  def couchDelete(id: String, rev: String): Future[Response] = {
    val laserRequest: WSRequestHolder = Global.getRequestHolderByHost("/" + id).withQueryString("rev" -> rev)
    laserRequest.delete()
  }

  def update(id: String, rev: String, title: String): Future[Response] = {
    val data = Json.obj(
      "_rev" -> rev,
      "title" -> title
    )
    val laserRequest: WSRequestHolder = Global.getRequestHolderByHost("/" + id)
    laserRequest.put(data)
  }

//	def list(filter: String = "%"): List[Disc] = DB.withConnection { implicit c =>
//		SQL("""select * from disc
//			where disc.title like {filter}""").on(
//			'filter -> filter).as(disc *)
//	}

//	def create(title: String) {
//		DB.withConnection { implicit c =>
//			SQL("Insert into disc (title) values ({title})").on(
//				'title -> title
//			).executeUpdate()
//		}
//	}

//	def delete(id: Long) {
//		DB.withConnection { implicit c =>
//			SQL("delete from disc where id = {id}").on(
//				'id -> id
//			).executeUpdate()
//		}
//	}
}
