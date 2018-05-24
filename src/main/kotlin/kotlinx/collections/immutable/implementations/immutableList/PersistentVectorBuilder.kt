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

        val lastElementWrapper = ObjectWrapper(null)
        val newRest = this.addInRest(rest!!, this.shiftStart, index, element, lastElementWrapper)
        this.addInLast(newRest, 0, lastElementWrapper.value as E)
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

    private fun addInRest(rest: Array<Any?>, shift: Int, index: Int, element: Any?, lastWrapper: ObjectWrapper): Array<Any?> {
        val bufferIndex = (index shr shift) and MAX_BUFFER_SIZE_MINUS_ONE

        if (shift == 0) {
            lastWrapper.value = rest[MAX_BUFFER_SIZE_MINUS_ONE]
            val mutableRest = makeMutable(rest)
            System.arraycopy(rest, bufferIndex, mutableRest, bufferIndex + 1, MAX_BUFFER_SIZE_MINUS_ONE - bufferIndex)
            mutableRest[bufferIndex] = element
            return mutableRest
        }

        val mutableRest = makeMutable(rest)
        mutableRest[bufferIndex] = addInRest(mutableRest[bufferIndex] as Array<Any?>, shift - LOG_MAX_BUFFER_SIZE, index, element, lastWrapper)

        for (i in bufferIndex + 1 until MAX_BUFFER_SIZE) {
            if (mutableRest[i] == null) { break }
            mutableRest[i] = addInRest(mutableRest[i] as Array<Any?>, shift - LOG_MAX_BUFFER_SIZE, 0, lastWrapper.value, lastWrapper)
        }

        return mutableRest
    }

    override fun get(index: Int): E {
        if (index < 0 || index >= size) {
            throw IndexOutOfBoundsException()
        }
        val buffer = bufferFor(index)
        return buffer[index and MAX_BUFFER_SIZE_MINUS_ONE]
    }

    private fun bufferFor(index: Int): Array<E> {
        if (this.lastOff() <= index) {
            return last!!
        }
        var buffer = rest!!
        var shift = shiftStart
        while (shift > 1) {
            buffer = buffer[(index shr shift) and MAX_BUFFER_SIZE_MINUS_ONE] as Array<Any?>
            shift -= LOG_MAX_BUFFER_SIZE
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
        val lastElementWrapper = ObjectWrapper(last!![0])
        val newRest = removeFromRest(rest!!, shiftStart, index, lastElementWrapper)
        removeFromLast(newRest, this.lastOff(), this.shiftStart, 0)
        return lastElementWrapper.value as E
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

    private fun removeFromRest(rest: Array<Any?>, shift: Int, index: Int, lastWrapper: ObjectWrapper): Array<Any?> {
        val bufferIndex = (index shr shift) and MAX_BUFFER_SIZE_MINUS_ONE

        if (shift == 0) {
            val removedElement = rest[bufferIndex]
            val mutableRest = makeMutable(rest)
            System.arraycopy(rest, bufferIndex + 1, mutableRest, bufferIndex, MAX_BUFFER_SIZE - bufferIndex - 1)
            mutableRest[MAX_BUFFER_SIZE - 1] = lastWrapper.value
            lastWrapper.value = removedElement
            return mutableRest
        }

        var bufferLastIndex = bufferIndex
        while (bufferLastIndex + 1 < MAX_BUFFER_SIZE && rest[bufferLastIndex + 1] != null) bufferLastIndex += 1  // TODO: optimize

        val mutableRest = makeMutable(rest)
        for (i in bufferLastIndex downTo bufferIndex + 1) {
            mutableRest[i] = removeFromRest(mutableRest[i] as Array<Any?>, shift - LOG_MAX_BUFFER_SIZE, 0, lastWrapper)
        }
        mutableRest[bufferIndex] = removeFromRest(mutableRest[bufferIndex] as Array<Any?>, shift - LOG_MAX_BUFFER_SIZE, index, lastWrapper)

        return mutableRest
    }

    private fun pullLastBufferFromRest(rest: Array<Any?>?, restSize: Int, shift: Int) {
        if (shift == 0) {
            this.rest = null
            this.last = rest as Array<E>?
            this.size = restSize
            this.shiftStart = shift
            return
        }

        val lastBufferWrapper = ObjectWrapper(null)
        val newRest = this.pullLastBuffer(rest!!, shift, restSize, lastBufferWrapper)!!
        this.rest = newRest
        this.last = lastBufferWrapper.value as Array<E>
        this.size = restSize
        this.shiftStart = shift
        if (newRest[1] == null) {
            this.rest = newRest[0] as Array<Any?>?
            this.shiftStart -= LOG_MAX_BUFFER_SIZE
        }
    }

    private fun pullLastBuffer(rest: Array<Any?>, shift: Int, restSize: Int, lastWrapper: ObjectWrapper): Array<Any?>? {
        val bufferIndex = ((restSize - 1) shr shift) and MAX_BUFFER_SIZE_MINUS_ONE

        if (shift == LOG_MAX_BUFFER_SIZE) {
            lastWrapper.value = rest[bufferIndex] as Array<Any?>
            if (bufferIndex == 0) {
                return null
            }
            val mutableRest = makeMutable(rest)
            mutableRest[bufferIndex] = null
            return mutableRest
        }
        val bufferAtIndex = pullLastBuffer(rest[bufferIndex] as Array<Any?>, shift - LOG_MAX_BUFFER_SIZE, restSize, lastWrapper)
        if (bufferAtIndex == null && bufferIndex == 0) {
            return null
        }
        val mutableRest = makeMutable(rest)
        mutableRest[bufferIndex] = bufferAtIndex
        return mutableRest
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
