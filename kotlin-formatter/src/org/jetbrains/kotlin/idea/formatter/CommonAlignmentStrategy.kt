// Copyright 2000-2022 JetBrains s.r.o. and contributors. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.

package org.jetbrains.kotlin.idea.formatter

import com.intellij.formatting.Alignment
import com.intellij.lang.ASTNode

abstract class CommonAlignmentStrategy {
    abstract fun getAlignment(node: ASTNode): Alignment?
}