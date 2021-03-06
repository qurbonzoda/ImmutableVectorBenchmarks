package benchmarks.clojureVector

import benchmarks.*
import clojure.lang.PersistentVector
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
    @Param(BM_1, BM_10,  BM_100, BM_1000, BM_10000, BM_100000, BM_1000000, BM_10000000)
    var listSize: Int = 0

    private var vector = PersistentVector.EMPTY

    @Setup(Level.Trial)
    fun prepare() {
        this.vector = PersistentVector.EMPTY
        repeat(times = listSize) {
            vector = vector.cons("some element")
        }
    }

    @Benchmark
    fun firstToLast(bh: Blackhole) {
        for (e in vector) {
            bh.consume(e)
        }
    }

    @Benchmark
    fun lastToFirst(bh: Blackhole) {
        val iterator = vector.listIterator(listSize)

        while (iterator.hasPrevious()) {
            bh.consume(iterator.previous())
        }
    }
}
