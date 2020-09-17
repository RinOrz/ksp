/*
 * Copyright 2010-2020 Google LLC, JetBrains s.r.o and and Kotlin Programming Language contributors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */


package com.google.devtools.ksp.processor

import com.google.devtools.ksp.processing.Resolver
import com.google.devtools.ksp.symbol.KSClassDeclaration
import com.google.devtools.ksp.symbol.KSFile
import com.google.devtools.ksp.symbol.KSFunctionDeclaration
import com.google.devtools.ksp.symbol.KSVisitorVoid

class AllFunctionsProcessor : AbstractTestProcessor() {
    val results = mutableListOf<String>()
    val visitor = AllFunctionsVisitor()

    override fun toResult(): List<String> {
        val finalResult = mutableListOf(results[0])
        finalResult.addAll(results.subList(1, results.size).sorted())
        return finalResult
    }

    override fun process(resolver: Resolver) {
        resolver.getAllFiles().map { it.accept(visitor, Unit) }
    }

    inner class AllFunctionsVisitor : KSVisitorVoid() {
        fun KSFunctionDeclaration.toSignature(): String {
            return "${this.simpleName.asString()}" +
                    "(${this.parameters.map { 
                        buildString {
                            append(it.type?.resolve()?.declaration?.qualifiedName?.asString())
                            if (it.hasDefault) {
                                append("(hasDefault)")
                            }
                        }
                    }.joinToString(",")})" +
                    ": ${this.returnType?.resolve()?.declaration?.qualifiedName?.asString() ?: ""}"
        }

        override fun visitClassDeclaration(classDeclaration: KSClassDeclaration, data: Unit) {
            results.add("class: ${classDeclaration.simpleName.asString()}")
            classDeclaration.getAllFunctions().map { it.accept(this, Unit) }
        }

        override fun visitFunctionDeclaration(function: KSFunctionDeclaration, data: Unit) {
            results.add(function.toSignature())
        }

        override fun visitFile(file: KSFile, data: Unit) {
            file.declarations.map { it.accept(this, Unit) }
        }
    }
}