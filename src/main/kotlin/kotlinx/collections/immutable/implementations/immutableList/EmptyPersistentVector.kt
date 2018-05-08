/*
 * Copyright 2016-2018 JetBrains s.r.o.
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

package kotlinx.collections.immutable.implementations.immutableList

import kotlinx.collections.immutable.ImmutableList
import kotlinx.collections.immutable.mutate
import java.util.*

internal object EmptyPersistentVector : ImmutableList<Any?>, AbstractImmutableList<Any?>() {
    override val size = 0

    override fun removeAll(predicate: (Any?) -> Boolean): ImmutableList<Any?> {
        return this
    }

    override fun addAll(index: Int, c: Collection<Any?>): ImmutableList<Any?> {
        if (index != 0) {
            throw IndexOutOfBoundsException()
        }
        return this.mutate { it.addAll(index, c) }
    }

    override fun add(index: Int, element: Any?): ImmutableList<Any?> {
        if (index != 0) {
            throw IndexOutOfBoundsException()
        }
        return SmallPersistentVector(arrayOf(element))
    }

    override fun removeAt(index: Int): ImmutableList<Any?> {
        throw IndexOutOfBoundsException()
    }

    override fun set(index: Int, element: Any?): ImmutableList<Any?> {
        throw IndexOutOfBoundsException()
    }

    override fun get(index: Int): Any? {
        throw IndexOutOfBoundsException()
    }

    override fun subList(fromIndex: Int, toIndex: Int): ImmutableList<Any?> {
        if (fromIndex == 0 && toIndex == 0) {
            return this
        }
        throw IndexOutOfBoundsException()
    }

    override fun builder(): ImmutableList.Builder<Any?> {
        return PersistentVectorBuilder(null, null, 0, 0)
    }

    override fun contains(element: Any?): Boolean {
        return false
    }

    override fun containsAll(elements: Collection<Any?>): Boolean {
        return elements.isEmpty()
    }

    override fun indexOf(element: Any?): Int {
        return -1
    }

    override fun lastIndexOf(element: Any?): Int {
        return -1
    }

    override fun listIterator(index: Int): ListIterator<Any?> {
        if (index != 0) {
            throw IndexOutOfBoundsException()
        }
        return Collections.emptyListIterator()
    }
}