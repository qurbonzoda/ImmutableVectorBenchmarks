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

internal class PersistentVector<E>(private val rest: Array<Any?>,
                                   private val last: Array<E>,
                                   override val size: Int,
                                   private val shiftStart: Int) : ImmutableList<E>, AbstractImmutableList<E>() {
    override fun add(element: E): ImmutableList<E> {
        val lastSize = this.size - this.lastOff()
        if (lastSize < MAX_BUFFER_SIZE) {
            val newLast = this.last.copyOf()
            newLast[lastSize] = element
            return PersistentVector(this.rest, newLast, this.size + 1, this.shiftStart)
        }

        val newLast = this.bufferWithOnlyElement(element) as Array<E>
        return this.pushFullLast(this.rest, this.last, newLast)
    }

    private fun pushFullLast(rest: Array<Any?>, fullLast: Array<E>, newLast: Array<E>): PersistentVector<E> {
        if (this.size shr LOG_MAX_BUFFER_SIZE > 1 shl this.shiftStart) {
            var newRest = this.bufferWithOnlyElement(rest)
            newRest = pushLast(this.shiftStart + LOG_MAX_BUFFER_SIZE, newRest, fullLast)
            return PersistentVector(newRest, newLast, this.size + 1, this.shiftStart + LOG_MAX_BUFFER_SIZE)
        }

        val newRest = pushLast(this.shiftStart, rest, fullLast)
        return PersistentVector(newRest, newLast, this.size + 1, this.shiftStart)
    }

    private fun bufferWithOnlyElement(e: Any?): Array<Any?> {
        val buffer = arrayOfNulls<Any?>(MAX_BUFFER_SIZE)
        buffer[0] = e
        return buffer
    }

    override fun removeAll(predicate: (E) -> Boolean): ImmutableList<E> {
        return this.mutate { it.removeAll(predicate) }
    }

    override fun addAll(index: Int, c: Collection<E>): ImmutableList<E> {
        return this.mutate { it.addAll(index, c) }
    }

    override fun add(index: Int, element: E): ImmutableList<E> {
        if (index < 0 || index > this.size) {
            throw IndexOutOfBoundsException()
        }
        if (index == this.size) {
            return this.add(element)
        }

        val lastOff = this.lastOff()
        if (index >= lastOff) {
            return this.addInLast(this.rest, index - lastOff, element)
        }

        val lastElementWrapper = ObjectWrapper(null)
        val newRest = this.addInRest(this.rest, this.shiftStart, index, element, lastElementWrapper)
        return this.addInLast(newRest, 0, lastElementWrapper.value as E)
    }

    private fun addInLast(rest: Array<Any?>, index: Int, element: E): PersistentVector<E> {
        val lastFilledSize = this.size - this.lastOff()
        val newLast = this.last.copyOf()
        if (lastFilledSize < MAX_BUFFER_SIZE) {
            System.arraycopy(this.last, index, newLast, index + 1, lastFilledSize - index)
            newLast[index] = element
            return PersistentVector(rest, newLast, this.size + 1, this.shiftStart)
        }

        val lastValue = this.last.last()
        System.arraycopy(this.last, index, newLast, index + 1, lastFilledSize - index - 1)
        newLast[index] = element
        return this.pushFullLast(rest, newLast, this.bufferWithOnlyElement(lastValue) as Array<E>)
    }

    private fun addInRest(rest: Array<Any?>, shift: Int, index: Int, element: Any?, lastWrapper: ObjectWrapper): Array<Any?> {
        val bufferIndex = (index shr shift) and MAX_BUFFER_SIZE_MINUS_ONE

        if (shift == 0) {
            lastWrapper.value = rest[MAX_BUFFER_SIZE - 1]
            val newRest = if (bufferIndex == 0) arrayOfNulls<Any?>(MAX_BUFFER_SIZE) else rest.copyOf()
            System.arraycopy(rest, bufferIndex, newRest, bufferIndex + 1, MAX_BUFFER_SIZE - bufferIndex - 1)
            newRest[bufferIndex] = element
            return newRest
        }

        val newRest = rest.copyOf()
        newRest[bufferIndex] = addInRest(rest[bufferIndex] as Array<Any?>,
                shift - LOG_MAX_BUFFER_SIZE, index, element, lastWrapper)

        for (i in bufferIndex + 1 until MAX_BUFFER_SIZE) {
            if (newRest[i] == null) { break }
            newRest[i] = addInRest(rest[i] as Array<Any?>, shift - LOG_MAX_BUFFER_SIZE, 0, lastWrapper.value, lastWrapper)
        }

        return newRest
    }

    override fun removeAt(index: Int): ImmutableList<E> {
        if (index < 0 || index >= this.size) {
            throw IndexOutOfBoundsException()
        }
        if (index >= this.lastOff()) {
            return this.removeFromLast(this.rest, this.lastOff(), this.shiftStart, index - this.lastOff())
        }
        val lastElementWrapper = ObjectWrapper(this.last[0])
        val newRest = this.removeFromRest(this.rest, this.shiftStart, index, lastElementWrapper)
        return this.removeFromLast(newRest, this.lastOff(), this.shiftStart, 0)
    }

    private fun pullLastBufferFromRest(rest: Array<Any?>, restSize: Int, shift: Int): ImmutableList<E> {
        if (shift == 0) {
            return SmallPersistentVector(rest as Array<E>)
        }
        val lastBufferWrapper = ObjectWrapper(null)
        val newRest = this.pullLastBuffer(rest, shift, restSize - 1, lastBufferWrapper)!!
        val newLast = lastBufferWrapper.value as Array<E>

        if (newRest[1] == null) {
            return PersistentVector(newRest[0] as Array<Any?>, newLast, restSize, shift - LOG_MAX_BUFFER_SIZE)
        }
        return PersistentVector(newRest, newLast, restSize, shift)
    }

    private fun pullLastBuffer(rest: Array<Any?>, shift: Int, index: Int, lastWrapper: ObjectWrapper): Array<Any?>? {
        val bufferIndex = (index shr shift) and MAX_BUFFER_SIZE_MINUS_ONE

        if (shift == LOG_MAX_BUFFER_SIZE) {
            lastWrapper.value = rest[bufferIndex]
            if (bufferIndex == 0) {
                return null
            }
            val newRest = rest.copyOf()
            newRest[bufferIndex] = null
            return newRest
        }
        val bufferAtIndex = pullLastBuffer(rest[bufferIndex] as Array<Any?>, shift - LOG_MAX_BUFFER_SIZE, index, lastWrapper)
        if (bufferAtIndex == null && bufferIndex == 0) {
            return null
        }
        val newRest = rest.copyOf()
        newRest[bufferIndex] = bufferAtIndex
        return newRest
    }

    private fun removeFromLast(rest: Array<Any?>, restSize: Int, shift: Int, index: Int): ImmutableList<E> {
        val lastFilledSize = this.size - restSize
        assert(index < lastFilledSize)

        if (lastFilledSize == 1) {
            return pullLastBufferFromRest(rest, restSize, shift)
        }
        val newLast = this.last.copyOf() as Array<Any?>
        if (index < lastFilledSize - 1) {
            System.arraycopy(this.last, index + 1, newLast, index, lastFilledSize - index - 1)
        }
        newLast[lastFilledSize - 1] = null
        return PersistentVector(rest, newLast as Array<E>, restSize + lastFilledSize - 1, shift)
    }

    private fun removeFromRest(rest: Array<Any?>, shift: Int, index: Int, lastWrapper: ObjectWrapper): Array<Any?> {
        val bufferIndex = (index shr shift) and MAX_BUFFER_SIZE_MINUS_ONE

        if (shift == 0) {
            val newRest = if (bufferIndex == 0) arrayOfNulls<Any?>(MAX_BUFFER_SIZE) else rest.copyOf()
            System.arraycopy(rest, bufferIndex + 1, newRest, bufferIndex, MAX_BUFFER_SIZE - bufferIndex - 1)
            newRest[MAX_BUFFER_SIZE - 1] = lastWrapper.value
            lastWrapper.value = rest[0]
            return newRest
        }

        var bufferLastIndex = bufferIndex
        while (bufferLastIndex + 1 < MAX_BUFFER_SIZE && rest[bufferLastIndex + 1] != null) bufferLastIndex += 1  // TODO: optimize

        val newRest = rest.copyOf()
        for (i in bufferLastIndex downTo bufferIndex + 1) {
            newRest[i] = removeFromRest(newRest[i] as Array<Any?>, shift - LOG_MAX_BUFFER_SIZE, 0, lastWrapper)
        }
        newRest[bufferIndex] = removeFromRest(newRest[bufferIndex] as Array<Any?>, shift - LOG_MAX_BUFFER_SIZE, index, lastWrapper)

        return newRest
    }

    override fun subList(fromIndex: Int, toIndex: Int): ImmutableList<E> {
        throw AssertionError("Not implemented yet")
    }

    override fun builder(): ImmutableList.Builder<E> {
        return PersistentVectorBuilder(this.rest, this.last, this.size, this.shiftStart)
    }

    override fun indexOf(element: E): Int {
        val listIterator = this.listIterator()
        while (listIterator.hasNext()) {
            val nextIndex = listIterator.nextIndex()
            val next = listIterator.next()
            if ((next == null && element == null) || next?.equals(element) == true) {
                return nextIndex
            }
        }
        return -1
    }

    override fun lastIndexOf(element: E): Int {
        val listIterator = this.listIterator(this.size)
        while (listIterator.hasPrevious()) {
            val previousIndex = listIterator.previousIndex()
            val previous = listIterator.previous()
            if ((previous == null && element == null) || previous?.equals(element) == true) {
                return previousIndex
            }
        }
        return -1
    }

    override fun listIterator(index: Int): ListIterator<E> {
        return PersistentVectorIterator(this.rest, this.last, index, this.size, this.shiftStart / LOG_MAX_BUFFER_SIZE + 1)
    }

    private fun lastOff(): Int {
        return ((this.size - 1) shr LOG_MAX_BUFFER_SIZE) shl LOG_MAX_BUFFER_SIZE
    }

    private fun pushLast(shift: Int, rest: Array<Any?>?, last: Array<E>): Array<Any?> {
        val index = ((this.size - 1) shr shift) and MAX_BUFFER_SIZE_MINUS_ONE
        val newRestNode = rest?.copyOf() ?: arrayOfNulls<Any?>(MAX_BUFFER_SIZE)

        if (shift == LOG_MAX_BUFFER_SIZE) {
            newRestNode[index] = last
        } else {
            newRestNode[index] = pushLast(shift - LOG_MAX_BUFFER_SIZE, newRestNode[index] as Array<Any?>?, last)
        }
        return newRestNode
    }

    private fun bufferFor(index: Int): Array<E> {
        val lastOff = this.lastOff()
        if (lastOff <= index) {
            return this.last
        }
        var buffer = this.rest
        for (shift in this.shiftStart downTo 1 step LOG_MAX_BUFFER_SIZE) {
            buffer = buffer[(index shr shift) and MAX_BUFFER_SIZE_MINUS_ONE] as Array<Any?>
        }
        return buffer as Array<E>
    }

    override fun get(index: Int): E {
        if (index < 0 || index >= this.size) {
            throw IndexOutOfBoundsException()
        }
        val buffer = bufferFor(index)
        return buffer[index and MAX_BUFFER_SIZE_MINUS_ONE]
    }

    override fun set(index: Int, element: E): ImmutableList<E> {
        if (index < 0 || index >= this.size) {
            throw IndexOutOfBoundsException()
        }
        if (this.lastOff() <= index) {
            val newLast = this.last.copyOf()
            newLast[index and MAX_BUFFER_SIZE_MINUS_ONE] = element
            return PersistentVector(this.rest, newLast, this.size, this.shiftStart)
        }

        val newRest = setInRest(this.rest, this.shiftStart, index, element)
        return PersistentVector(newRest, this.last, this.size, this.shiftStart)
    }

    private fun setInRest(rest: Array<Any?>, shift: Int, index: Int, e: E): Array<Any?> {
        val bufferIndex = (index shr shift) and MAX_BUFFER_SIZE_MINUS_ONE
        val newRest = rest.copyOf()
        if (shift == 0) {
            newRest[bufferIndex] = e
        } else {
            newRest[bufferIndex] = setInRest(newRest[bufferIndex] as Array<Any?>,
                    shift - LOG_MAX_BUFFER_SIZE, index, e)
        }
        return newRest
    }
}
