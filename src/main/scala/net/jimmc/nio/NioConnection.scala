package net.jimmc.nio

import net.jimmc.scoroutine.{CoQueue,CoScheduler}

import java.nio.ByteBuffer
import java.nio.channels.SelectionKey
import java.nio.channels.SocketChannel
import scala.util.continuations._

object NioConnection {
    def newConnection(app:NioApplication, socket:SocketChannel) {
        val conn = new NioConnection(app, socket)
        conn.start()
    }
}

class NioConnection(app:NioApplication, socket:SocketChannel) {

    private val buffer = ByteBuffer.allocateDirect(2000)
    private val lineDecoder = new LineDecoder
    private val inQ = new CoQueue[String](app.sched, 10)
    private val outQ = new CoQueue[String](app.sched, 10)

    def start():Unit = {
        startReader
        startWriter
        startApp
    }

    private def startApp() {
        reset {
            app.runConnection(this)
            close()
        }
    }

    private def startReader() {
        reset {
            while (socket.isOpen)
                readWait
        }
    }

    private def readWait:Unit @suspendable = {
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
            app.readSelector.register(socket, SelectionKey.OP_READ, {
                val n = socket.read(b)
                k(n)
            })
        }
    }

    def readLine():String @suspendable = inQ.blockingDequeue

    private def startWriter() {
        reset {
            while (socket.isOpen)
                writeWait
        }
    }

    private def write(b:ByteBuffer):Int @suspendable = {
        if (!socket.isOpen)
            -1  //indicate EOF
        else shift { k =>
            app.writeSelector.register(socket, SelectionKey.OP_WRITE, {
                val n = socket.write(b)
                k(n)
            })
        }
    }

    private def writeBuffer(b:ByteBuffer):Unit @suspendable = {
        write(b)
        if (b.remaining>0 && socket.isOpen)
            writeBuffer(b)
        else
            shiftUnit[Unit,Unit,Unit]()
    }

    private def writeWait():Unit @suspendable = {
        val str = outQ.blockingDequeue
        if (str eq closeMarker) {
            socket.close
            shiftUnit[Unit,Unit,Unit]()
        } else
            writeBuffer(ByteBuffer.wrap(str.getBytes("UTF-8")))
    }

    def writeLine(s:String):Unit @suspendable = write(s+"\n")
    def write(s:String):Unit @suspendable = outQ.blockingEnqueue(s)

    def isOpen = socket.isOpen
    private val closeMarker = new String("")
    def close():Unit @suspendable = write(closeMarker)
}
