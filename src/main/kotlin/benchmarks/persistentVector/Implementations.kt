package benchmarks.persistentVector

import immutableVector.ImmutableVector

internal const val FIXED_HEIGHT_SIZE_8  = "FIXED_HEIGHT_SIZE_8"
internal const val FIXED_HEIGHT_SIZE_16 = "FIXED_HEIGHT_SIZE_16"
internal const val FIXED_HEIGHT_SIZE_32 = "FIXED_HEIGHT_SIZE_32"
internal const val FIXED_HEIGHT_SIZE_64 = "FIXED_HEIGHT_SIZE_64"

internal const val VARIABLE_HEIGHT_SIZE_8  = "VARIABLE_HEIGHT_SIZE_8"
internal const val VARIABLE_HEIGHT_SIZE_16 = "VARIABLE_HEIGHT_SIZE_16"
internal const val VARIABLE_HEIGHT_SIZE_32 = "VARIABLE_HEIGHT_SIZE_32"
internal const val VARIABLE_HEIGHT_SIZE_64 = "VARIABLE_HEIGHT_SIZE_64"

val EMPTY_VECTOR = hashMapOf<String, ImmutableVector<String>>(
        FIXED_HEIGHT_SIZE_8 to immutableVector.sizeNotInBuffer.fixedHeight.fixedBufferSize.bufferSize8.emptyVector(),
        FIXED_HEIGHT_SIZE_16 to immutableVector.sizeNotInBuffer.fixedHeight.fixedBufferSize.bufferSize16.emptyVector(),
        FIXED_HEIGHT_SIZE_32 to immutableVector.sizeNotInBuffer.fixedHeight.fixedBufferSize.bufferSize32.emptyVector(),
        FIXED_HEIGHT_SIZE_64 to immutableVector.sizeNotInBuffer.fixedHeight.fixedBufferSize.bufferSize64.emptyVector(),

        VARIABLE_HEIGHT_SIZE_8 to immutableVector.sizeNotInBuffer.variableHeight.fixedBufferSize.bufferSize8.emptyVector(),
        VARIABLE_HEIGHT_SIZE_16 to immutableVector.sizeNotInBuffer.variableHeight.fixedBufferSize.bufferSize16.emptyVector(),
        VARIABLE_HEIGHT_SIZE_32 to immutableVector.sizeNotInBuffer.variableHeight.fixedBufferSize.bufferSize32.emptyVector(),
        VARIABLE_HEIGHT_SIZE_64 to immutableVector.sizeNotInBuffer.variableHeight.fixedBufferSize.bufferSize64.emptyVector()
)