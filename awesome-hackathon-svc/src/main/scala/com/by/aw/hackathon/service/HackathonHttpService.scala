package com.by.aw.hackathon.service

import com.by.aw.hackathon.model.{ModelRequest, ModelResponse}

import scala.concurrent.Future

trait HackathonHttpService:
  def promptInvoke(request: ModelRequest): Future[ModelResponse]
  def helloToPyBynder: Future[String]
