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
    val readSelector = new NioSelector()
    val writeSelector = new NioSelector()
    val sched = new DefaultCoScheduler
    val listener = new NioListener(sched,
        readSelector, writeSelector, hostAddr, port)

    def start() {
        listener.start(true)
        //run the NIO read and write selectors each on its own thread
        (new Thread(writeSelector,"WriteSelector")).start
        (new Thread(readSelector,"ReadSelector")).start
        Thread.currentThread.setName("CoScheduler")
        sched.run    //run the coroutine scheduler on our thread, renamed
    }
}
