package org.spongepowered.mctester.internal.coroutine

import kotlinx.coroutines.experimental.CoroutineDispatcher
import kotlinx.coroutines.experimental.CoroutineScope
import kotlinx.coroutines.experimental.Runnable
import kotlinx.coroutines.experimental.runBlocking
import org.spongepowered.api.Sponge
import org.spongepowered.mctester.internal.framework.TesterManager
import kotlin.coroutines.experimental.Continuation
import kotlin.coroutines.experimental.CoroutineContext

class CoroutineTestManager(val block: suspend CoroutineScope.() -> Unit, val testerManager: TesterManager) {

    var continuation: Continuation<Any>? = null
    //private val startContinuation: Continuation<Unit> = block.createCoroutine(CallbackCompletion(this))
    //internal var completed = false

    fun runToCompletion() {
        runBlocking(context = MainThreadDispatcher(this.testerManager), block = this.block)

        /*this.startCoroutine()
        while (!this.completed) {

        }*/
    }

    suspend fun mySuspend(myInt: Int) {

    }

    /*suspend fun doSuspend(block: ((Any) -> Unit)) {
        suspendCoroutine<Any> {
            this.continuation = it
            block(this::driveCoroutine)
        }
    }

    private fun startCoroutine() {
        if (this.continuation != null) {
            throw IllegalStateException("Already started coroutine!")
        }
        this.driveCoroutine(Unit)
    }

    private fun driveCoroutine(value: Any) {
        val continuation = this.continuation
        if (continuation == null) {
            this.startContinuation.resume(Unit)
        } else {
            continuation.resume(value)
        }
    }*/
}

private class MainThreadDispatcher(val testManager: TesterManager): CoroutineDispatcher() {

    override fun dispatch(context: CoroutineContext, block: Runnable) {
        try {
            if (Sponge.getServer().isMainThread) {
                block.run()
            } else {
                this.testManager.runOnMainThread(block)
            }
        } catch (e: Throwable) {
            e.printStackTrace();
            throw RuntimeException("Exception when scheduling coroutine block on main thread!", e)
        }
    }

}

/*private class CallbackCompletion<T>(val coroutineTestManager: CoroutineTestManager): AbstractCoroutine<T>(EmptyCoroutineContext) {

    override fun onCompleted(value: T) {
        this.coroutineTestManager.completed = true
    }
}*/
