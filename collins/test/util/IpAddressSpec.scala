package util

import org.specs2.mutable._
import org.specs2.matcher.DataTables

class IpAddressSpec extends Specification with DataTables {

  "IpAddress conversions" should {

    "support converting string addresses to long" >> {
      "address"         || "long"      |
      "170.112.108.147" !! 2859494547L |
      "10.0.0.1"        !! 167772161L  |
      "255.255.224.0"   !! 4294959104L |
      "255.255.255.255" !! 4294967295L |> {
      (address,long) =>
        IpAddress.toLong(address) mustEqual(long)
      }
    }

    "support converting long addresses to strings" >> {
      "long"      | "address"         |
      2859494547L ! "170.112.108.147" |
      167772161L  ! "10.0.0.1"        |
      4294959104L ! "255.255.224.0"   |
      4294967295L ! "255.255.255.255" |> {
      (long,address) =>
        IpAddress.toString(long) mustEqual(address)
      }
    }

  } // IpAddress conversions should

}
