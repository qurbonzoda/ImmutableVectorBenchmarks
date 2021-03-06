package benchmarks.scalaVector

import benchmarks.*
import scala.collection.immutable.`Vector$`
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

    private var vector = `Vector$`.`MODULE$`.empty<String>()

    @Setup(Level.Trial)
    fun prepare() {
        this.vector = `Vector$`.`MODULE$`.empty<String>()
        repeat(times = listSize) {
            vector = vector.appendBack("some element")
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
        val iterator = vector.reverseIterator()

        while (iterator.hasNext()) {
            bh.consume(iterator.next())
        }
    }
}
