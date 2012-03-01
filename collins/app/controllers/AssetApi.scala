package controllers

import models.{Status => AStatus}
import models._
import util._
import views.html

import play.api.data._
import play.api.http.{Status => StatusValues}
import play.api.mvc._
import play.api.libs.json._

import java.util.Date

trait AssetApi {
  this: Api with SecureController =>

  private lazy val lshwConfig = Helpers.subAsMap("lshw")

  // GET /api/asset/:tag
  def getAsset(tag: String) = Authenticated { user => Action { implicit req =>
    val result = Api.withAssetFromTag(tag) { asset =>
      val exposeCredentials = hasRole(user.get, Seq("infra"))
      val allAttributes = asset.getAllAttributes.exposeCredentials(exposeCredentials)
      Right(ResponseData(Results.Ok, allAttributes.toJsonObject, attachment = Some(allAttributes)))
    }
    result match {
      case Left(err) =>
        if (OutputType.isHtml(req)) {
          Redirect(app.routes.Resources.index).flashing(
            "message" -> ("Could not find asset with tag " + tag)
          )
        } else {
          formatResponseData(err)
        }
      case Right(success) =>
        if (OutputType.isHtml(req)) {
          val attribs = success.attachment.get.asInstanceOf[Asset.AllAttributes]
          Results.Ok(html.asset.show(attribs))
        } else {
          formatResponseData(success)
        }
    }
  }}

  // GET /api/assets?params
  private val finder = new actions.FindAsset()
  def getAssets(page: Int, size: Int, sort: String) = SecureAction { implicit req =>
    val rd = finder(page, size, sort) match {
      case Left(err) => Api.getErrorMessage(err)
      case Right(success) => actions.FindAsset.formatResultAsRd(success)
    }
    formatResponseData(rd)
  }

  // PUT /api/asset/:tag
  private val assetCreator = new actions.CreateAsset()
  def createAsset(tag: String) = SecureAction { implicit req =>
    formatResponseData(assetCreator(tag))
  }(SecuritySpec(true, "infra"))

  // POST /api/asset/:tag
  def updateAsset(tag: String) = SecureAction { implicit req =>
    actions.UpdateAsset.get().execute(tag) match {
      case Left(l) => formatResponseData(l)
      case Right(s) => formatResponseData(Api.statusResponse(s))
    }
  }(SecuritySpec(true, "infra"))

  // DELETE /api/asset/attribute/:attribute/:tag
  def deleteAssetAttribute(tag: String, attribute: String) = SecureAction { implicit req =>
    Api.withAssetFromTag(tag) { asset =>
      AssetLifecycle.updateAssetAttributes(asset, Map(attribute -> ""))
      .left.map(err => Api.getErrorMessage("Error deleting asset attributes", Results.InternalServerError, Some(err)))
      .right.map(status => Api.statusResponse(status, Results.Status(StatusValues.ACCEPTED)))
    }.fold(l => l, r => r).map(s => formatResponseData(s))
  }(SecuritySpec(true, "infra"))

  // DELETE /api/asset/:tag
  def deleteAsset(tag: String) = SecureAction { implicit req =>
    val options = Form("reason" -> optional(text(1))).bindFromRequest.fold(
      err => None,
      reason => reason.map { r => Map("reason" -> r) }
    ).getOrElse(Map.empty)
    val result = Api.withAssetFromTag(tag) { asset =>
      AssetLifecycle.decommissionAsset(asset, options)
        .left.map { e =>
          val msg = "Illegal state transition: %s".format(e.getMessage)
          Api.getErrorMessage(msg, Results.Status(StatusValues.CONFLICT))
        }
        .right.map(s => ResponseData(Results.Ok, JsObject(Seq("SUCCESS" -> JsBoolean(s)))))
    }
    val responseData = result.fold(l => l, r => r)
    formatResponseData(responseData)
  }(SecuritySpec(true, "infra"))


}
