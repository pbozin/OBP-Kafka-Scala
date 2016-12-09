/*
Open Bank Project - API
Copyright (C) 2011-2015, TESOBE / Music Pictures Ltd

This program is free software: you can redistribute it and/or modify
it under the terms of the GNU Affero General Public License as published by
the Free Software Foundation, either version 3 of the License, or
(at your option) any later version.

This program is distributed in the hope that it will be useful,
but WITHOUT ANY WARRANTY; without even the implied warranty of
MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
GNU Affero General Public License for more details.

You should have received a copy of the GNU Affero General Public License
along with this program. If not, see http://www.gnu.org/licenses/.

Email: contact@tesobe.com
TESOBE / Music Pictures Ltd
Osloerstrasse 16/17
Berlin 13359, Germany
*/

import java.util.{Optional, Properties}
import java.util.concurrent.TimeUnit

import akka.actor.ActorSystem
import com.tesobe.obp.kafka.SimpleSouth
import com.tesobe.obp.transport.{Decoder, Encoder, Responder, Transport}
import com.tesobe.obp.transport.Transport.Factory
import com.tesobe.obp.transport.spi.{ReceiverNov2016, DefaultResponder, LoggingReceiver}
import com.tesobe.obp.transport.spi.Receiver.Codecs
import com.typesafe.config.ConfigFactory
import kafka.utils.Json
import net.liftweb.json
import net.liftweb.json.{DefaultFormats, JsonAST, Serialization}
import org.apache.commons.codec.binary.Base64
import net.liftweb.util.Props

import scala.io.Source
import scala.concurrent.duration.Duration

object  OBPKafkaSandbox extends App {
  //org.apache.log4j.BasicConfigurator.configure()
  val props = new Properties()
  props.put("log4j.logger.org.apache.zookeeper", "ERROR")
  org.apache.log4j.PropertyConfigurator.configure(props)
  val config = ConfigFactory.load()
  val sandboxFilename = config.getString("sandbox.filename")

  val factory : Factory = Transport.factory(Transport.Version.Nov2016, Transport.Encoding.json).get
  //val factory : Factory = Transport.defaultFactory()
  val responder = new OBPResponder
  responder.importSandboxData(sandboxFilename)

  val receiver = new ReceiverNov2016(responder, factory.codecs())

  type JAccount = com.tesobe.obp.transport.Account
  type JBank = com.tesobe.obp.transport.Bank
  type JTransaction = com.tesobe.obp.transport.Transaction

  type JConnector = com.tesobe.obp.transport.Connector
  type JHashMap = java.util.HashMap[String, Object]

  val producerProps : JHashMap = new JHashMap
  val consumerProps : JHashMap = new JHashMap

  consumerProps.put("bootstrap.servers", Props.get("kafka.host").openOr("localhost:9092"))
  producerProps.put("bootstrap.servers", Props.get("kafka.host").openOr("localhost:9092"))

  val south: SimpleSouth = new SimpleSouth(
    Props.get("kafka.request_topic").openOr("Request"),
    Props.get("kafka.response_topic").openOr("Response"),
    consumerProps, producerProps, new LoggingReceiver(receiver)
  )

  val connector = factory.connector(south)
  south.receive() // start Kafka

}


class OBPResponder extends DefaultResponder {

  implicit var formats = DefaultFormats
  var sandboxJson = json.parse("") 

  def importSandboxData(sandboxFilename: String) = {
    sandboxJson = json.parse(Source.fromFile(sandboxFilename).mkString)
    println(sandboxJson)
  }

  override def getBanks(p: Decoder.Pager, ps: Decoder.Parameters, e: Encoder): String = { 
    return Serialization.write(sandboxJson \ "banks")
  }

}

