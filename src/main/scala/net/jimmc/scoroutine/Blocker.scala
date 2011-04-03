/* Copyright 2010 Jim McBeath under GPLv2 */

package net.jimmc.scoroutine

import scala.util.continuations._

/** A trait for defining when a coroutine is blocked by a resource.
 * Multiple Blocker instances may be associated with one coroutine.
 * @author Jim McBeath
 * @since 1.0
 */
trait Blocker {
    /** Concrete class must choose a scheduler implementation. */
    val scheduler:CoScheduler

    /** Determine when a coroutine is running.
     * This method is called by the scheduler when looking for a coroutine
     * to run.
     * @return true when the coroutine is blocked by the resource
     *         represented by this Blocker.
     */
    def isBlocked:Boolean

    /** Suspend the current coroutine and resume at some later point
     * (based on the scheduler) after isBlocked returns false. */
    def waitUntilNotBlocked:Unit @suspendable = {
        if (isBlocked)
            scheduler.waitUntilNotBlocked(this)
    }
}
