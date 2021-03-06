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
@State(Scope.Benchmark)
open class Set {
    @Param(BM_1, BM_10,  BM_100, BM_1000, BM_10000, BM_100000, BM_1000000, BM_10000000)
    var listSize: Int = 0

    private var vector: ImmutableList<String> = persistentVectorOf()

    private val randomIndices = mutableListOf<Int>()

    @Setup(Level.Trial)
    fun prepare() {
        this.vector = persistentVectorOf()
        this.randomIndices.clear()
        repeat(times = listSize) {
            vector = vector.add("some element")
            randomIndices.add(it)
        }
        randomIndices.shuffle()
    }

    @Benchmark
    fun setByIndex(): ImmutableList<String> {
        for (i in 0 until vector.size) {
            vector = vector.set(i, "another element")
        }
        return vector
    }

    @Benchmark
    fun setByRandomIndex(): ImmutableList<String> {
        for (i in 0 until vector.size) {
            vector = vector.set(randomIndices[i], "another element")
        }
        return vector
    }
}