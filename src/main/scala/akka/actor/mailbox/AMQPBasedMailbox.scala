/**
 *  Copyright (C) 2009-2012 Typesafe Inc. <http://www.typesafe.com>
 */
package akka.actor.mailbox

import java.util.concurrent.LinkedBlockingQueue

import com.rabbitmq.client.{
  Channel,
  GetResponse,
  MessageProperties,
  QueueingConsumer
}

import akka.AkkaException
import akka.actor.{
  ActorContext,
  ActorRef,
  DeadLetter
}
import akka.event.Logging
import akka.dispatch.{
  Envelope,
  MailboxType,
  MessageQueue
}
import akka.actor.ActorSystem.Settings
import com.typesafe.config.Config

class AMQPBasedMailboxException(message: String) extends AkkaException(message)

class AMQPBasedMailboxType(settings: Settings, config: Config) extends MailboxType {
  override def create(owner: Option[ActorContext]) = owner match {
    case Some(owner) ⇒ new AMQPBasedMailbox(owner, config)
    case None        ⇒ throw new AMQPBasedMailboxException("AMQPBasedMailbox needs an owner to work properly.")
  }
}

class AMQPBasedMailbox(owner: ActorContext, val config: Config) extends DurableMessageQueue(owner) with DurableMessageSerialization {

  private val settings = new AMQPBasedMailboxSettings(owner.system, config)
  private val pool = settings.ChannelPool
  private val log = Logging(system, "AMQPBasedMailbox")
  private val consumerQueue = new LinkedBlockingQueue[QueueingConsumer.Delivery]
  private var consumer = withErrorHandling {
    createConsumer()
  }

  withErrorHandling {
    pool.withChannel { _.queueDeclare(name, true, false, false, null) }
  }

  def enqueue(receiver: ActorRef, envelope: Envelope) {
    withErrorHandling {
      pool.withChannel { _.basicPublish("", name, MessageProperties.PERSISTENT_BASIC, serialize(envelope)) }
    }
  }

  def dequeue(): Envelope = withErrorHandling {
    val envelope = deserialize(consumer.nextDelivery.getBody)
    envelope
  }

  def cleanUp(owner: ActorContext, deadLetters: MessageQueue) {
    consumer.getChannel.close
    withErrorHandling {
      pool.withChannel { channel ⇒
        var message = channel.basicGet(name, true)
        while (message != null) {
          val envelope = deserialize(message.getBody)
          deadLetters.enqueue(owner.actorFor("/deadletters"), envelope)
          message = channel.basicGet(name, true)
        }
      }
      pool.close
    }
  }

  def numberOfMessages: Int = withErrorHandling {
    pool.withChannel { _.queueDeclare(name, true, false, false, null).getMessageCount }
  }

  def hasMessages: Boolean = !consumerQueue.isEmpty

  private def withErrorHandling[T](body: ⇒ T): T = {
    try {
      body
    } catch {
      case e: java.io.IOException ⇒ {
        log.error("Communication with AMQP server failed, retrying operation.", e)
        try {
          pool.pool.returnObject(consumer.getChannel)
          consumer = createConsumer
          body
        } catch {
          case e: java.io.IOException ⇒
            throw new AMQPBasedMailboxException("AMQP server seems to be offline.")
        }
      }
    }
  }

  private def createConsumer() = {
    val channel = pool.pool.borrowObject.asInstanceOf[Channel]
    val consumer = new QueueingConsumer(channel, consumerQueue)
    channel.queueDeclare(name, true, false, false, null)
    channel.basicConsume(name, true, consumer)
    consumer
  }
}
