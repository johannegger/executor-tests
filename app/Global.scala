import play.api.libs.ws.WS
import play.api.{Play, Configuration, Application, GlobalSettings}
import play.api.libs.concurrent.Execution.Implicits.defaultContext

import scala.concurrent.Future

object Global extends GlobalSettings{

  override def onStart(app: Application) = {
    Play.configuration(app).getBoolean("conf.server").collect{ case true =>
      println("starting server")
      t()(app)
    }.getOrElse{ println("no server") }
    super[GlobalSettings].onStart(app)
  }

  def t(times:Int=2,counter:Int=0)(implicit application: Application):Future[Long] = {
    println(s"will perform $times requests")
    val reqs = (1 to times).map{ case index =>
      WS.url("http://localhost:9000/indexThrottled").get().map{
        _.body.size
      }
    }

    Future.sequence(reqs).flatMap{ case done =>
      val sum = done.sum
      println(s"$times requests performed $sum")
      t(times * 2).map{ case res => res +  sum}
    }
  }

}
