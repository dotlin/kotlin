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

package org.jetbrains.kotlin.load.kotlin

import com.intellij.openapi.components.ServiceManager
import com.intellij.openapi.project.Project
import com.intellij.openapi.vfs.StandardFileSystems
import com.intellij.openapi.vfs.VfsUtilCore
import org.jetbrains.kotlin.descriptors.DeclarationDescriptor
import org.jetbrains.kotlin.descriptors.PackageFragmentDescriptor
import org.jetbrains.kotlin.descriptors.PackageViewDescriptor
import org.jetbrains.kotlin.load.java.lazy.descriptors.LazyJavaPackageFragment
import org.jetbrains.kotlin.load.kotlin.incremental.IncrementalPackageFragmentProvider
import org.jetbrains.kotlin.modules.Module
import org.jetbrains.kotlin.resolve.DescriptorUtils
import org.jetbrains.kotlin.resolve.source.KotlinSourceElement
import org.jetbrains.kotlin.util.ModuleVisibilityHelper
import java.io.File

public fun isContainedByCompiledPartOfOurModule(descriptor: DeclarationDescriptor, outDirectory: File?): Boolean {
    val packageFragment = DescriptorUtils.getParentOfType(descriptor, PackageFragmentDescriptor::class.java, false)

    if (packageFragment is IncrementalPackageFragmentProvider.IncrementalPackageFragment) return true

    if (outDirectory == null || packageFragment !is LazyJavaPackageFragment) return false

    val source = DescriptorUtils.getSourceElement(descriptor)
    if (source is KotlinJvmBinarySourceElement) {
        val binaryClass = source.binaryClass
        if (binaryClass is VirtualFileKotlinClass) {
            val file = binaryClass.file
            if (file.fileSystem.protocol == StandardFileSystems.FILE_PROTOCOL) {
                val ioFile = VfsUtilCore.virtualToIoFile(file)
                return ioFile.absolutePath.startsWith(outDirectory.absolutePath + File.separator);
            }
        }
    }

    return false;
}

class ModuleVisibilityHelperImpl: ModuleVisibilityHelper {

    override fun isInFriendModule(what: DeclarationDescriptor, from: DeclarationDescriptor): Boolean {
        val fromOrModule = if (from is PackageViewDescriptor) from.module else from
        if (!DescriptorUtils.isInFriendModule(what, fromOrModule)) return false

        val fromSource = DescriptorUtils.getSourceElement(from)
        if (fromSource !is KotlinSourceElement) return true

        val project = fromSource.psi.project
        val moduleVisibilityManager = ModuleVisibilityManager.SERVICE.getInstance(project)

        val whatSource = DescriptorUtils.getSourceElement(what)
        if (whatSource is KotlinSourceElement) return true

        val outputDirectories = moduleVisibilityManager.chunk.map { File(it.getOutputDirectory()) }
        if (outputDirectories.isEmpty()) return isContainedByCompiledPartOfOurModule(what, null)

        outputDirectories.forEach {
            if (isContainedByCompiledPartOfOurModule(what, it)) return true
        }

        return false
    }
}

interface ModuleVisibilityManager {
     val chunk: MutableList<Module>
    fun addModule(module: Module)

    public object SERVICE {
        @JvmStatic
        public fun getInstance(project: Project): ModuleVisibilityManager =
                ServiceManager.getService(project, ModuleVisibilityManager::class.java)
    }

}

class ModuleVisibilityManagerImpl() : ModuleVisibilityManager {
    override val chunk: MutableList<Module> = arrayListOf()

    override fun addModule(module: Module) {
        chunk.add(module)
    }
}