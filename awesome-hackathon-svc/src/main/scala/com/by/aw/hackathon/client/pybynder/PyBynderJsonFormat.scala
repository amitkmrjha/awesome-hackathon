package com.by.aw.hackathon.client.pybynder

import com.by.aw.hackathon.client.pybynder.model.PyBynderModel.CollectionRequest
import org.apache.pekko.http.scaladsl.marshallers.sprayjson.SprayJsonSupport
import spray.json.{DefaultJsonProtocol, RootJsonFormat}

trait PyBynderJsonFormat extends SprayJsonSupport with DefaultJsonProtocol:
  given collectionRequestFormat: RootJsonFormat[CollectionRequest] = jsonFormat1(CollectionRequest.apply)
