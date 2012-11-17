package controllers

import play.api._
import play.api.mvc._
import play.api.libs.concurrent.Execution.Implicits._

object Application extends Controller {
  
  def index = Action {

    import scala.util.Random
    import play.api.libs.iteratee._
    import play.api.libs.concurrent._

    val promises = Range(1,1000).map(i => Promise.timeout(i,Random.nextInt(500)))

    val superS = Enumerator.interleave(promises.map(p => p.map( Enumerator(_))).map(Enumerator.flatten))

    Ok.stream((superS.map(_.toString) &> play.api.libs.Comet("console.log")) >>> Enumerator.eof)
  }
  
}

import play.api.libs.ws._
import play.api.libs.iteratee._
import play.api.libs.concurrent._

object Application1 extends Controller {
	
  val EXPECTED_LENGTH = 15;
	
  def index = Action {
    val p1 = WS.url("http://linkedin.com").get().map{ _ => "linkedin" }
    val p2 = WS.url("http://zenexity.com").get().map{ _ => "zenexity" }
    val p3 = WS.url("http://news.ycombinator.com").get().map{ _ => "ycombinator" }
    val enumerators = List(p1,p2,p3).map(p => Enumerator.flatten(p.map( r => Enumerator(r:_*))))
    val r = 
      Enumerator.interleave(enumerators) &>
      Enumeratee.take(EXPECTED_LENGTH) |>>
      Iteratee.fold(List[Char]()){ (es, e: Char) => println(e); e :: es }

      val p = r.flatMap(_.run).map( cs => Ok(cs.reverse.mkString))

      AsyncResult(p)
  }
}
