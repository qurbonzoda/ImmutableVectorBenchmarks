package benchmarks.persistentVector

import benchmarks.*
import immutableVector.ImmutableVector
import immutableVector.sizeNotInBuffer.fixedHeight.fixedBufferSize.bufferSize8.emptyVector
import org.openjdk.jmh.annotations.*
import org.openjdk.jmh.infra.Blackhole
import java.util.concurrent.TimeUnit

@Fork(1)
@Warmup(iterations = 5)
@Measurement(iterations = 5)
@BenchmarkMode(Mode.AverageTime)
@OutputTimeUnit(TimeUnit.MICROSECONDS)
@State(Scope.Benchmark)
open class Iterate {
    @Param(BM_1, BM_4, BM_10, BM_15, BM_20, BM_25, BM_35, BM_50, BM_75,
            BM_100, BM_1000, BM_10000, BM_100000, BM_1000000)
    var listSize: Int = 0

    @Param(FIXED_HEIGHT_SIZE_8, FIXED_HEIGHT_SIZE_16, FIXED_HEIGHT_SIZE_32, FIXED_HEIGHT_SIZE_64,
            VARIABLE_HEIGHT_SIZE_8, VARIABLE_HEIGHT_SIZE_16, VARIABLE_HEIGHT_SIZE_32, VARIABLE_HEIGHT_SIZE_64)
    var impl: String = ""

    var vector: ImmutableVector<String> = emptyVector()

    @Setup(Level.Trial)
    fun prepare() {
        this.vector = EMPTY_VECTOR[impl]!!
        repeat(times = listSize) {
            vector = vector.addLast("some element")
        }
    }

    @Benchmark
    fun firstToLast(bh: Blackhole) {
        for (e in vector) {
            bh.consume(e)
        }
    }
}
