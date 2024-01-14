package com.jeantuffier.generator.generator.extension

import com.google.devtools.ksp.symbol.KSType
import com.jeantuffier.generator.GeneratorProcessor.Companion.PACKAGE_NAME
import com.jeantuffier.statemachine.orchestrate.Action
import com.jeantuffier.statemachine.orchestrate.Feature
import com.jeantuffier.statemachine.orchestrate.Orchestrated
import com.jeantuffier.statemachine.orchestrate.OrchestratedData
import com.jeantuffier.statemachine.orchestrate.OrchestratedPage
import com.jeantuffier.statemachine.orchestrate.Orchestration
import com.jeantuffier.statemachine.orchestrate.StateUpdater
import com.squareup.kotlinpoet.ClassName
import com.squareup.kotlinpoet.asClassName
import com.squareup.kotlinpoet.ksp.toClassName

/**
 * Checks if the extended type is [com.jeantuffier.statemachine.orchestrate.Orchestration]
 */
fun KSType.isOrchestration() = toClassName() == Orchestration::class.asClassName()

/**
 * Checks if the extended type is [com.jeantuffier.statemachine.orchestrate.Orchestrated]
 */
fun KSType.isOrchestrated() = toClassName() == Orchestrated::class.asClassName()

/**
 * Checks if the extended type is [com.jeantuffier.statemachine.orchestrate.Action]
 */
fun KSType.isAction() = toClassName() == Action::class.asClassName()

/**
 * Checks if the extended type is [com.jeantuffier.statemachine.orchestrate.OrchestratedData]
 */
fun KSType.isOrchestratedData() = toClassName() == OrchestratedData::class.asClassName()

/**
 * Checks if the extended type is [com.jeantuffier.statemachine.orchestrate.OrchestratedPage]
 */
fun KSType.isOrchestratedPage() = toClassName() == OrchestratedPage::class.asClassName()

/**
 * Returns the ClassName type of the first element in the arguments property.
 */
fun KSType.firstArgumentClassName() = arguments[0].type!!.resolve().toClassName()

/**
 * Returns the simpleName value of a type with its first character lowered.
 */
fun KSType.lowerCaseSimpleName(): String = toClassName().simpleName.replaceFirstChar(Char::lowercaseChar)

/**
 * Checks if the type is [com.jeantuffier.statemachine.orchestrate.Feature]
 */
fun KSType.isFeature() = toClassName() == Feature::class.asClassName()

/**
 * Checks if the type is [com.jeantuffier.statemachine.orchestrate.StateUpdater]
 */
fun KSType.isStateUpdater() = toClassName() == StateUpdater::class.asClassName()

/**
 * Checks if the type is com.jeantuffier.generator.With
 */
fun KSType.isWith() = toClassName() == ClassName(PACKAGE_NAME, "With")

/**
 * Checks if the type is com.jeantuffier.generator.With
 */
fun KSType.isUpdate() = toClassName() == ClassName(PACKAGE_NAME, "Update")
