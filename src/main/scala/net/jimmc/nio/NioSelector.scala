package net.jimmc.nio

import scala.collection.JavaConversions
import scala.collection.mutable.SynchronizedQueue
import java.nio.channels.SelectableChannel
import java.nio.channels.SelectionKey
import java.nio.channels.spi.SelectorProvider

/** Handle nio channel selection. */
class NioSelector extends Runnable {

    val selector = SelectorProvider.provider.openSelector()

    private case class RegistrationRequest(
        channel:SelectableChannel,op:Int,callback:Function0[Unit])
    private val regQ = new SynchronizedQueue[RegistrationRequest]

    def register(channel:SelectableChannel, op:Int, body: => Unit) {
        val callback:Function0[Unit] = { () => { body }}
        regQ.enqueue(RegistrationRequest(channel,op,callback))
        selector.wakeup()
    }

    def run() {
        selectLoop(true)
    }

    def selectLoop(continueProcessing: => Boolean) {
        while (continueProcessing) {
            selectOnce(0)
        }
    }

    def selectOnce(timeout:Long) {
        while (regQ.size>0) {
            val req = regQ.dequeue()
            req.channel.register(selector,req.op,req.callback)
        }
        selector.select(timeout)
        val jKeys:java.util.Set[SelectionKey] = selector.selectedKeys
        val keys = JavaConversions.asScalaSet(jKeys).toList
        selector.selectedKeys.clear()
        keys foreach { _.interestOps(0) }
        val callbacks = keys map { _.attachment.asInstanceOf[()=>Unit] }
        executeCallbacks(callbacks) //Execute callbacks for all selected keys
    }

    def executeCallbacks(callbacks:List[()=>Unit]) {
        callbacks foreach { _() }
    }
}

