package com.jeantuffier.statemachine

sealed class AppError

object SomeRandomError : AppError()