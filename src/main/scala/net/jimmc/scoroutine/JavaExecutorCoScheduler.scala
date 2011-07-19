/* Copyright 2011 Jim McBeath under GPLv2 */

package net.jimmc.scoroutine

import java.lang.Runnable
import java.util.concurrent.Executors
import java.util.concurrent.ExecutorService

import scala.collection.mutable.LinkedHashMap
import scala.collection.mutable.SynchronizedMap

/** A scheduler that uses a Java ExecutorService.
 * @author Jim McBeath
 * @since 1.0.5
 */
class JavaExecutorCoScheduler(numWorkers:Int) extends CoScheduler {
    type Task = Option[Unit=>Unit]
    case class RunnableCont(task:Task) extends Runnable {
        def run() = task foreach { _() }
    }

    private val pool = Executors.newFixedThreadPool(numWorkers)
    private val lock = new java.lang.Object
    private val blockedTasks = new LinkedHashMap[Blocker,Task] with
            SynchronizedMap[Blocker,Task]

    private[scoroutine] def setRoutineContinuation(b:Blocker,task:Task) {
        lock.synchronized {
            if (b.isBlocked) {
                blockedTasks(b) = task
            } else {
                pool.execute(RunnableCont(task))
                coNotify
            }
        }
    }

    def unblocked(b:Blocker):Unit = {
        lock.synchronized {
            if (!b.isBlocked)
                blockedTasks.remove(b) foreach { task =>
                    pool.execute(RunnableCont(task)) }
        }
        coNotify
    }

    //When using this scheduler, the application doesn't need to call
    //our run method.  If so, we just wait, as we have no work to do.
    def runNextUnblockedRoutine():RunStatus = SomeRoutinesBlocked
}
