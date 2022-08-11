package com.jeantuffier.statemachine.processor

import com.google.devtools.ksp.closestClassDeclaration
import com.google.devtools.ksp.processing.KSPLogger
import com.google.devtools.ksp.symbol.*
import java.io.OutputStream

class ViewStateVisitor(
    private val logger: KSPLogger,
    private val file: OutputStream,
) : KSVisitorVoid() {

    override fun visitClassDeclaration(classDeclaration: KSClassDeclaration, data: Unit) {
        if (invalidClass(classDeclaration)) return
        val qualifiedClassName: String = qualifiedName(classDeclaration) ?: return

        file += "\n"
        file += "import $qualifiedClassName\n"

        val entries = entries(classDeclaration)
        val className = classDeclaration.simpleName.getShortName()
        generateFromExtension(className, entries)
        generateToExtension(className, entries)
    }

    private fun invalidClass(classDeclaration: KSClassDeclaration) =
        classDeclaration.classKind != ClassKind.ENUM_CLASS

    private fun qualifiedName(classDeclaration: KSClassDeclaration): String? =
        (classDeclaration.qualifiedName?.asString()).also {
            if (it == null) logger.error("$classDeclaration does not have a qualified name", classDeclaration)
        }

    private fun entries(classDeclaration: KSClassDeclaration): Map<KSClassDeclaration, String> =
        classDeclaration.declarations.mapNotNull { classToValue(it) }.toMap()

    private fun classToValue(declaration: KSDeclaration): Pair<KSClassDeclaration, String>? =
        declaration.closestClassDeclaration()?.let { classToValue(it) }

    private fun classToValue(classDeclaration: KSClassDeclaration): Pair<KSClassDeclaration, String>? =
        if (classDeclaration.classKind == ClassKind.ENUM_ENTRY) {
            val annotation: KSAnnotation? = serialNameAnnotation(classDeclaration)
            val value: String? = annotationValue(annotation)
            if (value == null) null else Pair(classDeclaration, value)
        } else null

    private fun serialNameAnnotation(classDeclaration: KSClassDeclaration): KSAnnotation? =
        classDeclaration.annotations.firstOrNull { it.shortName.asString() == "SerialName" }

    private fun annotationValue(annotation: KSAnnotation?): String? =
        annotation?.arguments
            ?.firstOrNull { arg -> arg.name?.asString() == "value" }
            ?.value as String?

    private fun generateFromExtension(
        className: String,
        properties: Map<KSClassDeclaration, String>,
    ) {
        file += "\n"
        file += "fun $className.Companion.fromSerialName(name: String): $className = when (name) {\n"
        properties.forEach { property ->
            file += "    \"${property.value}\" -> $className.${property.key.simpleName.getShortName()}\n"
        }
        file += "    else -> throw Exception(\"Unsupported serial name \$name\")"
        file += "\n}\n"
    }

    private fun generateToExtension(
        className: String,
        properties: Map<KSClassDeclaration, String>,
    ) {
        file += "\n"
        file += "fun $className.toSerialName(): String = when (this) {\n"
        properties.forEach { property ->
            file += "    $className.${property.key.simpleName.getShortName()} -> \"${property.value}\"\n"
        }
        file += "}\n"
    }
}
