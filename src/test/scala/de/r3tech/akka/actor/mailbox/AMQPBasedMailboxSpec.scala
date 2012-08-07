package de.r3tech.akka.actor.mailbox

import akka.actor.mailbox.DurableMailboxSpec

object AMQPBasedMailboxSpec {
  val config = """
    AMQP-dispatcher {
      mailbox-type = de.r3tech.akka.actor.mailbox.AMQPBasedMailboxType
      throughput = 1
    }
    """
}

@org.junit.runner.RunWith(classOf[org.scalatest.junit.JUnitRunner])
class AMQPBasedMailboxSpec extends DurableMailboxSpec("AMQP", AMQPBasedMailboxSpec.config) {
  val settings = new AMQPBasedMailboxSettings(system, system.settings.config.getConfig("AMQP-dispatcher"))
}
