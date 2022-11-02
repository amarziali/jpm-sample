# Getting Started

The application spawns a websocket channel on each connection to `http://localhost:8080/ws` (You can use a chrome ws
client to test)

The basic flow is:

* Once the WS channel is established, an initial message is sent to the consumer.
* Each message sent on kafka topic `test.topic` is also sent to the ws channel.
* Each message sent to the application through the websocket channel is logged.

You can use `docker-compose up` to bootstrap a local zk/kafka stack.

To send a message you can use kafka-cat
E.g.

```shell
echo "Hello World" | kcat -b localhost:29092 -t test.topic -P -c 1
```
