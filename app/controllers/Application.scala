package controllers

import play.api._
import play.api.mvc._

object Application extends Controller {

  def index = Action {
    //Ok(views.html.index("Your new application is ready."))
    //Ok("Hello World")
    //Ok("Hello World)
    Redirect(routes.Application.discs)
  }

  def discs = TODO

  def newDisc = TODO

  def deleteDisc(id: Long) = TODO

}
