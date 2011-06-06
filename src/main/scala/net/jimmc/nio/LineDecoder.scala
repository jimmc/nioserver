package net.jimmc.nio

import java.nio.{ByteBuffer,CharBuffer}
import java.nio.charset.{Charset,CharsetDecoder,CharsetEncoder,CoderResult}
import scala.util.continuations._

class LineDecoder {

    //Encoders and decoders are not multi-thread safe, so create one
    //for each connection in case we are using multiple threads.
    val utf8Charset = Charset.forName("UTF-8")
    val utf8Encoder = utf8Charset.newEncoder
    val utf8Decoder = utf8Charset.newDecoder

    def processBytes(b:ByteBuffer,
            lineHandler:(String)=>Unit @suspendable):Unit @suspendable =
        processChars(utf8Decoder.decode(b),lineHandler)

    private def processChars(cb:CharBuffer,
            lineHandler:(String)=>Unit @suspendable):Unit @suspendable = {
        val len = lengthOfFirstLine(cb)
        if (len>=0) {
            val ca = new Array[Char](len)
            cb.get(ca,0,len)
            eatLineEnding(cb)
            val line = new String(ca)
            lineHandler(line)
            processChars(cb, lineHandler)       //handle multiple lines
        }
    }

    //Assuming the first character in the buffer is an eol char,
    //consume it and a possible matching CR or LF in case the EOL is 2 chars.
    private def eatLineEnding(cb:CharBuffer) {
        //Eat the first character and see what it is
        cb.get match {
            case '\n' => if (cb.remaining>0 && cb.charAt(0)=='\r') cb.get
            case '\r' => if (cb.remaining>0 && cb.charAt(0)=='\n') cb.get
            case _ => //ignore everything else (TODO - report error?)
        }
    }

    /*
    private def lengthOfFirstLine(cb:CharBuffer):Int = {
        var cbLen = cb.remaining
        for (i <- 0 until cbLen) {
            val ch = cb.charAt(i)
            if (ch == '\n' || ch == '\r')
                return i
        }
        return -1
    }
    */
    private def lengthOfFirstLine(cb:CharBuffer):Int = {
        (0 until cb.remaining) find { i =>
            List('\n','\r').indexOf(cb.charAt(i))>=0 } getOrElse -1
    }
}
