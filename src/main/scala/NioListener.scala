import net.jimmc.scoroutine.CoScheduler

import java.net.{InetAddress,InetSocketAddress}
import java.nio.channels.{ServerSocketChannel,SocketChannel}
import java.nio.channels.SelectionKey
import scala.util.continuations._

class NioListener(sched:CoScheduler, readSelector:NioSelector,
        writeSelector:NioSelector, hostAddr:InetAddress, port:Int) {

    val serverChannel = ServerSocketChannel.open()
    serverChannel.configureBlocking(false);
    val isa = new InetSocketAddress(hostAddr,port)
    serverChannel.socket.bind(isa)

    def start(continueListening: =>Boolean):Unit = {
        reset {
            while (continueListening) {
                val socket = accept()
                NioConnection.newConnection(sched,
                    readSelector,writeSelector,socket)
            }
        }
    }

    private def accept():SocketChannel @suspendable = {
        shift { k =>
            readSelector.register(serverChannel,SelectionKey.OP_ACCEPT, {
                val conn = serverChannel.accept()
                conn.configureBlocking(false)
                k(conn)
            })
        }
    }
}
