import net.jimmc.scoroutine.DefaultCoScheduler

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
    val sched = new DefaultCoScheduler
    val listener = new NioListener(sched, selector, hostAddr, port)

    def start() {
        listener.start(true)
        //run the NIO selector on its own thread
        (new Thread(selector,"NioSelector")).start
        Thread.currentThread.setName("CoScheduler")
        sched.run    //run the coroutine scheduler on our thread, renamed
    }
}
