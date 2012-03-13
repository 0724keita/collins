package models

import util.Cache

import org.squeryl.PrimitiveTypeMode._
import org.squeryl.Schema

case class Status(name: String, description: String, id: Int = 0) extends ValidatedEntity[Int] {
  def getId(): Int = id
  override def validate() {
    require(name != null && name.length > 0, "Name must not be empty")
    require(description != null && description.length > 0, "Description must not be empty")
  }
}

object Status extends Schema with AnormAdapter[Status] { //Magic[Status](Some("status")) {

  override val tableDef = table[Status]("status")
  on(tableDef)(s => declare(
    s.id is (autoIncremented,primaryKey),
    s.name is(unique)
  ))

  override protected def cacheKeys(s: Status) = Seq(
    "Status.findById(%d)".format(s.id),
    "Status.findByName(%s)".format(s.name.toLowerCase)
  )

  def findById(id: Int): Option[Status] = getOrElseUpdate("Status.findById(%d)".format(id)) {
    tableDef.lookup(id)
  }

  def findByName(name: String): Option[Status] = {
    getOrElseUpdate("Status.findByName(%s)".format(name.toLowerCase)) {
      tableDef.where(s =>
        s.name.toLowerCase === name.toLowerCase
      ).headOption
    }
  }

  override def delete(s: Status): Int = inTransaction {
    tableDef.deleteWhere(p => p.id === s.id)
  }

  type Enum = Enum.Value
  object Enum extends Enumeration(1) {
    val New, Unallocated, Allocated, Cancelled, Maintenance, Decommissioned, Incomplete, Provisioning, Provisioned = Value
  }

}
