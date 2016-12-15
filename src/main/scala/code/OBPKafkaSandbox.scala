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
import code.json.{AccountJSON, BankJSON, TransactionId}
import com.tesobe.obp.kafka.SimpleSouth
import com.tesobe.obp.transport.Transport
import com.tesobe.obp.transport.Transport.Factory
import com.tesobe.obp.transport.spi._
import com.typesafe.config.ConfigFactory
import kafka.utils.Json
import net.liftweb.common.Full
import net.liftweb.util.Helpers.tryo
import net.liftweb.json
import net.liftweb.json.Extraction
import net.liftweb.json.compactRender
import net.liftweb.json.render
import net.liftweb.json.JsonAST.{JField, JObject, JString, JValue}
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

  //val factory : Factory = Transport.factory(Transport.Version.Nov2016, Transport.Encoding.json).get
  val factory : Factory = Transport.defaultFactory()
  //val responder = new OBPResponder
  //responder.importSandboxData(sandboxFilename)

  val receiver = new OBPResponder(factory.decoder(), factory.encoder())
  receiver.importSandboxData(sandboxFilename)

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


class OBPResponder(d: Decoder, e: Encoder) extends DefaultReceiver(d, e) {

  implicit var formats = DefaultFormats
  var sandboxJson = json.parse("") 

  def importSandboxData(sandboxFilename: String) = {
    sandboxJson = json.parse(Source.fromFile(sandboxFilename).mkString)
    println(sandboxJson)
  }

  override def getBanks(r: Decoder.Request, e: Encoder): String = {
    val list = for {
      banks@JObject(_) <- sandboxJson \ "banks"
      bank@JObject(b) <- banks
      JField("id", JString(id)) <- bank
      JField("short_name", JString(short)) <- bank
      JField("full_name", JString(name)) <- bank
      JField("logo", JString(logo)) <- bank
      JField("website", JString(url)) <- bank
    } yield BankJSON(id, short, name, logo, url)
    return compactRender(Extraction.decompose(list))
  }

  override def getBank(r: Decoder.Request, e: Encoder): String = {
    val list = for {
      banks@JObject(_) <- sandboxJson \ "banks"
      bank@JObject(b) <- banks
      if (b contains JField("id", JString(r.bankId.get)))
      JField("id", JString(id)) <- bank
      JField("short_name", JString(short)) <- bank
      JField("full_name", JString(name)) <- bank
      JField("logo", JString(logo)) <- bank
      JField("website", JString(url)) <- bank
    } yield BankJSON(id, short, name, logo, url)
    val xyz = list match {
      case List(x) => x
      case _ => BankJSON("", "", "", "", "")
    }
    return compactRender(Extraction.decompose(xyz))
  }

  override def getAccount(r: Decoder.Request,  e: Encoder): String = {
    val list = for {
      accounts@JObject(_) <- sandboxJson \ "accounts"
      account@JObject(a) <- accounts
      if (a contains JField("id", JString(r.accountId().get)))
      if (a contains JField("bank", JString(r.bankId().get)))
      JString(id) <- account \\ "id"
      JString(bank) <- account \\ "bank"
      JString(label) <- account \\ "label"
      JString(number) <- account \\ "number"
      JString(type1) <- account \\ "type"
      JString(currency) <- account \\ "currency"
      JString(amount) <- account \\ "amount"
      JString(iban) <- account \\ "IBAN"
    } yield AccountJSON(id, bank, label, number, type1, currency, amount, iban)
    val xyz = list match {
      case List(x) => x
      case _ => AccountJSON("", "", "", "", "","","","")
    }
    return compactRender(Extraction.decompose(xyz))
  }

  override def getAccounts(r: Decoder.Request,  e: Encoder): String = {
    val list = for {
      accounts@JObject(_) <- sandboxJson \ "accounts"
      account@JObject(a) <- accounts
      JString(id) <- account \\ "id"
      JString(bank) <- account \\ "bank"
      JString(label) <- account \\ "label"
      JString(number) <- account \\ "number"
      JString(type1) <- account \\ "type"
      JString(currency) <- account \\ "currency"
      JString(amount) <- account \\ "amount"
      JString(iban) <- account \\ "IBAN"
    } yield AccountJSON(id, bank, label, number, type1, currency, amount, iban)
    return compactRender(Extraction.decompose(list))
  }

}

