package akka.actor.mailbox

import akka.dispatch.Mailbox

object AMQPBasedMailboxSpec {
  val config = """
    AMQP-dispatcher {
      mailbox-type = akka.actor.mailbox.AMQPBasedMailboxType
      throughput = 1
    }
    """
}

@org.junit.runner.RunWith(classOf[org.scalatest.junit.JUnitRunner])
class AMQPBasedMailboxSpec extends DurableMailboxSpec("AMQP", AMQPBasedMailboxSpec.config) {
  val settings = new AMQPBasedMailboxSettings(system, system.settings.config.getConfig("AMQP-dispatcher"))

  def isDurableMailbox(m: Mailbox): Boolean = {
    println(m.messageQueue)
    m.messageQueue.isInstanceOf[AMQPBasedMailbox]
  }
}
