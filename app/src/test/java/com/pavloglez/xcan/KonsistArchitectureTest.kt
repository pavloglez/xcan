package com.pavloglez.xcan

import com.lemonappdev.konsist.api.Konsist
import com.lemonappdev.konsist.api.ext.list.withNameEndingWith
import com.lemonappdev.konsist.api.verify.assertTrue
import org.junit.Test

class KonsistArchitectureTest {

    @Test
    fun `viewModels reside in presentation package`() {
        Konsist.scopeFromProject()
            .classes()
            .withNameEndingWith("ViewModel")
            .assertTrue {
                it.resideInPackage("..presentation..")
            }
    }

    @Test
    fun `viewModels only expose immutable StateFlow or MVI effect Flow`() {
        Konsist.scopeFromProject()
            .classes()
            .withNameEndingWith("ViewModel")
            .assertTrue { clazz ->
                val publicProperties = clazz.properties().filter { it.hasPublicOrDefaultModifier }
                publicProperties.all { prop ->
                    val typeName = prop.type?.name ?: ""
                    typeName.startsWith("StateFlow") || (prop.name == "effect" && typeName.startsWith("Flow"))
                }
            }
    }

    @Test
    fun `domain model layer does not import android framework classes`() {
        Konsist.scopeFromProduction()
            .files
            .filter { it.packagee?.name?.contains("model") == true || it.packagee?.name?.contains("domain") == true }
            .assertTrue { file ->
                file.imports.none { it.name.startsWith("android.") || it.name.startsWith("androidx.") }
            }
    }

    @Test
    fun `mutable state flow properties are private`() {
        Konsist.scopeFromProject()
            .properties()
            .filter { it.type?.name == "MutableStateFlow" || it.type?.hasNameStartingWith("MutableStateFlow<") == true }
            .assertTrue { it.hasPrivateModifier }
    }
}
