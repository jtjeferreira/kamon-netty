package kamon.netty.instrumentation.advisor

import io.netty.util.concurrent.EventExecutor
import kamon.agent.libs.net.bytebuddy.asm.Advice.{OnMethodEnter, This}
import kamon.netty.Metrics
import kamon.netty.util.EventLoopUtils.name

class NioCancelMethodAdvisor
object NioCancelMethodAdvisor {

  @OnMethodEnter
  def onEnter(@This eventLoop: EventExecutor): Unit = {
    val registeredChannels = Metrics.forEventLoop(name(eventLoop)).registeredChannels
    registeredChannels.decrement()
  }

}