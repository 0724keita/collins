package collins
package action

import util.config.{ConfigAccessor, ConfigSource, ConfigValue, TypesafeConfiguration}
import com.typesafe.config.ConfigValueType


case class ActionConfigException(component: String)
  extends Exception("Didn't find %s in configuration for action"
      .format(component))


case class ActionConfig(override val source: TypesafeConfiguration)
  extends ConfigAccessor with ConfigSource {

  def actionType = ActionType(getString("type").getOrElse{"exec"})
  def command = getCommand()

  def validateConfig() {
    command
  }

  /**
   * Gets an action.command as a sequence of strings, detecting whether the
   * command was specified as a String or as a List.
   *
   * @return a Set of Strings, comprising the Action's command
   */
  protected def getCommand(): Seq[String] = {
    val cmd = getConfigValue("command") match {
      case None =>
        throw ActionConfigException("command")
      case Some(v) => v.valueType match {
        case ConfigValueType.LIST =>
          getStringList("command")
        case o =>
          Seq(getString("command")(ConfigValue.Required).get)
      }
    }
    val filtered = cmd.filter(_.nonEmpty)
    if (filtered.isEmpty) {
      throw ActionConfigException("command")
    }
    filtered
  }

}
