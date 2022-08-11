package com.jeantuffier.statemachine.processor

import com.google.devtools.ksp.processing.*
import com.google.devtools.ksp.symbol.KSAnnotated
import com.google.devtools.ksp.symbol.KSClassDeclaration
import com.google.devtools.ksp.validate
import java.io.OutputStream

class ViewStateProcessor(
    private val codeGenerator: CodeGenerator,
    private val logger: KSPLogger,
) : SymbolProcessor {

    override fun process(resolver: Resolver): List<KSAnnotated> {
        val serializable = resolver
            .getSymbolsWithAnnotation("kotlinx.serialization.Serializable")
            .filterIsInstance<KSClassDeclaration>()

        println("serializable found: ${serializable.iterator().hasNext()}")
        if (!serializable.iterator().hasNext()) return emptyList()

        serializable.forEach {
            val file: OutputStream = codeGenerator.createNewFile(
                dependencies = Dependencies(false, *resolver.getAllFiles().toList().toTypedArray()),
                packageName = it.packageName.asString(),
                fileName = "${it.simpleName.getShortName()}Extensions"
            )
            file += "package metadata.commonMain.kotlin.${it.packageName.asString()}\n"
            it.accept(ViewStateVisitor(logger, file), Unit)
            file.close()
        }

        return serializable.filterNot { it.validate() }.toList()
    }
}
