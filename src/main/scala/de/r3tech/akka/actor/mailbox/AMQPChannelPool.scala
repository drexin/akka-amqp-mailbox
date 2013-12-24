/**
 *  Copyright (C) 2012 Dario Rexin
 */
package de.r3tech.akka.actor.mailbox

import akka.AkkaException
import akka.actor.ActorSystem
import akka.event.LoggingAdapter
import org.apache.commons.pool._
import org.apache.commons.pool.impl._
import com.rabbitmq.client.ConnectionFactory
import com.rabbitmq.client.Channel
import java.io.IOException

class AMQPConnectException(message: String) extends AkkaException(message)

private[akka] class AMQPChannelFactory(factory: ConnectionFactory, log: LoggingAdapter) extends PoolableObjectFactory[Channel] {

  private var connection = createConnection

  def makeObject(): Channel = {
    try {
      createChannel
    } catch {
      case e: IOException ⇒ {
        log.error("Could not create a channel. Will retry after reconnecting to AMQP Server.", e)
        connection.close()
        connection = createConnection
        createChannel
      }
    }
  }

  private def createConnection = {
    try {
      factory.newConnection
    } catch {
      case e: IOException ⇒
        throw new AMQPConnectException("Could not connect to AMQP broker: " + e.getMessage)
    }
  }

  private def createChannel = {
    val channel = connection.createChannel
    channel.basicQos(100)
    channel
  }

  def destroyObject(channel: Channel): Unit = {
    channel.asInstanceOf[Channel].close
  }

  def passivateObject(channel: Channel): Unit = {}
  def validateObject(channel: Channel) = {
    try {
      channel.asInstanceOf[Channel].basicQos(1)
      true
    } catch {
      case _: Exception ⇒ false
    }
  }

  def activateObject(channel: Channel): Unit = {}
}

class AMQPChannelPool(factory: ConnectionFactory, log: LoggingAdapter) {
  val pool = new StackObjectPool(new AMQPChannelFactory(factory, log))

  def withChannel[T](body: Channel ⇒ T) = {
    val channel = pool.borrowObject
    try {
      body(channel)
    } finally {
      pool.returnObject(channel)
    }
  }

  def close() = pool.close()
}

