package controllers
package actors

import akka.util.Duration
import models.{AssetLifecycle, MetaWrapper, Model, Status => AStatus}
import play.api.mvc.{AnyContent, Request}
import util.SoftLayer

case class AssetCancelProcessor(tag: String, userTimeout: Option[Duration] = None)(implicit req: Request[AnyContent])
  extends BackgroundProcess[Either[ResponseData,Long]]
{
  override def defaultTimeout: Duration =
    Duration.parse("10 seconds")

  val timeout = userTimeout.getOrElse(defaultTimeout)
  def run(): Either[ResponseData,Long] = {
    req.body.asUrlFormEncoded.flatMap(_.get("reason")).flatMap(_.headOption).map(_.trim).filter(_.size > 0).map { _reason =>
      val reason = _reason.trim
      Api.withAssetFromTag(tag) { asset =>
        SoftLayer.pluginEnabled.map { plugin =>
          plugin.softLayerId(asset) match {
            case None =>
              Left(Api.getErrorMessage("Asset is not a softlayer asset"))
            case Some(n) =>
              plugin.cancelServer(n, reason)() match {
                case 0L =>
                  Left(Api.getErrorMessage("There was an error cancelling this server"))
                case ticketId =>
                  Model.withTransaction { implicit con =>
                    MetaWrapper.createMeta(asset, Map("CANCEL_TICKET" -> ticketId.toString))
                    AssetLifecycle.updateAssetStatus(asset, Map(
                      "status" -> AStatus.Enum.Cancelled.toString,
                      "reason" -> reason
                    ), con)
                  }
                  plugin.setNote(n, "Cancelled: %s".format(reason))()
                  Right(ticketId)
              }
          }
        }.getOrElse {
          Left(Api.getErrorMessage("SoftLayer plugin is not enabled"))
        }
      }
    }.getOrElse(Left(Api.getErrorMessage("No reason specified for cancellation")))
  }
}
