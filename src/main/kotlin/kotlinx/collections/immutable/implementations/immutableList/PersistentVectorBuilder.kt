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
import java.util.concurrent.atomic.AtomicReference

private class Marker

class PersistentVectorBuilder<E>(private var rest: Array<Any?>?,
                                 private var last: Array<E>?,
                                 override var size: Int,
                                 private var shiftStart: Int) : AbstractMutableList<E>(), ImmutableList.Builder<E> {
    private var marker = Marker()

    override fun build(): ImmutableList<E> {
        marker = Marker()
        if (rest == null) {
            if (last == null) {
                return persistentVectorOf()
            }
            return SmallPersistentVector(last!!.copyOf(size) as Array<E>)
        }
        return PersistentVector(rest!!, last!!, size, shiftStart)
    }

    private fun lastOff(): Int {
        if (size <= MAX_BUFFER_SIZE) {
            return 0
        }
        return ((size - 1) shr LOG_MAX_BUFFER_SIZE) shl LOG_MAX_BUFFER_SIZE
    }

    private fun <T> makeMutable(buffer: Array<T>?): Array<T> {
        if (buffer == null) {
            val newBuffer = arrayOfNulls<Any?>(MAX_BUFFER_SIZE_PlUS_ONE)
            newBuffer[MAX_BUFFER_SIZE] = marker
            return newBuffer as Array<T>
        }
        if (buffer.size != MAX_BUFFER_SIZE_PlUS_ONE || buffer[MAX_BUFFER_SIZE] !== marker) {
            val newBuffer = arrayOfNulls<Any?>(MAX_BUFFER_SIZE_PlUS_ONE)
            System.arraycopy(buffer, 0, newBuffer, 0, minOf(buffer.size, MAX_BUFFER_SIZE))
            newBuffer[MAX_BUFFER_SIZE] = marker
            return newBuffer as Array<T>
        }
        return buffer
    }

    private fun <T> mutableBufferWith(element: T): Array<T> {
        val buffer = arrayOfNulls<Any?>(MAX_BUFFER_SIZE_PlUS_ONE)
        buffer[0] = element
        buffer[MAX_BUFFER_SIZE] = marker
        return buffer as Array<T>
    }

    override fun add(element: E): Boolean {
        modCount += 1

        val lastSize = size - lastOff()
        if (lastSize < MAX_BUFFER_SIZE) {
            val mutableLast = makeMutable(last)
            mutableLast[lastSize] = element
            this.last = mutableLast
            this.size += 1
        } else {
            val newLast = mutableBufferWith(element)
            this.pushFullLast(rest, last!!, newLast)
        }
        return true
    }

    private fun pushFullLast(rest: Array<Any?>?, fullLast: Array<E>, newLast: Array<E>) {
        if (size shr LOG_MAX_BUFFER_SIZE > 1 shl shiftStart) {
            var mutableRest = mutableBufferWith(rest) as Array<Any?>
            mutableRest = pushLast(mutableRest, fullLast, shiftStart + LOG_MAX_BUFFER_SIZE)
            this.rest = mutableRest
            this.last = newLast
            this.shiftStart += LOG_MAX_BUFFER_SIZE
            this.size += 1
            return
        } else if (rest == null) {
            this.rest = fullLast as Array<Any?>
            this.last = newLast
            this.size += 1
            return
        }

        val newRest = pushLast(rest, fullLast, shiftStart)
        this.rest = newRest
        this.last = newLast
        this.size += 1
    }

    private fun pushLast(rest: Array<Any?>?, last: Array<E>, shift: Int): Array<Any?> {
        val index = ((size - 1) shr shift) and MAX_BUFFER_SIZE_MINUS_ONE
        val mutableRest = makeMutable(rest)

        if (shift == LOG_MAX_BUFFER_SIZE) {
            mutableRest[index] = last
        } else {
            mutableRest[index] = pushLast(mutableRest[index] as Array<Any?>?, last, shift - LOG_MAX_BUFFER_SIZE)
        }
        return mutableRest
    }

    override fun add(index: Int, element: E) {
        if (index < 0 || index > size) {
            throw IndexOutOfBoundsException()
        }
        if (index == size) {
            add(element)
            return
        }

        modCount += 1

        val lastOff = this.lastOff()
        if (index >= lastOff) {
            this.addInLast(rest, index - lastOff, element)
            return
        }

        val pair = this.addInRest(rest!!, this.shiftStart, index, element)
        this.addInLast(pair.first, 0, pair.second as E)
    }


    private fun addInLast(rest: Array<Any?>?, index: Int, element: E) {
        val lastFilledSize = this.size - this.lastOff()
        val mutableLast = makeMutable(last)
        if (lastFilledSize < MAX_BUFFER_SIZE) {
            System.arraycopy(last, index, mutableLast, index + 1, lastFilledSize - index)
            mutableLast[index] = element
            this.rest = rest
            this.last = mutableLast
            this.size += 1
        } else {
            val lastElement = last!![MAX_BUFFER_SIZE_MINUS_ONE]
            System.arraycopy(last, index, mutableLast, index + 1, MAX_BUFFER_SIZE_MINUS_ONE - index)
            mutableLast[index] = element
            pushFullLast(rest, mutableLast, mutableBufferWith(lastElement))
        }
    }

    private fun addInRest(rest: Array<Any?>, shift: Int, index: Int, element: Any?): Pair<Array<Any?>, Any?> {
        val bufferIndex = (index shr shift) and MAX_BUFFER_SIZE_MINUS_ONE

        if (shift == 0) {
            val lastElement = rest[MAX_BUFFER_SIZE_MINUS_ONE]
            val mutableRest = makeMutable(rest)
            System.arraycopy(rest, bufferIndex, mutableRest, bufferIndex + 1, MAX_BUFFER_SIZE_MINUS_ONE - bufferIndex)
            mutableRest[bufferIndex] = element
            return Pair(mutableRest, lastElement)
        }

        val mutableRest = makeMutable(rest)
        var pair = addInRest(mutableRest[bufferIndex] as Array<Any?>, shift - LOG_MAX_BUFFER_SIZE, index, element)
        mutableRest[bufferIndex] = pair.first

        for (i in bufferIndex + 1 until MAX_BUFFER_SIZE) {
            if (mutableRest[i] == null) { break }
            pair = addInRest(mutableRest[i] as Array<Any?>, shift - LOG_MAX_BUFFER_SIZE, 0, pair.second)
            mutableRest[i] = pair.first
        }

        return Pair(mutableRest, pair.second)
    }

    override fun get(index: Int): E {
        if (index < 0 || index >= size) {
            throw IndexOutOfBoundsException()
        }
        val buffer = bufferFor(index)
        return buffer[index and MAX_BUFFER_SIZE_MINUS_ONE]
    }

    private fun bufferFor(index: Int): Array<E> {
        val lastOff = this.lastOff()
        if (lastOff <= index) {
            return last!!
        }
        var buffer = rest!!
        for (shift in this.shiftStart downTo 1 step LOG_MAX_BUFFER_SIZE) {
            buffer = buffer[(index shr shift) and MAX_BUFFER_SIZE_MINUS_ONE] as Array<Any?>
        }
        return buffer as Array<E>
    }

    override fun removeAt(index: Int): E {
        if (index < 0 || index >= size) {
            throw IndexOutOfBoundsException()
        }
        modCount += 1

        if (index >= lastOff()) {
            return removeFromLast(rest, lastOff(), shiftStart, index - lastOff())
        }
        val pair = removeFromRest(rest!!, shiftStart, index, last!![0])
        removeFromLast(pair.first, this.lastOff(), this.shiftStart, 0)
        return pair.second as E
    }

    private fun removeFromLast(rest: Array<Any?>?, restSize: Int, shift: Int, index: Int): E {
        val lastFilledSize = size - restSize
        assert(index < lastFilledSize)

        val removedElement: E
        if (lastFilledSize == 1) {
            removedElement = last!![0]
            pullLastBufferFromRest(rest, restSize, shift)
        } else {
            removedElement = last!![index]
            val mutableLast = makeMutable(last)
            System.arraycopy(last, index + 1, mutableLast, index, lastFilledSize - index - 1)
            mutableLast[lastFilledSize - 1] = null as E
            this.rest = rest
            this.last = mutableLast
            this.size = restSize + lastFilledSize - 1
            this.shiftStart = shift
        }
        return removedElement
    }

    private fun removeFromRest(rest: Array<Any?>, shift: Int, index: Int, lastElement: E): Pair<Array<Any?>, Any?> {
        val bufferIndex = (index shr shift) and MAX_BUFFER_SIZE_MINUS_ONE

        if (shift == 0) {
            val removedElement = rest[bufferIndex]
            val mutableRest = makeMutable(rest)
            System.arraycopy(rest, bufferIndex + 1, mutableRest, bufferIndex, MAX_BUFFER_SIZE - bufferIndex - 1)
            mutableRest[MAX_BUFFER_SIZE - 1] = lastElement
            return Pair(mutableRest, removedElement)
        }

        var bufferLastIndex = bufferIndex
        while (bufferLastIndex + 1 < MAX_BUFFER_SIZE && rest[bufferLastIndex + 1] != null) bufferLastIndex += 1  // TODO: optimize
        val mutableRest = makeMutable(rest)

        var pair = Pair(mutableRest, lastElement as Any?)

        for (i in bufferLastIndex downTo bufferIndex + 1) {
            pair = removeFromRest(mutableRest[i] as Array<Any?>, shift - LOG_MAX_BUFFER_SIZE, 0, pair.second as E)
            mutableRest[i] = pair.first
        }

        pair = removeFromRest(mutableRest[bufferIndex] as Array<Any?>, shift - LOG_MAX_BUFFER_SIZE, index, pair.second as E)
        mutableRest[bufferIndex] = pair.first

        return Pair(mutableRest, pair.second)
    }

    private fun pullLastBufferFromRest(rest: Array<Any?>?, restSize: Int, shift: Int) {
        if (shift == 0) {
            this.rest = null
            this.last = rest as Array<E>?
            this.size = restSize
            this.shiftStart = shift
            return
        }

        val pair = this.pullLastBuffer(rest!!, shift, restSize)
        val newRest = pair.first!!
        val newLast = pair.second as Array<E>
        if (newRest[1] == null) {
            this.rest = newRest[0] as Array<Any?>?
            this.last = newLast
            this.size = restSize
            this.shiftStart = shift - LOG_MAX_BUFFER_SIZE
            return
        }
        this.rest = newRest
        this.last = newLast
        this.size = restSize
        this.shiftStart = shift
    }

    private fun pullLastBuffer(rest: Array<Any?>, shift: Int, restSize: Int): Pair<Array<Any?>?, Array<Any?>> {
        val bufferIndex = ((restSize - 1) shr shift) and MAX_BUFFER_SIZE_MINUS_ONE

        if (shift == LOG_MAX_BUFFER_SIZE) {
            val lastBuffer = rest[bufferIndex] as Array<Any?>
            if (bufferIndex == 0) {
                return Pair(null, lastBuffer)
            }
            val mutableRest = makeMutable(rest)
            mutableRest[bufferIndex] = null
            return Pair(mutableRest, lastBuffer)
        }
        val pair = pullLastBuffer(rest[bufferIndex] as Array<Any?>, shift - LOG_MAX_BUFFER_SIZE, restSize)
        if (pair.first == null && bufferIndex == 0) {
            return Pair(null, pair.second)
        }
        val mutableRest = makeMutable(rest)
        mutableRest[bufferIndex] = pair.first
        return Pair(mutableRest, pair.second)
    }

    override fun set(index: Int, element: E): E {
        if (index < 0 || index >= size) {
            throw IndexOutOfBoundsException()
        }
        if (lastOff() <= index) {
            val mutableLast = makeMutable(last)
            val oldElement = mutableLast[index and MAX_BUFFER_SIZE_MINUS_ONE]
            mutableLast[index and MAX_BUFFER_SIZE_MINUS_ONE] = element
            this.last = mutableLast
            return oldElement
        }

        val oldElement = ObjectWrapper(null)
        this.rest = setInRest(rest!!, shiftStart, index, element, oldElement)
        return oldElement.value as E
    }

    private fun setInRest(rest: Array<Any?>, shift: Int, index: Int, e: E, oldElement: ObjectWrapper): Array<Any?> {
        val bufferIndex = (index shr shift) and MAX_BUFFER_SIZE_MINUS_ONE
        val mutableRest = makeMutable(rest)

        if (shift == 0) {
            oldElement.value = mutableRest[bufferIndex]
            mutableRest[bufferIndex] = e
            return mutableRest
        }
        mutableRest[bufferIndex] =
                setInRest(mutableRest[bufferIndex] as Array<Any?>,shift - LOG_MAX_BUFFER_SIZE, index, e, oldElement)
        return mutableRest
    }
}
