package controllers

import play.api._
import play.api.mvc._
import play.api.data._
import play.api.data.Forms._
import models.Disc

object Application extends Controller {

  def index = Action {
    //Ok(views.html.index("Your new application is ready."))
    //Ok("Hello World")
    //Ok("Hello World)
    Redirect(routes.Application.discs)
  }

  def discs = Action {
  	 Ok(views.html.index(Disc.all(), discForm))
  }

  def newDisc = Action { implicit request =>
  		discForm.bindFromRequest.fold(
  			errors => BadRequest(views.html.index(Disc.all(), errors)),
  			label => {
  				Disc.create(label)
  				Redirect(routes.Application.discs)
  			}
  		)
  	}

  def deleteDisc(id: Long) = TODO

  val discForm = Form(
  		"label" -> nonEmptyText
  	)

}
