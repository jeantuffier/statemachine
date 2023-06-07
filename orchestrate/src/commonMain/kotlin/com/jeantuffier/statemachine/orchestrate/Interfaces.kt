package com.jeantuffier.statemachine.orchestrate

interface Event
interface PageLoader {
    val offset: Offset
    val limit: Limit
}
