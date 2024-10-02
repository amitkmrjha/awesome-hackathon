FROM 893087526002.dkr.ecr.eu-west-1.amazonaws.com/bynder-amazoncorretto-sbt:jdk17-builder as base

COPY --chown=bynder:bynder .scalafmt.conf .scalafmt.conf
COPY --chown=bynder:bynder project project
COPY --chown=bynder:bynder build.sbt build.sbt

# Only fetch dependencies
RUN sbt update

COPY --chown=bynder:bynder src src

RUN sbt stage

FROM base as test

RUN sbt scalafmtCheckAll coverage test coverageReport

FROM scratch AS coverage
COPY --from=test /app/target/scala-2.13/ .
COPY --from=test /app/target/test-reports/ ./test-reports

FROM 893087526002.dkr.ecr.eu-west-1.amazonaws.com/bynder-amazoncorretto:jdk17 as prod

COPY --from=base --chown="bynder:bynder" /app/target/universal/stage /app

ENTRYPOINT "/app/bin/taxonomy-migration-svc"
CMD []