package utils

import play.api.GlobalSettings
import play.api.libs.ws.WS
import play.api.libs.ws.WS.WSRequestHolder

/**
 * Created by sebastianbasner on 23.07.14.
 */
object Global extends GlobalSettings {
  lazy val COUCHDB_HOST: WSRequestHolder = WS.url(play.Play.application.configuration.getString("couch.url"))
//  lazy val COUCHDB_LASER_HOST: WSRequestHolder = WS.url(play.Play.application.configuration.getString("couch.base.url"))

  def getRequestHolderByHost(path: String): WSRequestHolder = {
    val COUCHDB_LASER_HOST: WSRequestHolder = WS.url(play.Play.application.configuration.getString("couch.base.url") + path)
    COUCHDB_LASER_HOST
  }


}
