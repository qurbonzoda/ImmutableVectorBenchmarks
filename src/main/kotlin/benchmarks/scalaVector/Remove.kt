package benchmarks.scalaVector

import benchmarks.*
import scala.collection.immutable.`Vector$`
import org.openjdk.jmh.annotations.*
import scala.collection.immutable.Vector
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

    private var preparedVector = `Vector$`.`MODULE$`.empty<String>()

    @Setup(Level.Trial)
    fun prepare() {
        preparedVector = `Vector$`.`MODULE$`.empty<String>()
        repeat(times = listSize) {
            preparedVector = preparedVector.appendBack("some element")
        }
    }

    @Benchmark
    fun removeLast(): Vector<String> {
        var vector = preparedVector
        for (i in 0 until listSize) {
            vector = vector.dropRight(1)
        }
        return vector
    }
}