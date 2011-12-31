package controllers

import models.{User, UserImpl}
import play.api.mvc._
import play.api.http.HeaderNames
import play.api.templates.Txt

trait SpecHelpers {
  case class MockRequest(
    uri: String = "http://tumblr.com/api/fizz?foo=bar",
    queryString: Map[String,Seq[String]] = Map.empty,
    body: AnyContent = AnyContentAsEmpty,
    path: String = "/api/fizz",
    headers: Seq[String] = Seq.empty,
    method: String = "GET"
  )

  def getApi(user: Option[User]) = new Api with SecureController {
    override def authenticate(request: RequestHeader) = user
    override def getUser(request: RequestHeader) = user.get
    override def onUnauthorized = Action { req =>
      Results.Unauthorized(Txt("Invalid username/password specified"))
    }
  }
  def getLoggedInUser(group: String) = Some(UserImpl("test", "*", Seq(group), 123, true))

  def getRequestHeader(req: MockRequest): RequestHeader = new RequestHeader {
    def uri = req.uri
    def method = req.method
    def queryString = req.queryString
    def username = Some("testuser")
    def path = req.path
    def headers = new Headers {
      def getAll(key: String) = req.headers
      def keys: Set[String] = Set(HeaderNames.ACCEPT)
    }
    def cookies = new Cookies {
      def get(name: String) = Some(Cookie(name="foo",value="yay"))
    }
  }

  def getRequest(req: MockRequest): Request[AnyContent] = new Request[AnyContent] {
    def uri = req.uri
    def method = req.method
    def queryString = req.queryString
    def body = req.body
    def username = Some("testuser")
    def path = req.path
    def headers = new Headers {
      def getAll(key: String) = req.headers
      def keys: Set[String] = Set(HeaderNames.ACCEPT)
    }
    def cookies = new Cookies {
      def get(name: String) = Some(Cookie(name="foo",value="yay"))
    }
  }

  def getResource(filename: String) = {
    val stream = getClass.getClassLoader.getResourceAsStream(filename)
    val tmp = io.Source.fromInputStream(stream)
    val str = tmp.mkString
    tmp.close()
    str
  }


}
