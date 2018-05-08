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

internal class SmallPersistentVector<T>(private val buffer: Array<T>) : ImmutableList<T>, AbstractImmutableList<T>() {
    override val size: Int
        get() = this.buffer.size

    private fun bufferOfSize(size: Int): Array<T> {
        return arrayOfNulls<Any?>(size) as Array<T>
    }

    private fun copyBufferTo(newBuffer: Array<T>): Array<T> {
        System.arraycopy(this.buffer, 0, newBuffer, 0, this.size)
        return newBuffer
    }

    override fun add(element: T): ImmutableList<T> {
        if (this.size < MAX_BUFFER_SIZE) {
            val newBuffer = copyBufferTo(bufferOfSize(this.size + 1))
            newBuffer[this.size] = element
            return SmallPersistentVector(newBuffer)
        }
        val last = bufferOfSize(MAX_BUFFER_SIZE)
        last[0] = element
        return PersistentVector(this.buffer as Array<Any?>, last, this.size + 1, 0)
    }

    override fun addAll(elements: Collection<T>): ImmutableList<T> {
        if (this.size + elements.size < MAX_BUFFER_SIZE) {
            val newBuffer = copyBufferTo(bufferOfSize(this.size + elements.size))
            var index = this.size
            for (element in elements) {
                newBuffer[index++] = element
            }
            return SmallPersistentVector(newBuffer)
        }
        val builder = this.builder()
        builder.addAll(elements)
        return builder.build()
    }

    override fun removeAll(predicate: (T) -> Boolean): ImmutableList<T> {
        val newBuffer = this.buffer.copyOf()
        var newSize = 0
        for (element in this.buffer) {
            if (!predicate(element)) {
                newBuffer[newSize++] = element
            }
        }
        if (newSize == this.size) {
            return this
        }
        return SmallPersistentVector(newBuffer.copyOfRange(0, newSize))
    }

    override fun addAll(index: Int, c: Collection<T>): ImmutableList<T> {
        if (index < 0 || index > this.size) {
            throw IndexOutOfBoundsException()
        }
        if (this.size + c.size < MAX_BUFFER_SIZE) {
            val newBuffer = bufferOfSize(this.size + c.size)
            System.arraycopy(this.buffer, 0, newBuffer, 0, index)
            System.arraycopy(this.buffer, index, newBuffer, index + c.size, this.size)
            var position = index
            for (element in c) {
                newBuffer[position++] = element
            }
            return SmallPersistentVector(newBuffer)
        }
        val builder = this.builder()
        builder.addAll(c)
        return builder.build()
    }

    override fun add(index: Int, element: T): ImmutableList<T> {
        if (index < 0 || index > this.size) {
            throw IndexOutOfBoundsException()
        }
        if (index == this.size) {
            return this.add(element)
        }

        if (this.size < MAX_BUFFER_SIZE) {
            val newBuffer = bufferOfSize(this.size + 1)
            System.arraycopy(this.buffer, 0, newBuffer, 0, index)
            System.arraycopy(this.buffer, index, newBuffer, index + 1, this.size - index)
            newBuffer[index] = element
            return SmallPersistentVector(newBuffer)
        }

        val rest = this.buffer.copyOf()
        System.arraycopy(this.buffer, index, rest, index + 1, this.size - index - 1)
        rest[index] = element
        val last = arrayOfNulls<Any?>(MAX_BUFFER_SIZE)
        last[0] = this.buffer.last()
        return PersistentVector(rest as Array<Any?>, last as Array<T>, this.size + 1, 0)
    }

    override fun removeAt(index: Int): ImmutableList<T> {
        if (index < 0 || index > this.size) {
            throw IndexOutOfBoundsException()
        }
        if (this.size == 1) {
            return persistentVectorOf()
        }
        val newBuffer = bufferOfSize(this.size - 1)
        System.arraycopy(this.buffer, 0, newBuffer, 0, index)
        System.arraycopy(this.buffer, index + 1, newBuffer, index, this.size - index - 1)
        return SmallPersistentVector(newBuffer)
    }

    override fun subList(fromIndex: Int, toIndex: Int): ImmutableList<T> {
        return SmallPersistentVector(this.buffer.copyOfRange(fromIndex, toIndex))
    }

    override fun builder(): ImmutableList.Builder<T> {
        return PersistentVectorBuilder(null, this.buffer, this.size, 0)
    }

    override fun indexOf(element: T): Int {
        return this.buffer.indexOf(element)
    }

    override fun lastIndexOf(element: T): Int {
        return this.buffer.lastIndexOf(element)
    }

    override fun listIterator(index: Int): ListIterator<T> {
        return BufferIterator(this.buffer, index, this.size)
    }

    override fun get(index: Int): T {
        return this.buffer[index]
    }

    override fun set(index: Int, element: T): ImmutableList<T> {
        if (index >= this.size) {
            throw IndexOutOfBoundsException()
        }
        val newBuffer = this.buffer.copyOf()
        newBuffer[index] = element
        return SmallPersistentVector(newBuffer)
    }

    override fun iterator(): Iterator<T> {
        return this.buffer.iterator()
    }
}