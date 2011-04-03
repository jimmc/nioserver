import net.jimmc.scoroutine.{CoQueue,CoScheduler}

import java.nio.ByteBuffer
import java.nio.channels.SelectionKey
import java.nio.channels.SocketChannel
import scala.util.continuations._

object NioConnection {
    def newConnection(sched:CoScheduler, selector:NioSelector, socket:SocketChannel) {
        val conn = new NioConnection(sched,selector,socket)
        conn.start()
    }
}

class NioConnection(sched:CoScheduler, selector:NioSelector, socket:SocketChannel) {

    private val buffer = ByteBuffer.allocateDirect(2000)
    private val lineDecoder = new LineDecoder
    private val inQ = new CoQueue[String](sched, 10)

    def start():Unit = {
        startReader
        startApp
    }

    private def startApp() {
        reset {
            while (socket.isOpen)
                writeLine(readLine())
        }
    }

    private def startReader() {
        reset {
            while (socket.isOpen)
                readWait
        }
    }

    private def readWait = {
        buffer.clear()
        val count = read(buffer)
        if (count<1) {
            socket.close()
            shiftUnit[Unit,Unit,Unit]()
        } else {
            buffer.flip()
            lineDecoder.processBytes(buffer, inQ.blockingEnqueue(_))
        }
    }

    private def read(b:ByteBuffer):Int @suspendable = {
        if (!socket.isOpen)
            -1  //indicate EOF
        else shift { k =>
            selector.register(socket, SelectionKey.OP_READ, {
                val n = socket.read(b)
                k(n)
            })
        }
    }

    def readLine():String @suspendable = inQ.blockingDequeue

    def writeLine(line:String) {
        socket.write(ByteBuffer.wrap((line+"\n").getBytes("UTF-8")))
    }
}
