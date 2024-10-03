package com.by.aw.hackathon.repository

import org.apache.pekko.actor.typed.ActorSystem

import java.sql.{Connection, ResultSet, Statement}
import scala.concurrent.{ExecutionContext, Future}
import scala.util.{Success, Try}

trait SnowFlakeRepository:
  def close: Unit
  def executeQuery(query: String): Future[Seq[String]]

class DefaultSnowFlakeRepository(connection: Try[Connection])(using system: ActorSystem[?]) extends SnowFlakeRepository:

  given executionContext: ExecutionContext = system.executionContext

  val statement: Try[Statement] = connection.map(_.createStatement())

  override def executeQuery(query: String): Future[Seq[String]] =
    Future.fromTry(statement).map { statement =>
      val resultSet: ResultSet = statement.executeQuery(query)
      val results              = scala.collection.mutable.Buffer[String]()
      while (resultSet.next()) {
        results += resultSet.getString("ASSET_ID") // Adjust the column name as needed
      }
      results.toSeq
    }

  override def close: Unit =
    (connection, statement) match
      case (Success(conn), Success(stm)) =>
        stm.close()
        conn.close()
      case _                             => ()
