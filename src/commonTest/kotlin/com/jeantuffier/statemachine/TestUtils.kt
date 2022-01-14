package com.jeantuffier.statemachine

import kotlinx.coroutines.CoroutineScope

internal expect fun runBlockingTest(block: suspend CoroutineScope.() -> Unit)