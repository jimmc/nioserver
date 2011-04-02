import java.net.InetAddress

object NioServer {
    def main(args:Array[String]) {
        val hostAddr:InetAddress = null //listen on local connection
        val port = 1234
        val server = new NioServer(hostAddr,port)
        server.start()
    }
}

class NioServer(hostAddr:InetAddress, port:Int) {
    val selector = new NioSelector()
    val listener = new NioListener(selector, hostAddr, port)

    def start() {
        listener.start(true)
        selector.run()
    }
}
