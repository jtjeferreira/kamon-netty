/*
 * =========================================================================================
 * Copyright © 2013-2017 the kamon project <http://kamon.io/>
 *
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file
 * except in compliance with the License. You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software distributed under the
 * License is distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND,
 * either express or implied. See the License for the specific language governing permissions
 * and limitations under the License.
 * =========================================================================================
 */

package kamon.netty


import java.lang.invoke.MethodHandle

import io.netty.channel.AbstractChannel
import io.netty.handler.codec.http.{HttpRequest, HttpResponse}
import kamon.Kamon
import kamon.context.{Context, TextMap}

package object instrumentation {

  implicit class ContextAwareSyntax(val channel: io.netty.channel.Channel) extends AnyVal {
    def toContextAware(): ChannelContextAware =
      channel.asInstanceOf[ChannelContextAware]
  }

  implicit class HttpSyntax(val obj: AnyRef) extends AnyVal {
    def toHttpRequest(): HttpRequest =
      obj.asInstanceOf[HttpRequest]

    def isHttpRequest(): Boolean =
      obj.isInstanceOf[HttpRequest]

    def toHttpResponse(): HttpResponse =
      obj.asInstanceOf[HttpResponse]

    def isHttpResponse(): Boolean =
      obj.isInstanceOf[HttpResponse]
  }

  implicit class AbstractChannelSyntax(val channel: AnyRef) extends AnyVal {
    def isOpen():Boolean =
      AbstractChannelMethodInvoker.callIsOpen(channel)
  }

  def isError(statusCode: Int): Boolean =
    statusCode >= 500 && statusCode < 600

  def encodeContext(ctx:Context, request:HttpRequest): HttpRequest = {
    val textMap = Kamon.contextCodec().HttpHeaders.encode(ctx)
    textMap.values.foreach { case (key, value) => request.headers().add(key, value) }
    request
  }

  def decodeContext(request: HttpRequest): Context = {
    val headersTextMap = readOnlyTextMapFromHeaders(request)
    Kamon.contextCodec().HttpHeaders.decode(headersTextMap)
  }

  private def readOnlyTextMapFromHeaders(request: HttpRequest): TextMap = new TextMap {
    import scala.collection.JavaConverters._

    private val headersMap = request.headers().iterator().asScala.map { h => h.getKey -> h.getValue }.toMap

    override def values: Iterator[(String, String)] = headersMap.iterator
    override def get(key: String): Option[String] = headersMap.get(key)
    override def put(key: String, value: String): Unit = {}
  }

  private object AbstractChannelMethodInvoker {
    import java.lang.invoke.{MethodHandles, MethodType}

    val isOpenMethodType: MethodType = MethodType.methodType(classOf[Unit], classOf[Boolean])
    val isOpenHandle: MethodHandle = MethodHandles.lookup.findVirtual(classOf[AbstractChannel],"isOpen", isOpenMethodType)

    def callIsOpen(obj:AnyRef):Boolean =
      isOpenHandle.invokeExact(obj)
  }
}
