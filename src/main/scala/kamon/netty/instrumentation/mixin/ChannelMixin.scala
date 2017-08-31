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

package kamon.netty.instrumentation.mixin

import kamon.Kamon
import kamon.agent.api.instrumentation.Initializer
import kamon.context.Context

import scala.beans.BeanProperty


trait ChannelContextAware {
  def setStartTime(value:Long):Unit
  def getStartTime:Long
  def setContext(ctx:Context):Unit
  def getContext:Context
}

/**
  * --
  */
class ChannelContextAwareMixin extends ChannelContextAware {
  @volatile @BeanProperty var startTime = 0L
  @volatile @BeanProperty var context:Context = _

  @Initializer
  def init(): Unit = context = Kamon.currentContext()
}