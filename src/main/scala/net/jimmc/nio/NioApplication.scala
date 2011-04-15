package net.jimmc.nio

import net.jimmc.scoroutine.DefaultCoScheduler

import scala.util.continuations._

abstract class NioApplication {
    val readSelector = new NioSelector()
    val writeSelector = new NioSelector()
    val sched = new DefaultCoScheduler

    def runConnection(conn:NioConnection):Unit @suspendable
}
