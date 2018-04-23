package benchmarks.persistentVector

import immutableVector.ImmutableVector

internal const val FIXED_H_FIXED_S_8        = "FIXED_H_FIXED_S_8"
internal const val FIXED_H_FIXED_S_16       = "FIXED_H_FIXED_S_16"
internal const val FIXED_H_FIXED_S_32       = "FIXED_H_FIXED_S_32"
internal const val FIXED_H_FIXED_S_64       = "FIXED_H_FIXED_S_64"
internal const val FIXED_H_FLAT_S_8         = "FIXED_H_FLAT_S_8"
internal const val FIXED_H_FLAT_S_16        = "FIXED_H_FLAT_S_16"
internal const val FIXED_H_FLAT_S_32        = "FIXED_H_FLAT_S_32"
internal const val FIXED_H_FLAT_S_64        = "FIXED_H_FLAT_S_64"
internal const val FIXED_H_GROWABLE_S_8     = "FIXED_H_GROWABLE_S_8"
internal const val FIXED_H_GROWABLE_S_16    = "FIXED_H_GROWABLE_S_16"
internal const val FIXED_H_GROWABLE_S_32    = "FIXED_H_GROWABLE_S_32"
internal const val FIXED_H_GROWABLE_S_64    = "FIXED_H_GROWABLE_S_64"

internal const val VARIABLE_H_FIXED_S_8     = "VARIABLE_H_FIXED_S_8"
internal const val VARIABLE_H_FIXED_S_16    = "VARIABLE_H_FIXED_S_16"
internal const val VARIABLE_H_FIXED_S_32    = "VARIABLE_H_FIXED_S_32"
internal const val VARIABLE_H_FIXED_S_64    = "VARIABLE_H_FIXED_S_64"
internal const val VARIABLE_H_FLAT_S_8      = "VARIABLE_H_FLAT_S_8"
internal const val VARIABLE_H_FLAT_S_16     = "VARIABLE_H_FLAT_S_16"
internal const val VARIABLE_H_FLAT_S_32     = "VARIABLE_H_FLAT_S_32"
internal const val VARIABLE_H_FLAT_S_64     = "VARIABLE_H_FLAT_S_64"
internal const val VARIABLE_H_GROWABLE_S_8  = "VARIABLE_H_GROWABLE_S_8"
internal const val VARIABLE_H_GROWABLE_S_16 = "VARIABLE_H_GROWABLE_S_16"
internal const val VARIABLE_H_GROWABLE_S_32 = "VARIABLE_H_GROWABLE_S_32"
internal const val VARIABLE_H_GROWABLE_S_64 = "VARIABLE_H_GROWABLE_S_64"

val EMPTY_VECTOR = hashMapOf<String, ImmutableVector<String>>(
        FIXED_H_FIXED_S_8        to immutableVector.sizeNotInBuffer.fixedHeight.fixedBufferSize.bufferSize8.emptyVector(),
        FIXED_H_FIXED_S_16       to immutableVector.sizeNotInBuffer.fixedHeight.fixedBufferSize.bufferSize16.emptyVector(),
        FIXED_H_FIXED_S_32       to immutableVector.sizeNotInBuffer.fixedHeight.fixedBufferSize.bufferSize32.emptyVector(),
        FIXED_H_FIXED_S_64       to immutableVector.sizeNotInBuffer.fixedHeight.fixedBufferSize.bufferSize64.emptyVector(),
        FIXED_H_FLAT_S_8         to immutableVector.sizeNotInBuffer.fixedHeight.flatBufferSize.bufferSize8.emptyVector(),
        FIXED_H_FLAT_S_16        to immutableVector.sizeNotInBuffer.fixedHeight.flatBufferSize.bufferSize16.emptyVector(),
        FIXED_H_FLAT_S_32        to immutableVector.sizeNotInBuffer.fixedHeight.flatBufferSize.bufferSize32.emptyVector(),
        FIXED_H_FLAT_S_64        to immutableVector.sizeNotInBuffer.fixedHeight.flatBufferSize.bufferSize64.emptyVector(),
        FIXED_H_GROWABLE_S_8     to immutableVector.sizeNotInBuffer.fixedHeight.growableBufferSize.bufferSize8.emptyVector(),
        FIXED_H_GROWABLE_S_16    to immutableVector.sizeNotInBuffer.fixedHeight.growableBufferSize.bufferSize16.emptyVector(),
        FIXED_H_GROWABLE_S_32    to immutableVector.sizeNotInBuffer.fixedHeight.growableBufferSize.bufferSize32.emptyVector(),
        FIXED_H_GROWABLE_S_64    to immutableVector.sizeNotInBuffer.fixedHeight.growableBufferSize.bufferSize64.emptyVector(),

        VARIABLE_H_FIXED_S_8     to immutableVector.sizeNotInBuffer.variableHeight.fixedBufferSize.bufferSize8.emptyVector(),
        VARIABLE_H_FIXED_S_16    to immutableVector.sizeNotInBuffer.variableHeight.fixedBufferSize.bufferSize16.emptyVector(),
        VARIABLE_H_FIXED_S_32    to immutableVector.sizeNotInBuffer.variableHeight.fixedBufferSize.bufferSize32.emptyVector(),
        VARIABLE_H_FIXED_S_64    to immutableVector.sizeNotInBuffer.variableHeight.fixedBufferSize.bufferSize64.emptyVector(),
        VARIABLE_H_FLAT_S_8      to immutableVector.sizeNotInBuffer.variableHeight.flatBufferSize.bufferSize8.emptyVector(),
        VARIABLE_H_FLAT_S_16     to immutableVector.sizeNotInBuffer.variableHeight.flatBufferSize.bufferSize16.emptyVector(),
        VARIABLE_H_FLAT_S_32     to immutableVector.sizeNotInBuffer.variableHeight.flatBufferSize.bufferSize32.emptyVector(),
        VARIABLE_H_FLAT_S_64     to immutableVector.sizeNotInBuffer.variableHeight.flatBufferSize.bufferSize64.emptyVector(),
        VARIABLE_H_GROWABLE_S_8  to immutableVector.sizeNotInBuffer.variableHeight.growableBufferSize.bufferSize8.emptyVector(),
        VARIABLE_H_GROWABLE_S_16 to immutableVector.sizeNotInBuffer.variableHeight.growableBufferSize.bufferSize16.emptyVector(),
        VARIABLE_H_GROWABLE_S_32 to immutableVector.sizeNotInBuffer.variableHeight.growableBufferSize.bufferSize32.emptyVector(),
        VARIABLE_H_GROWABLE_S_64 to immutableVector.sizeNotInBuffer.variableHeight.growableBufferSize.bufferSize64.emptyVector()
)