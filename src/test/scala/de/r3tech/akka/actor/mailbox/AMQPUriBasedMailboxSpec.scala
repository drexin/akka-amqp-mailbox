package de.r3tech.akka.actor.mailbox

import akka.actor.mailbox.DurableMailboxSpec

object AMQPUriBasedMailboxSpec {
  val config = """
    AMQP-dispatcher {
      mailbox-type = de.r3tech.akka.actor.mailbox.AMQPBasedMailboxType
      throughput = 100
      uri = "amqp://127.0.0.1"
    }
    """
}

@org.junit.runner.RunWith(classOf[org.scalatest.junit.JUnitRunner])
class AMQPUriBasedMailboxSpec extends DurableMailboxSpec("AMQP", AMQPUriBasedMailboxSpec.config) {
  val settings = new AMQPBasedMailboxSettings(system, system.settings.config.getConfig("AMQP-dispatcher"))
}
