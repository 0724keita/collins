package models

import org.specs2.mutable._

class AssetSpec extends DatabaseSpec {

  "Assets" should {

    "support findByMeta" in {
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

    "support getAttributes" in {
      val _asset = Asset.findById(1)
      _asset must beSome[Asset]
      val asset = _asset.get
      val attribs = asset.getAttributes(Set(AssetMeta.Enum.ChassisTag))
      attribs must haveSize(1)
      val attrib = attribs.head
      attrib.getValue mustEqual("chassis tag abc")
      attrib.getNameEnum must beSome(AssetMeta.Enum.ChassisTag)
    }

    "support findById" in {
      val asset = Asset.findById(1)
      asset must beSome[Asset]
      val _asset = asset.get
      _asset.secondary_id mustEqual "tumblrtag1"
      _asset.getStatus.name mustEqual "New"
      _asset.getType.name mustEqual "Server Node"
      val attribs = _asset.getAttributes(Set(
        AssetMeta.Enum.ServiceTag,
        AssetMeta.Enum.ChassisTag,
        AssetMeta.Enum.IpmiAddress,
        AssetMeta.Enum.Hostname
        ))
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
    } // support findById in

  } // Assets should

}
