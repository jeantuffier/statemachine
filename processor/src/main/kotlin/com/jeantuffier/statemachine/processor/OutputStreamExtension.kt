package com.jeantuffier.statemachine.processor

import java.io.OutputStream

operator fun OutputStream.plusAssign(str: String) {
    this.write(str.toByteArray())
}
