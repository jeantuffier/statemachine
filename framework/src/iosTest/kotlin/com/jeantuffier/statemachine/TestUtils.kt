package com.jeantuffier.statemachine

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.newSingleThreadContext
import kotlinx.coroutines.runBlocking
import kotlin.coroutines.CoroutineContext

@ExperimentalCoroutinesApi
private val testCoroutineContext: CoroutineContext = newSingleThreadContext("testRunner")

@ExperimentalCoroutinesApi
internal actual fun runBlockingTest(block: suspend CoroutineScope.() -> Unit) =
    runBlocking(testCoroutineContext) { this.block() }