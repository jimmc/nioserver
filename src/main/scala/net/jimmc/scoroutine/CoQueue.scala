/* Copyright 2010,2011 Jim McBeath under GPLv2 */

package net.jimmc.scoroutine

import scala.util.continuations._
import scala.collection.mutable.SynchronizedQueue

//Use scaladoc in version 2.8.0 after about August 24 in order for
//the "@constructor" tag to work correctly.
/** A Queue designed to work with coroutines by providing blocking
 * versions of enqueue and dequeue.
 * @author Jim McBeath
 * @since 1.0
 * @constructor Create a CoQueue.
 * @param scheduler The scheduler to be used with the coroutines
 *        using this queue.
 * @param waitSize The queue will be considered full when it has this
 *        many entries, at which point a coroutine trying to put another
 *        element into the queue will be blocked.
 */
class CoQueue[A](val scheduler:CoScheduler, val waitSize:Int)
        extends SynchronizedQueue[A] { coqueue =>

    /** Blocks when the queue is full. */
    val enqueueBlocker = new Blocker() {
        val scheduler = coqueue.scheduler
        def isBlocked() = length >= waitSize
    }

    /** Blocks when the queue is empty. */
    val dequeueBlocker = new Blocker() {
        val scheduler = coqueue.scheduler
        def isBlocked() = isEmpty
    }

    /** Suspend the calling coroutine until there is space to put
     * a value into the queue.
     * Upon resuming, add the value to the queue.
     * @param x The value to be added to the queue.
     */
    def blockingEnqueue(x:A):Unit @suspendable = {
        enqueueBlocker.waitUntilNotBlocked
        enqueue(x)
        scheduler.coNotify
    }

    /** Suspend the calling coroutine until there is a value ready to
     * be taken out of the queue.
     * Upon resuming, remove the first value from the queue and return it.
     * @return The first value in the queue.
     */
    def blockingDequeue():A @suspendable = {
        dequeueBlocker.waitUntilNotBlocked
        val x = dequeue
        scheduler.coNotify
        x
    }
}
