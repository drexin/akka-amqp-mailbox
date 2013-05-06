/**
 *  Copyright (C) 2012 Dario Rexin
 */
package de.r3tech.akka.actor.mailbox

import akka.actor._
import akka.event.Logging
import com.rabbitmq.client.ConnectionFactory
import com.typesafe.config.Config

class AMQPBasedMailboxSettings(val system: ActorSystem, val userConfig: Config) {

  private val config = userConfig.withFallback(system.settings.config.getConfig("akka.actor.mailbox.amqp"))
  private val log = Logging(system, "AMQPBasedMailbox")

  import config._

  private val factory = new ConnectionFactory

  hasPath("uri") match {
    case true ⇒ factory.setUri(getString("uri"))
    case false ⇒ {
      val ConnectionTimeout = getMilliseconds("connectionTimeout")
      val Hostname = getString("hostname")
      val Password = getString("password")
      val Port = getInt("port")
      val User = getString("user")
      val VirtualHost = getString("virtualHost")

      factory.setUsername(User)
      factory.setPassword(Password)
      factory.setVirtualHost(VirtualHost)
      factory.setHost(Hostname)
      factory.setPort(Port)
      factory.setConnectionTimeout(ConnectionTimeout.toInt)
    }
  }

  val ChannelPool = new AMQPChannelPool(factory, log)
}
