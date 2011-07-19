/* Copyright 2010 Jim McBeath under GPLv2 */

package net.jimmc.scoroutine

import scala.collection.mutable.ArrayBuffer
import scala.collection.mutable.HashMap

//If you want to get a feel for the order of execution of various
//coroutines, uncomment all of the print statements.

/** A scheduler that uses a simple single-thread round-robin algorithm.
 * @author Jim McBeath
 * @since 1.0
 */
class DefaultCoScheduler extends CoScheduler {
    private val blockerIndexMap = new HashMap[Blocker,Int]
    private case class BlockerInfo(val blocker:Blocker, index:Int,
            var cont:Option[Unit=>Unit])
    private val blockerList = new ArrayBuffer[BlockerInfo]
    private var nextIndex = 0

    private[scoroutine] def setRoutineContinuation(
            b:Blocker,cont:Option[Unit=>Unit]) {
        if (blockerIndexMap.get(b).isEmpty) {
            //println("Registering blocker "+b)
            val nextIndex = blockerIndexMap.size
            blockerIndexMap.put(b,nextIndex)
            blockerList += BlockerInfo(b, nextIndex, cont)
        } else {
            val n = blockerIndexMap(b)
            blockerList(n).cont = cont
        }
    }

    //We only maintain a single list for both blocked and unblocked tasks.
    def unblocked(b:Blocker) = coNotify

    def runNextUnblockedRoutine():RunStatus = {
        var blockedCount = 0
        for (i <- 0 until blockerList.size) {
            val index = (nextIndex + i) % blockerList.size
            val bInfo = blockerList(index)
            if (bInfo.cont.isDefined && bInfo.blocker.isBlocked) {
                blockedCount += 1
            }
            if (bInfo.cont.isDefined && !bInfo.blocker.isBlocked) {
                nextIndex = index + 1
                val nextCont = bInfo.cont
                bInfo.cont = None
                nextCont.get()          //run the continuation
                return RanOneRoutine
            }
        }
        if (blockedCount>0) {
            //println("Some blocked routines")
            SomeRoutinesBlocked
        } else {
            //println("All routines done")
            AllRoutinesDone
        }
    }
}
