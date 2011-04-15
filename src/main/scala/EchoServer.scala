import net.jimmc.nio.{NioApplication,NioConnection,NioServer}
import net.jimmc.scoroutine.DefaultCoScheduler

import java.net.InetAddress
import scala.util.continuations._

object EchoServer {
    def main(args:Array[String]) {
        val app = new EchoApplication
        val hostAddr:InetAddress = null //listen on local connection
        val port = 1234
        val server = new NioServer(app,hostAddr,port)
        server.start()
    }
}

class EchoApplication extends NioApplication {
    def runConnection(conn:NioConnection):Unit @suspendable = {
        while (conn.isOpen) {
            conn.writeLine(conn.readLine)
        }
    }
}
