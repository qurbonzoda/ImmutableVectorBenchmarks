package benchmarks.rrbTree

import benchmarks.*
import org.openjdk.jmh.annotations.*
import org.openjdk.jmh.infra.Blackhole
import org.organicdesign.fp.collections.RrbTree
import java.util.concurrent.TimeUnit

@Fork(1)
@Warmup(iterations = 5)
@Measurement(iterations = 5)
@BenchmarkMode(Mode.AverageTime)
@OutputTimeUnit(TimeUnit.MICROSECONDS)
@State(Scope.Benchmark)
open class Get {
    @Param(BM_1, BM_10, BM_100, BM_1000, BM_10000, BM_100000, BM_1000000, BM_10000000)
    var listSize: Int = 0

    private var vector = RrbTree.empty<String>()

    @Setup(Level.Trial)
    fun prepare() {
        this.vector = RrbTree.empty<String>()
        repeat(times = listSize) {
            vector = vector.append("some element")
        }
    }

    @Benchmark
    fun getByIndex(bh: Blackhole) {
        for (i in 0 until vector.size) {
            bh.consume(vector.get(i))
        }
    }
}