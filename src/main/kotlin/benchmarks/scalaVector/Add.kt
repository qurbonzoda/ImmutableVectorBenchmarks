package benchmarks.scalaVector

import benchmarks.*
import com.aol.cyclops.dexx.collections.DexxPVector
import org.openjdk.jmh.annotations.*
import org.openjdk.jmh.infra.Blackhole
import java.util.concurrent.TimeUnit
import scala.collection.immutable.Vector
import scala.collection.immutable.`Vector$`

@Fork(1)
@Warmup(iterations = 5)
@Measurement(iterations = 5)
@BenchmarkMode(Mode.AverageTime)
@OutputTimeUnit(TimeUnit.MICROSECONDS)
@State(Scope.Thread)
open class Add {
    @Param(BM_1, BM_10,  BM_100, BM_1000, BM_10000, BM_100000, BM_1000000, BM_10000000)
    var listSize: Int = 0

    private var emptyVector = `Vector$`.`MODULE$`.empty<String>()

    @Benchmark
    fun addLast(): Vector<String> {
        var vector = this.emptyVector
        repeat(times = this.listSize) {
            vector = vector.appendBack("some element")
        }
        return vector
    }

    @Benchmark
    fun addLastAndIterate(bh: Blackhole): Vector<String> {
        var vector = this.emptyVector
        repeat(times = this.listSize) {
            vector = vector.appendBack("some element")
        }

        for (e in vector) {
            bh.consume(e)
        }

        return vector
    }

    @Benchmark
    fun addLastAndGet(bh: Blackhole): Vector<String> {
        var vector = this.emptyVector
        repeat(times = this.listSize) {
            vector = vector.appendBack("some element")
        }

        for (i in 0 until vector.length()) {
            bh.consume(vector.apply(i))
        }

        return vector
    }
}