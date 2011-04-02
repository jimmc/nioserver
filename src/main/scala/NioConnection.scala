import java.nio.ByteBuffer
import java.nio.channels.SelectionKey
import java.nio.channels.SocketChannel
import scala.util.continuations._

object NioConnection {
    def newConnection(selector:NioSelector, socket:SocketChannel) {
        val conn = new NioConnection(selector,socket)
        conn.start()
    }
}

class NioConnection(selector:NioSelector, socket:SocketChannel) {

    private val buffer = ByteBuffer.allocateDirect(2000)

    def start():Unit = {
        reset {
            while (socket.isOpen)
                readWait
        }
    }

    private def readWait = {
        buffer.clear()
        val count = read(buffer)
        if (count<1)
            socket.close()
        else
            readAction(buffer)
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

    private def readAction(b:ByteBuffer) {
        b.flip()
        socket.write(b)
        b.clear()
    }
}

