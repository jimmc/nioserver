/* Copyright 2010,2011 Jim McBeath under GPLv2 */

package net.jimmc.scoroutine

import scala.annotation.tailrec
import scala.util.continuations._

/** Status indicating the results of a request to a scheduler
 * to run coroutines.
 * @author Jim McBeath
 * @since 1.0
 */
sealed abstract class RunStatus

/** Status indicating that a coroutine ran. */
case object RanOneRoutine extends RunStatus

/** Status indicating that there are still some coroutines that are
 * blocked from running. */
case object SomeRoutinesBlocked extends RunStatus

/** Status indicating that all coroutines have completed. */
case object AllRoutinesDone extends RunStatus

//If you want to get a feel for the order of execution of various
//coroutines, uncomment all of the print statements.

/** The API for the scheduler that is used to control when coroutines run.
 * @author Jim McBeath
 * @since 1.0
 */
trait CoScheduler extends Runnable { cosched =>
    private val defaultLock = new java.lang.Object

    private[scoroutine] def setRoutineContinuation(
            b:Blocker, cont:Option[Unit=>Unit]):Unit

    /** Note that a possibly-blocked continuation is now probably runnable. */
    def unblocked(b:Blocker):Unit

    /** Find the next runnable coroutine and run it until it yields
     * (by calling waitUntilNotBlocked).
     * @return The status telling what happened.
     */
    def runNextUnblockedRoutine():RunStatus

    /** Wait until we get notified via coNotify that we might have
     * some work to do, or until the specified number of time in milliseconds
     * has passed.
     * The default implementation does a wait on our defaultLock.
     */
    def coWait():Unit = {
        defaultLock.synchronized { defaultLock.wait() }
    }

    /** Notify us that a potentially blocked CoResource may have become ready
     * due to actions in another coroutine.
     * Subclass can override if it wants to use this info to optimize.
     * The default implementation does a notify on our defaultLock.
     */
    def coNotify():Unit = {
        defaultLock.synchronized { defaultLock.notify }
    }

    /* We use a class rather than an object because we are using the
     * instance as a key to find more info about the associated routine. */
    /** A Blocker that never blocks.
     * This is when adding a new coroutine.
     */
    class BlockerNever() extends Blocker {
        val scheduler = cosched
        val isBlocked = false
    }

    //Can't have a version with just the body arg because we are using
    //a curried parameter list on addRoutine(name)(body).

    /** Add a new coroutine to the queue and mark it as ready to run.
     * @param name The name of the coroutine, for logging and debugging.
     * @param body The executable body of the coroutine.
     */
    def addRoutine(name:String)(body: => Unit @suspendable) {
        //println("addRoutine "+name)
        reset {
            val blocker = new BlockerNever()
            waitUntilNotBlocked(blocker)
            //println("before run "+name)
            body
            //println("after run "+name)
        }
    }

    /** Alias for runNextUnblockedRoutine. */
    def runOne() = runNextUnblockedRoutine()

    /** Keep running coroutines until there are no more runnable coroutines.
     * @return Status telling whether there are still any unfinished coroutines.
     */
    def runUntilBlockedOrDone():RunStatus = {
        var status:RunStatus = RanOneRoutine
        while (status==RanOneRoutine) {
            status = runNextUnblockedRoutine()
        }
        status
    }

    /** Run forever. */
    def run {
        while (true) {
            runUntilBlockedOrDone
            coWait
        }
    }

    /** Save the continuation of the current coroutine,
     * to be executed later when then specified Blocker is not blocked.
     * @param b The Blocker that tells when the continuation is running.
     */
    def waitUntilNotBlocked(b:Blocker):Unit @suspendable = {
        shift( (cont: Unit=>Unit) => {
            //println(""+b+(if (b.isBlocked) " blocked" else " not blocked"))
            setRoutineContinuation(b,Some(cont))
        })
    }
}
