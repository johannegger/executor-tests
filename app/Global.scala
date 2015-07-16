import org.joda.time
import org.joda.time.format.PeriodFormat
import play.api.libs.ws.WS
import play.api.{Play, Configuration, Application, GlobalSettings}
import play.api.libs.concurrent.Execution.Implicits.defaultContext
import scala.concurrent.Future
import scala.concurrent.duration._

object Global extends GlobalSettings{

  override def onStart(app: Application) = {
    implicit val appl = app
    Play.configuration.getInt("http.port").collect{
      case 9001 =>
        println("server")
        t(9000)
      case 9000 =>
        println("client")
        u(9001)
    }
    super[GlobalSettings].onStart(app)
  }

  def t(port:Int, times:Int=128)(implicit application: Application):Future[Long] = {
    println(s"will perform $times requests")
    val reqs = (1 to times).map{ case index =>
      WS.url(s"http://localhost:$port/indexThrottled").get().map{
        _.body.size
      }
    }

    Future.sequence(reqs).flatMap{ case done =>
      val sum = done.sum
      println(s"$times requests performed $sum")
      t(port,times * 2).map{ case res => res +  sum }
    }
  }

  def u(port:Int)(implicit application: Application): Unit = {
    val start = time.DateTime.now()
    WS.url(s"http://localhost:$port/index").get().onComplete{ case resp =>
      val duration = new time.Duration(start, time.DateTime.now())
      val s = PeriodFormat.getDefault().print(duration.toPeriod())
      println(s"request took $s")
      play.api.libs.concurrent.Promise.timeout("pause", 2.seconds).onComplete{ case pause =>
        u(port)
      }
    }
  }

}
