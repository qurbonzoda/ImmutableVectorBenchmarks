package benchmarks.clojureVector

import benchmarks.*
import clojure.lang.PersistentVector
import org.openjdk.jmh.annotations.*
import java.util.concurrent.TimeUnit

@Fork(1)
@Warmup(iterations = 5)
@Measurement(iterations = 5)
@BenchmarkMode(Mode.AverageTime)
@OutputTimeUnit(TimeUnit.MICROSECONDS)
@State(Scope.Benchmark)
open class Set {
    @Param(BM_1, BM_4, BM_10, BM_15, BM_20, BM_25, BM_50,
            BM_100, BM_1000, BM_10000, BM_100000, BM_1000000, BM_10000000)
    var listSize: Int = 0

    var vector = PersistentVector.EMPTY

    @Setup(Level.Trial)
    fun prepare() {
        this.vector = PersistentVector.EMPTY
        repeat(times = listSize) {
            vector = vector.cons("some element")
        }
    }

    @Benchmark
    fun setByIndex(): PersistentVector {
        for (i in 0 until vector.size) {
            vector = vector.assocN(i, "another element")
        }
        return vector
    }
}