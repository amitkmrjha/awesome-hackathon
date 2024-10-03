package com.by.aw.hackathon.util

import java.sql.DriverManager
import java.sql.Connection
import java.sql.ResultSet
import java.sql.ResultSetMetaData
import java.sql.SQLException
import java.sql.Statement
import java.util.Properties
import scala.util.{Failure, Success, Try}

object SnowFlakeJdbc:

  def getConnection: Try[Connection] =
    val properties = new java.util.Properties()
    properties.put("user", "NICHOLAS.ANDO@BYNDER.COM")
    // properties.put("authenticator", "externalbrowser")
    properties.put("password", "password")
    properties.put("warehouse", "EXTERNAL_ANALYTICS_READER")
    properties.put("db", "BYNDER_EXTERNAL_ANALYTICS_DB")
    properties.put("schema", "EXTERNAL_ANALYTICS_BI")
    properties.put("role", "FULL_READONLY")
    val connectStr = "jdbc:snowflake://bynder-product_ops_development.snowflakecomputing.com"
    Try(DriverManager.getConnection(connectStr, properties))
