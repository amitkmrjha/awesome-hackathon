package com.by.aw.hackathon.util

import org.bouncycastle.jce.provider.BouncyCastleProvider

import java.security.spec.PKCS8EncodedKeySpec
import java.security.{KeyFactory, PrivateKey, Security}
import java.sql.{Connection, DriverManager}
import java.util.{Base64, Properties}
import scala.io.Source
import scala.util.Try

object SnowFlakeJdbc:
  Security.addProvider(new BouncyCastleProvider)
  val PRIVATE_KEY_FILE = "/Users/amit.kumar/ws/bynder/hackathon/snowflake_dev_rsa_key.p8"

  def loadPrivateKey(path: String): PrivateKey = {
    // Load the private key from PEM file
    val keyPem   = Source.fromFile(path).getLines().filterNot(line => line.startsWith("-----")).mkString
    val keyBytes = Base64.getDecoder.decode(keyPem)

    // Convert to PrivateKey format (DER -> PKCS8)
    val keySpec    = new PKCS8EncodedKeySpec(keyBytes)
    val keyFactory = KeyFactory.getInstance("RSA")
    keyFactory.generatePrivate(keySpec)
  }

  def getConnection: Try[Connection] =
    val properties = new java.util.Properties()
    properties.put("user", "hackathon")
    properties.put("privateKey", loadPrivateKey(PRIVATE_KEY_FILE))
    properties.put("warehouse", "EXTERNAL_ANALYTICS_READER")
    properties.put("db", "BYNDER_EXTERNAL_ANALYTICS_DB")
    properties.put("schema", "EXTERNAL_ANALYTICS_BI")
    properties.put("role", "FULL_READONLY")
    val connectStr = "jdbc:snowflake://bynder-product_ops_development.snowflakecomputing.com"
    Try(DriverManager.getConnection(connectStr, properties))
