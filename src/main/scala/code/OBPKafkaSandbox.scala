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

import java.util.Properties
import java.util.concurrent.TimeUnit

import akka.actor.ActorSystem
import code.ResponseCache
import com.typesafe.config.ConfigFactory
import kafka.utils.Json
import net.liftweb.json
import net.liftweb.json.{DefaultFormats, JsonAST}
import org.apache.commons.codec.binary.Base64

import scala.io.Source

import scala.concurrent.duration.Duration

object  OBPKafkaSandbox extends App {
  //org.apache.log4j.BasicConfigurator.configure()
  val props = new Properties()
  props.put("log4j.logger.org.apache.zookeeper", "ERROR")
  org.apache.log4j.PropertyConfigurator.configure(props)
  val config = ConfigFactory.load()
  val sandboxFilename = config.getString("sandbox.filename")
  val transform = new OBPTransform()
  transform.importSandboxData(sandboxFilename)
  //consumer.run(producer, transform.processRequest)
}


class OBPTransform (){

  implicit var formats = DefaultFormats

  def importSandboxData(sandboxFilename: String) = {
    val sandboxJson = json.parse(Source.fromFile(sandboxFilename).mkString)
    println(sandboxJson)
  } 

}
