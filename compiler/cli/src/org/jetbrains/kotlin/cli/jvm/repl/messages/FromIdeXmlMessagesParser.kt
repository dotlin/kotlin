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

package org.jetbrains.kotlin.cli.jvm.repl.messages

import com.intellij.openapi.util.text.StringUtil
import org.w3c.dom.Element
import org.xml.sax.InputSource
import java.io.ByteArrayInputStream
import javax.xml.parsers.DocumentBuilderFactory
import kotlin.platform.platformStatic

public object FromIdeXmlMessagesParser {
    private fun strToSource(s: String) = InputSource(ByteArrayInputStream(s.toByteArray()))

    platformStatic fun parse(inputMessage: String): String {
        val docFactory = DocumentBuilderFactory.newInstance()
        val docBuilder = docFactory.newDocumentBuilder()
        val input = docBuilder.parse(strToSource(inputMessage))

        val root = input.firstChild as Element
        val inputContent = StringUtil.unescapeStringCharacters(root.textContent).trim()

        return inputContent
    }
}