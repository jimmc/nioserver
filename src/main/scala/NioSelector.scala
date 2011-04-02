import scala.collection.JavaConversions
import java.nio.channels.SelectableChannel
import java.nio.channels.SelectionKey
import java.nio.channels.spi.SelectorProvider

/** Handle nio channel selection. */
class NioSelector extends Runnable {

    val selector = SelectorProvider.provider.openSelector()

    def register(channel:SelectableChannel, op:Int, body: => Unit) {
        val callback:Function0[Unit] = { () => { body }}
        channel.register(selector, op, callback)
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

