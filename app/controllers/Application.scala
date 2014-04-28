package controllers

import play.api._
import play.api.mvc._
import play.api.data._
import play.api.data.Forms._
import play.api.libs.json
import play.api.libs.json._
import play.api.libs.functional.syntax._
import models.Disc

object Application extends Controller {

  implicit val discWrites: Writes[Disc] = (
    (JsPath \ "id").write[Long] and
    (JsPath \ "label").write[String]
  )(unlift(Disc.unapply))

  def index = Action {
    //Ok(views.html.index("Your new application is ready."))
    //Ok("Hello World")
    //Ok("Hello World)
    Redirect(routes.Application.discs(""))
  }

  def discs(filter: String) = Action { implicit request =>
  	 // Ok(views.html.index(Disc.all(), discForm, ""))
  	 println("angekommen " + filter)
     val discList = Disc.list(filter = ("%"+filter+"%"))
     println("gefiltert " + discList)
  	 Ok(views.html.index(Disc.list(filter = ("%"+filter+"%")), discForm, filter))
  }

  def jsonDiscs(filter: String) = Action { implicit request =>
  	val filteredDiscs = Disc.list(filter = ("%"+filter+"%"))
  	val json = Json.toJson(filteredDiscs)
  	println(json)
  	// Redirect(routes.Application.discs(filter))
  	Ok(json)
  }

  def newDisc = Action { implicit request =>
  		discForm.bindFromRequest.fold(
  			errors => BadRequest(views.html.index(Disc.all(), errors, "")),
  			label => {
  				Disc.create(label)
  				Redirect(routes.Application.discs(""))
  			}
  		)
  	}

  def deleteDisc(id: Long) = Action { implicit request =>
  	Disc.delete(id)
  	Redirect(routes.Application.discs(""))
  }

  val discForm = Form(
  		"label" -> nonEmptyText
  	)

}
