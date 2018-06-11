package benchmarks.persistentVector

import benchmarks.*
import kotlinx.collections.immutable.ImmutableList
import kotlinx.collections.immutable.implementations.immutableList.persistentVectorOf
import org.openjdk.jmh.annotations.*
import java.util.concurrent.TimeUnit

@Fork(1)
@Warmup(iterations = 5)
@Measurement(iterations = 5)
@BenchmarkMode(Mode.AverageTime)
@OutputTimeUnit(TimeUnit.MICROSECONDS)
@State(Scope.Thread)
open class Remove {
    @Param(BM_1, BM_10,  BM_100, BM_1000, BM_10000, BM_100000, BM_1000000, BM_10000000)
    var listSize: Int = 0

    private var preparedVector: ImmutableList<String> = persistentVectorOf()

    @Setup(Level.Trial)
    fun prepare() {
        preparedVector = persistentVectorOf()
        repeat(times = listSize) {
            preparedVector = preparedVector.add("some element")
        }
    }

    @Benchmark
    fun removeLast(): ImmutableList<String> {
        var vector = preparedVector
        for (i in 0 until listSize) {
            vector = vector.removeAt(vector.size - 1)
        }
        return vector
    }
}