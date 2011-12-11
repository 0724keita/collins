package controllers

import models._
import org.specs2.mutable._
import play.api.test.Extract

import org.specs2.mock._

class ApiSpec extends models.DatabaseSpec with SpecHelpers {

  val user = getLoggedInUser("infra")
  val api = getApi(user)

  "The API" should {
    "Support Basic Auth" >> {
      val request = getRequest(MockRequest(path = "/api/asset/foo.txt"))
      val result = api.asset("foo").apply(request)
      val extracted = Extract.from(result)
      extracted._1 must equalTo(200)
    }
  }

}
