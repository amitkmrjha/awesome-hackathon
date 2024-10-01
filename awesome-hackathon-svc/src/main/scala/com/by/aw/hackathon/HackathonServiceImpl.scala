package com.by.aw.hackathon

import com.aw.hackathon.grpc.*
import com.google.protobuf.empty.Empty
import com.google.protobuf.timestamp.Timestamp
import org.apache.pekko.actor.typed.ActorSystem
import org.apache.pekko.cluster.sharding.typed.scaladsl.ClusterSharding
import org.apache.pekko.grpc.scaladsl.Metadata
import org.apache.pekko.util.Timeout
import org.slf4j.LoggerFactory

import java.net.InetAddress
import java.time.Instant
import scala.concurrent.{ExecutionContext, Future}

class HackathonServiceImpl[A: ActorSystem]() extends HackathonServicePowerApi:

  val system   = summon[ActorSystem[?]]
  val log      = LoggerFactory.getLogger(getClass)
  val sharding = ClusterSharding(system)

  given ec: ExecutionContext = system.executionContext

  given timeout: Timeout = Timeout.create(system.settings.config.getDuration("nft-asset-svc.grpc.ask-timeout"))

  private val hostname = InetAddress.getLocalHost.getHostAddress

  override def getHealth(in: Empty, metadata: Metadata): Future[GetHealthResponse] =
    Future.successful(
      GetHealthResponse("NFTAsset gRPC is healthy!", hostname, Some(Timestamp(Instant.now())))
    )
