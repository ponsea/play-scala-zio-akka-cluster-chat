package interfaces.controllers

import play.api.mvc._

class HomeController(cc: ControllerComponents) extends AbstractController(cc) {

  def index = Action {
    Ok(views.html.index())
  }
}
