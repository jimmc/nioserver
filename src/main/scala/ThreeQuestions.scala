import net.jimmc.nio.{NioApplication,NioServer,NioConnection}

import java.io.{BufferedReader,InputStreamReader,PrintWriter}
import java.net.InetAddress

import scala.util.continuations._

object ThreeQuestionsConsole {
    def main(args:Array[String]) {
        val in = new BufferedReader(new InputStreamReader(System.in))
        val out = new PrintWriter(System.out)
        val io = new SysReader(in,out)
        reset {
            (new ThreeQuestions(io)).run
        }
    }
}

object ThreeQuestionsServer {
    def main(args:Array[String]) {
        val app = new ThreeQuestionsApp
        val hostAddr:InetAddress = null //localhost
        val port = 1234
        val server = new NioServer(app,hostAddr,port)
        server.start()
    }
}

class ThreeQuestionsApp extends NioApplication {
    def runConnection(conn:NioConnection):Unit @suspendable = {
        val io = new ConnReader(conn)
        (new ThreeQuestions(io)).run
    }
}

trait ReaderWriter {
    def readLine():String @suspendable
    def writeLine(s:String):Unit @suspendable
}

class SysReader(in:BufferedReader,out:PrintWriter) extends ReaderWriter {
    def readLine() = in.readLine
    def writeLine(s:String) = { out.println(s); out.flush() }
}

class ConnReader(conn:NioConnection) extends ReaderWriter {
    def readLine():String @suspendable = conn.readLine
    def writeLine(s:String):Unit @suspendable = conn.writeLine(s)
}

class ThreeQuestions(io:ReaderWriter) {
    def run():Unit @suspendable = {
        val RxArthur = ".*arthur.*".r
        val RxGalahad = ".*galahad.*".r
        val RxLauncelot = ".*(launcelot|lancelot).*".r
        val RxRobin = ".*robin.*".r
        val RxHolyGrail = ".*seek the holy grail.*".r
        val RxSwallow = ".*african or european.*".r
        val RxAssyriaCapital =
            ".*(assur|shubat.enlil|kalhu|calah|nineveh|dur.sharrukin).*".r
        val name = ask("What is your name?").toLowerCase
        val quest = ask("What is your quest?").toLowerCase
        val holy = quest match {
            case RxHolyGrail() => true
            case _ => false
        }
        if (holy) {
            val q3Type = name match {
                case RxRobin() => 'capital
                case RxArthur() => 'swallow
                case _ => 'color
            }
            val a3 = (q3Type match {
                case 'capital => ask("What is the capital of Assyria?")
                case 'swallow => ask("What is the air-speed velocity of an unladen swallow?")
                case 'color => ask("What is your favorite color?")
            }).toLowerCase
            (q3Type,a3,name) match {
                //Need to use an underscore in regex patterns with alternates
                case ('capital,RxAssyriaCapital(_),_) => accept
                case ('capital,_,_) => reject
                case ('swallow,RxSwallow(),_) => rejectMe
                case ('swallow,_,_) => reject
                case ('color,"blue",RxLauncelot(_)) => accept
                case ('color,_,RxLauncelot(_)) => reject
                case ('color,"yellow",RxGalahad()) => accept
                case ('color,_,RxGalahad()) => reject
                case ('color,_,_) => accept
            }
        } else {
            reject
        }
    }

    def ask(s:String):String @suspendable = { io.writeLine(s); io.readLine }
    def accept:Unit @suspendable = io.writeLine("You may pass")
    def reject:Unit @suspendable = io.writeLine("you: Auuuuuuuugh!")
    def rejectMe:Unit @suspendable = io.writeLine("me: Auuuuuuuugh!")
}
