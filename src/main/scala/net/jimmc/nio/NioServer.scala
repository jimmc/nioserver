package net.jimmc.nio

import net.jimmc.scoroutine.DefaultCoScheduler

import java.net.InetAddress

class NioServer(app:NioApplication, hostAddr:InetAddress, port:Int) {
    val listener = new NioListener(app, hostAddr, port)

    def start() {
        listener.start(true)
        //run the NIO read and write selectors each on its own thread
        (new Thread(app.writeSelector,"WriteSelector")).start
        (new Thread(app.readSelector,"ReadSelector")).start
        Thread.currentThread.setName("CoScheduler")
        app.sched.run    //run the coroutine scheduler on our thread, renamed
    }
}
