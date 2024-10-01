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
    libraryDependencies ++= commonDeps ++ pekkoDeps ++ postgresDeps ++ Seq(sslConfig),
    excludeDependencies ++= excludeLibraryDependencies
  )
  .dependsOn(`awesome-hackathon-protobuf`)
