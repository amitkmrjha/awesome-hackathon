import BuildSettings.*
import Dependencies.*

val commonDeps = Seq(scalaTest, scalaCheck, logback, logbackJson, logbackJackson)

val pekkoDeps = Seq(
  pekkoTyped,
  pekkoActorTestkitTyped,
  pekkoClusterShardingTyped,
  pekkoPersistence,
  pekkoPersistenceTestKit,
  pekkoPersistenceJdbc,
  pekkoPersistenceQuery,
  pekkoDiscovery,
  pekkoDiscoveryKubernetes,
  pekkoManagement,
  pekkoManagementClusterBootstrap,
  pekkoManagementHttp,
  pekkoHttpSprayJson,
  pekkoHttp,
  pekkoStream,
  pekkoJackson
)

val postgresDeps = Seq(postgres)

val awsSdk = Seq(awsBedrock, awsBedrockRuntime, awsSts)

val orgJsonCollection = Seq(orgJsonSbt)

val sttpClientDeps = Seq(sttpClientAkkHttp, sttpSprayJson, sttpSlf4JLog)

val excludeLibraryDependencies = Seq(
  ExclusionRule(
    "ssl-config-core_2.13"
  )
)

lazy val `awesome-hackathon` = (project in file("."))
  .aggregate(`awesome-hackathon-protobuf`, `awesome-hackathon-svc`)

lazy val `awesome-hackathon-protobuf` = (project in file("awesome-hackathon-protobuf"))
  .enablePlugins(PekkoGrpcPlugin)
  .settings(pekkoGrpcCodeGeneratorSettings += "server_power_apis")
  .settings(buildSettings *)
  .settings(libraryDependencies ++= commonDeps ++ Seq(scalapbRuntime))

lazy val `awesome-hackathon-svc` = (project in file("awesome-hackathon-svc"))
  .enablePlugins(JavaServerAppPackaging, PekkoGrpcPlugin, JavaAgent)
  .settings(buildSettings *)
  .settings(
    libraryDependencies ++= orgJsonCollection ++ sttpClientDeps ++ awsSdk ++ commonDeps ++
      pekkoDeps ++ postgresDeps ++ Seq(
        snowFlakeJdbc,
        sslConfig
      ),
    excludeDependencies ++= excludeLibraryDependencies
  )
  .dependsOn(`awesome-hackathon-protobuf`)
