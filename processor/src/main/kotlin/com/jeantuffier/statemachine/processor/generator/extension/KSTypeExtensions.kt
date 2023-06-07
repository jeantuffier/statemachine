package com.jeantuffier.statemachine.processor.generator.extension

import com.google.devtools.ksp.symbol.KSType
import com.jeantuffier.statemachine.orchestrate.Action
import com.jeantuffier.statemachine.orchestrate.Orchestrated
import com.jeantuffier.statemachine.orchestrate.OrchestratedData
import com.jeantuffier.statemachine.orchestrate.OrchestratedPage
import com.jeantuffier.statemachine.orchestrate.Orchestration
import com.squareup.kotlinpoet.asClassName
import com.squareup.kotlinpoet.ksp.toClassName

fun KSType.isOrchestration() = toClassName() == Orchestration::class.asClassName()

fun KSType.isOrchestrated() = toClassName() == Orchestrated::class.asClassName()

fun KSType.isAction() = toClassName() == Action::class.asClassName()

fun KSType.isOrchestratedData() = toClassName() == OrchestratedData::class.asClassName()

fun KSType.isOrchestratedPage() = toClassName() == OrchestratedPage::class.asClassName()

fun KSType.firstArgumentClassName() = arguments[0].type!!.resolve().toClassName()

fun KSType.lowerCaseSimpleName(): String = toClassName().simpleName.replaceFirstChar(Char::lowercaseChar)
