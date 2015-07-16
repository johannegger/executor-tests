package controllers
import play.api._
import play.api.mvc._
import play.api.libs.concurrent.Execution.Implicits.defaultContext
import scala.concurrent.duration._

class Application extends Controller{

  def indexThrottled = Action.async{
    play.api.libs.concurrent.Promise.timeout("Oops", 10.seconds).map{_ =>
      Ok(views.html.index("Your new application is ready."))
    }
  }

  def index = Action{
    Ok(views.html.index("Your new application is ready."))
  }
}
