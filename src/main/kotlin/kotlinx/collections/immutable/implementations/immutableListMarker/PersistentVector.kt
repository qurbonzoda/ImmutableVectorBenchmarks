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

package kotlinx.collections.immutable.implementations.immutableListMarker

import kotlinx.collections.immutable.ImmutableList

internal class PersistentVector<E>(private val root: Node,
                                   private val tail: Array<Any?>,
                                   override val size: Int,
                                   private val shiftStart: Int) : ImmutableList<E>, AbstractImmutableList<E>() {
    private fun rootSize(): Int {
        return ((size - 1) shr LOG_MAX_BUFFER_SIZE) shl LOG_MAX_BUFFER_SIZE
    }

    private fun bufferWith(e: Any?): Array<Any?> {
        val buffer = arrayOfNulls<Any?>(MAX_BUFFER_SIZE)
        buffer[0] = e
        return buffer
    }

    override fun add(element: E): ImmutableList<E> {
        val tailSize = size - rootSize()
        if (tailSize < MAX_BUFFER_SIZE) {
            val newTail = tail.copyOf(MAX_BUFFER_SIZE)
            newTail[tailSize] = element
            return PersistentVector(root, newTail, size + 1, shiftStart)
        }

        val newTail = bufferWith(element)
        return pushFullTail(root, tail, newTail)
    }

    private fun pushFullTail(root: Node, fullTail: Array<Any?>, newTail: Array<Any?>): PersistentVector<E> {
        if (size shr LOG_MAX_BUFFER_SIZE > 1 shl shiftStart) {
            var newRoot = Node(bufferWith(root), null)
            newRoot = pushTail(shiftStart + LOG_MAX_BUFFER_SIZE, newRoot, fullTail)
            return PersistentVector(newRoot, newTail, size + 1, shiftStart + LOG_MAX_BUFFER_SIZE)
        }

        val newRoot = pushTail(shiftStart, root, fullTail)
        return PersistentVector(newRoot, newTail, size + 1, shiftStart)
    }

    override fun removeAll(predicate: (E) -> Boolean): ImmutableList<E> {
        TODO("Not implemented")
    }

    override fun addAll(index: Int, c: Collection<E>): ImmutableList<E> {
        TODO("Not implemented")
    }

    override fun add(index: Int, element: E): ImmutableList<E> {
        TODO("Not implemented")
    }

    override fun removeAt(index: Int): ImmutableList<E> {
        TODO("Not implemented")
    }

    override fun subList(fromIndex: Int, toIndex: Int): ImmutableList<E> {
        TODO("Not implemented")
    }

    override fun builder(): ImmutableList.Builder<E> {
        TODO("Not implemented")
    }

    override fun indexOf(element: E): Int {
        TODO("Not implemented")
    }

    override fun lastIndexOf(element: E): Int {
        TODO("Not implemented")
    }

    override fun listIterator(index: Int): ListIterator<E> {
        TODO("Not implemented")
    }

    private fun pushTail(shift: Int, root: Node?, tail: Array<Any?>): Node {
        val index = ((size - 1) shr shift) and MAX_BUFFER_SIZE_MINUS_ONE
        val newRootBuffer = root?.buffer?.copyOf(MAX_BUFFER_SIZE) ?: arrayOfNulls<Any?>(MAX_BUFFER_SIZE)

        if (shift == LOG_MAX_BUFFER_SIZE) {
            newRootBuffer[index] = Node(tail, null)
        } else {
            newRootBuffer[index] = pushTail(shift - LOG_MAX_BUFFER_SIZE, newRootBuffer[index] as Node?, tail)
        }
        return Node(newRootBuffer, null)
    }

    private fun bufferFor(index: Int): Array<Any?> {
        if (rootSize() <= index) {
            return tail
        }
        var buffer = root.buffer
        var shift = shiftStart
        while (shift > 1) {
            buffer = (buffer[(index shr shift) and MAX_BUFFER_SIZE_MINUS_ONE] as Node).buffer
            shift -= LOG_MAX_BUFFER_SIZE
        }
        return buffer
    }

    override fun get(index: Int): E {
        if (index < 0 || index >= size) {
            throw IndexOutOfBoundsException()
        }
        val buffer = bufferFor(index)
        return buffer[index and MAX_BUFFER_SIZE_MINUS_ONE] as E
    }

    override fun set(index: Int, element: E): ImmutableList<E> {
        if (index < 0 || index >= size) {
            throw IndexOutOfBoundsException()
        }
        if (rootSize() <= index) {
            val newTail = tail.copyOf(MAX_BUFFER_SIZE)
            newTail[index and MAX_BUFFER_SIZE_MINUS_ONE] = element
            return PersistentVector(root, newTail, size, shiftStart)
        }

        val newRoot = setInRoot(root, shiftStart, index, element)
        return PersistentVector(newRoot, tail, size, shiftStart)
    }

    private fun setInRoot(root: Node, shift: Int, index: Int, e: Any?): Node {
        val bufferIndex = (index shr shift) and MAX_BUFFER_SIZE_MINUS_ONE
        val newRootBuffer = root.buffer.copyOf(MAX_BUFFER_SIZE)
        if (shift == 0) {
            newRootBuffer[bufferIndex] = e
        } else {
            newRootBuffer[bufferIndex] = setInRoot(newRootBuffer[bufferIndex] as Node,
                    shift - LOG_MAX_BUFFER_SIZE, index, e)
        }
        return Node(newRootBuffer, null)
    }
}