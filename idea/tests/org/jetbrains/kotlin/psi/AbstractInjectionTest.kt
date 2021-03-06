/*
 * Copyright 2010-2016 JetBrains s.r.o.
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

package org.jetbrains.kotlin.psi

import com.intellij.injected.editor.EditorWindow
import com.intellij.psi.injection.Injectable
import com.intellij.testFramework.LightProjectDescriptor
import junit.framework.TestCase
import org.intellij.plugins.intelliLang.Configuration
import org.intellij.plugins.intelliLang.inject.InjectLanguageAction
import org.intellij.plugins.intelliLang.inject.UnInjectLanguageAction
import org.intellij.plugins.intelliLang.references.FileReferenceInjector
import org.jetbrains.kotlin.idea.test.KotlinLightCodeInsightFixtureTestCase
import org.jetbrains.kotlin.idea.test.KotlinLightCodeInsightFixtureTestCaseBase
import org.jetbrains.kotlin.idea.test.KotlinLightProjectDescriptor
import org.jetbrains.kotlin.idea.test.KotlinWithJdkAndRuntimeLightProjectDescriptor

abstract class AbstractInjectionTest : KotlinLightCodeInsightFixtureTestCase() {
    override fun getProjectDescriptor(): LightProjectDescriptor {
        val testName = getTestName(true)
        return when {
            testName.endsWith("WithAnnotation") -> KotlinLightProjectDescriptor.INSTANCE
            testName.endsWith("WithRuntime") -> KotlinWithJdkAndRuntimeLightProjectDescriptor.INSTANCE
            else -> KotlinLightCodeInsightFixtureTestCaseBase.JAVA_LATEST
        }
    }

    protected fun assertInjectionPresent(text: String, languageId: String? = null, unInjectShouldBePresent: Boolean = true) {
        myFixture.configureByText("${getTestName(true)}.kt", text.trimMargin())

        TestCase.assertFalse("Injection action is available. There's probably no injection at caret place",
                             InjectLanguageAction().isAvailable(project, myFixture.editor, myFixture.file))

        if (languageId != null) {
            val injectedFile = (editor as? EditorWindow)?.injectedFile
            KotlinLightCodeInsightFixtureTestCaseBase.assertEquals("Wrong injection language", languageId, injectedFile?.language?.id)
        }

        if (unInjectShouldBePresent) {
            TestCase.assertTrue("UnInjection action is not available. There's no injection at caret place or some other troubles.",
                                UnInjectLanguageAction().isAvailable(project, myFixture.editor, myFixture.file))
        }
    }

    protected fun assertNoInjection(text: String) {
        myFixture.configureByText("${getTestName(true)}.kt", text.trimMargin())

        TestCase.assertTrue("Injection action is not available. There's probably some injection but nothing was expected.",
                            InjectLanguageAction().isAvailable(project, myFixture.editor, myFixture.file))
    }

    protected fun doRemoveInjectionTest(before: String, after: String) {
        myFixture.setCaresAboutInjection(false)

        myFixture.configureByText("${getTestName(true)}.kt", before.trimMargin())

        TestCase.assertTrue(UnInjectLanguageAction().isAvailable(project, myFixture.editor, myFixture.file))
        UnInjectLanguageAction.invokeImpl(project, myFixture.editor, myFixture.file)

        myFixture.checkResult(after.trimMargin())
    }

    protected fun doFileReferenceInjectTest(before: String, after: String) {
        doTest(FileReferenceInjector(), before, after)
    }

    protected fun doTest(injectable: Injectable, before: String, after: String) {
        val configuration = Configuration.getProjectInstance(project).advancedConfiguration
        val allowed = configuration.isSourceModificationAllowed

        configuration.isSourceModificationAllowed = true
        try {
            myFixture.configureByText("${getTestName(true)}.kt", before.trimMargin())
            InjectLanguageAction.invokeImpl(project, myFixture.editor, myFixture.file, injectable)
            myFixture.checkResult(after.trimMargin())
        }
        finally {
            configuration.isSourceModificationAllowed = allowed
        }
    }
}
