package models

import org.specs2.mutable._
import util.AssetStateMachine

class AssetSpec extends DatabaseSpec {

  args(sequential = true)

  "Asset" should {

    "CRUD" >> {
      val _asset = Asset("tumblrtag2", Status.Enum.Incomplete, AssetType.Enum.ServerNode)

      "CREATE" >> {
        val result = Model.withConnection { implicit con => Asset.create(_asset) }
        result.id.isDefined must beTrue
        result.getId must beGreaterThan(1L)
      }

      "UPDATE" >> {
        Model.withConnection { implicit con =>
          val asset = Asset.findBySecondaryId("tumblrtag2").get
          AssetStateMachine(asset).update().executeUpdate() mustEqual(1)
          val a2 = Asset.findBySecondaryId("tumblrtag2").get
          a2.getStatus().getId mustEqual(Status.Enum.New.id)
        }
      }

      "DELETE" >> {
        Model.withConnection { implicit con =>
          val tag2 = Asset.findBySecondaryId("tumblrtag2").get
          val query = Asset.delete("id={id}").on('id -> tag2.getId).executeUpdate() mustEqual 1
          Asset.findById(tag2.getId) must beNone
        }
      }
    }

    "Support getters/finders" >> {
      "findBySecondaryId" in {
        Asset.findBySecondaryId("tumblrtag1") must beSome
      }

      "findByMeta" in {
        val criteria = List(
          AssetMeta.Enum.IpmiAddress -> "10.0.0.1"
        )
        val assets = Asset.findByMeta(criteria)
        assets must haveSize(1)
        assets(0).getAttributes().foreach { attrib =>
          attrib.getNameEnum() match {
            case AssetMeta.Enum.IpmiAddress =>
              attrib.getValue() mustEqual "10.0.0.1"
            case _ =>
              // ignored
          }
        }
        success
      }

      "getAttributes" >> {
        "one" >> {
          val _asset = Asset.findById(1)
          _asset must beSome[Asset]
          val asset = _asset.get
          val attribs = asset.getAttributes(Set(AssetMeta.Enum.ChassisTag))
          attribs must haveSize(1)
          val attrib = attribs.head
          attrib.getValue mustEqual("chassis tag abc")
          attrib.getNameEnum must beSome(AssetMeta.Enum.ChassisTag)
        }

        "none" >> {
          val _asset = Asset.findById(1)
          _asset must beSome[Asset]
          val asset = _asset.get
          val attribs = asset.getAttributes()
          attribs.size must be_>=(3)
        }

        "many" in {
          val _asset = Asset.findById(1)
          _asset must beSome[Asset]
          val asset = _asset.get
          asset.secondary_id mustEqual "tumblrtag1"
          asset.getStatus.name mustEqual "New"
          asset.getType.name mustEqual "Server Node"
          val attribs = asset.getAttributes(Set(
            AssetMeta.Enum.ServiceTag,
            AssetMeta.Enum.ChassisTag,
            AssetMeta.Enum.IpmiAddress,
            AssetMeta.Enum.Hostname))
          attribs must not be empty
          attribs must haveSize(4)
          attribs.foreach { attrib =>
            val enum = attrib.getNameEnum()
            enum must beSome
            enum.get match {
              case AssetMeta.Enum.ServiceTag =>
                attrib.getValue mustEqual "asset tag 123"
              case AssetMeta.Enum.ChassisTag =>
                attrib.getValue mustEqual "chassis tag abc"
              case AssetMeta.Enum.IpmiAddress =>
                attrib.getValue mustEqual "10.0.0.1"
              case AssetMeta.Enum.Hostname =>
                attrib.getValue mustEqual "test.tumblr.test"
              case v =>
                failure("Unexpected value " + v)
            }
          }
          success
        } //many
      } // getAttributes
    } // support getters/finders
  } // Asset should
}
