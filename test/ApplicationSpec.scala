import models.remote.{Coursera, CourseraWSClient}
import org.specs2.mutable._
import org.specs2.runner._
import org.junit.runner._
import play.api.test._
import play.api.test.Helpers._

/**
 * Add your spec here.
 * You can mock out a whole application including requests, plugins etc.
 * For more information, consult the wiki.
 */
@RunWith(classOf[JUnitRunner])
class ApplicationSpec extends Specification {

  "Application" should {

    "get coursera json" in new WithApplication {
//
//      Coursera.getCategoryByShortName("cs-programming").map {
//        z =>
//          println(z)
//      }


      //      z.nonEmpty shouldEqual true

    }

  }
}
