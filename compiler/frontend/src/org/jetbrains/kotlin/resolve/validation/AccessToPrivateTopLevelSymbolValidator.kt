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

package org.jetbrains.kotlin.resolve.validation

import com.intellij.psi.PsiElement
import org.jetbrains.kotlin.descriptors.*
import org.jetbrains.kotlin.diagnostics.Errors
import org.jetbrains.kotlin.psi.JetFile
import org.jetbrains.kotlin.resolve.BindingTrace
import org.jetbrains.kotlin.resolve.DescriptorToSourceUtils
import org.jetbrains.kotlin.resolve.DescriptorUtils

public class AccessToPrivateTopLevelSymbolValidator : SymbolUsageValidator {
    override fun validateCall(targetDescriptor: CallableDescriptor, trace: BindingTrace, element: PsiElement) {
        val descriptor =
            if (DescriptorUtils.isTopLevelDeclaration(targetDescriptor)) targetDescriptor
            else DescriptorUtils.getContainingClass(targetDescriptor)

        descriptor?.let {
            reportIfNeeded(it, trace, element)
        }
    }

    override fun validateTypeUsage(targetDescriptor: ClassifierDescriptor, trace: BindingTrace, element: PsiElement) =
            reportIfNeeded(targetDescriptor, trace, element)

    private fun reportIfNeeded(descriptor: DeclarationDescriptor, trace: BindingTrace, element: PsiElement) {
        if (descriptor !is DeclarationDescriptorWithVisibility ||
            descriptor.visibility != Visibilities.PRIVATE ||
            !DescriptorUtils.isTopLevelDeclaration(descriptor)) return

        if (descriptor is PropertySetterDescriptor && descriptor.correspondingProperty.visibility == Visibilities.PRIVATE) return

        val elementFile = element.containingFile as? JetFile ?: return

        if (elementFile != DescriptorToSourceUtils.getContainingFile(descriptor)) {
            trace.report(Errors.INVISIBLE_FILE_MEMBER.on(element, descriptor, descriptor.visibility))
        }
    }
}
