package com.by.aw.hackathon.repository

import com.by.aw.hackathon.util.SnowFlakeJdbc
import org.apache.pekko.actor.typed.ActorSystem

import java.sql.{Connection, Statement}
import scala.concurrent.{ExecutionContext, Future}
import scala.util.{Failure, Success, Try}

trait SnowFlakeRepository:
  def close: Unit
  def executeQuery(query: String): Future[Seq[String]]

class DefaultSnowFlakeRepository(connection: Try[Connection])(using system: ActorSystem[?]) extends SnowFlakeRepository:

  given executionContext: ExecutionContext = system.executionContext

  val statement: Try[Statement] = connection.map(_.createStatement())

  override def executeQuery(query: String): Future[Seq[String]] =
    Future.fromTry(statement).map { statement =>
      val resultSet   = statement.executeQuery(query)
      val metaData    = resultSet.getMetaData
      val columnCount = metaData.getColumnCount
      val columnNames = (1 to columnCount).map(metaData.getColumnName)
      val rows        = Iterator
        .continually(resultSet)
        .takeWhile(_.next())
        .map { row =>
          columnNames.map(row.getString)
        }
        .toSeq
      rows.map(_.mkString(","))
    }

  override def close: Unit =
    (connection, statement) match
      case (Success(conn), Success(stm)) =>
        stm.close()
        conn.close()
      case _                             => ()
