/*
 * Copyright 2010-2015 JetBrains s.r.o.
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

package org.jetbrains.kotlin.idea.conversion.copy

import com.intellij.openapi.actionSystem.IdeActions
import org.jetbrains.kotlin.idea.AbstractCopyPasteTest
import org.jetbrains.kotlin.idea.test.KotlinWithJdkAndRuntimeLightProjectDescriptor
import org.jetbrains.kotlin.idea.test.PluginTestCaseBase
import org.jetbrains.kotlin.idea.editor.KotlinEditorOptions
import org.jetbrains.kotlin.test.InTextDirectivesUtils
import org.jetbrains.kotlin.test.KotlinTestUtils
import java.io.File
import kotlin.test.assertEquals

abstract class AbstractJavaToKotlinCopyPasteConversionTest : AbstractCopyPasteTest() {
    private val BASE_PATH = PluginTestCaseBase.getTestDataPathBase() + "/copyPaste/conversion"

    private var oldEditorOptions: KotlinEditorOptions? = null

    override fun getTestDataPath() = BASE_PATH

    override fun getProjectDescriptor() = KotlinWithJdkAndRuntimeLightProjectDescriptor.INSTANCE

    override fun setUp() {
        super.setUp()
        oldEditorOptions = KotlinEditorOptions.getInstance().state
        KotlinEditorOptions.getInstance().isEnableJavaToKotlinConversion = true
        KotlinEditorOptions.getInstance().isDonTShowConversionDialog = true
    }

    override fun tearDown() {
        KotlinEditorOptions.getInstance().loadState(oldEditorOptions)
        super.tearDown()
    }

    fun doTest(path: String) {
        myFixture.testDataPath = BASE_PATH
        val testName = getTestName(false)
        myFixture.configureByFiles(testName + ".java")

        val fileText = myFixture.editor.document.text
        val noConversionExpected = InTextDirectivesUtils.findListWithPrefixes(fileText, "// NO_CONVERSION_EXPECTED").isNotEmpty()

        myFixture.performEditorAction(IdeActions.ACTION_COPY)

        configureByDependencyIfExists(testName + ".dependency.kt")
        configureByDependencyIfExists(testName + ".dependency.java")

        configureTargetFile(testName + ".to.kt")

        ConvertJavaCopyPastePostProcessor.conversionPerformed = false

        myFixture.performEditorAction(IdeActions.ACTION_PASTE)

        assertEquals(noConversionExpected, !ConvertJavaCopyPastePostProcessor.conversionPerformed,
        if (noConversionExpected) "Conversion to Kotlin should not be suggested" else "No conversion to Kotlin suggested")

        KotlinTestUtils.assertEqualsToFile(File(path.replace(".java", ".expected.kt")), myFixture.file.text)
    }
}
