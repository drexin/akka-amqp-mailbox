AMQP based durable mailbox for akka actors.

# License

This software is distributed under the Apache License 2.0.

# Usage

To make use of the durable amqp mailbox, you have to have an amqp broker
running and add the following to your akka.conf:

    akka {
      actor {
        mailbox {
          amqp {
            hostname = "127.0.0.1"
            port = 5672
            user = "guest"
            password = "guest"
            virtualHost = "/"
            # as an alternative to the single setings,
            # a uri can be provided, which wil be
            # prefered over the other settings
            # uri = "amqp://guest:guest@127.0.0.1:5672/"
            connectionTimeout = 1000ms
          }
        }
      }
    }

