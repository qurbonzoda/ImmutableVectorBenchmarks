package benchmarks.scalaVector

import benchmarks.*
import com.aol.cyclops.scala.collections.ScalaPVector
import org.openjdk.jmh.annotations.*
import java.util.concurrent.TimeUnit

@Fork(1)
@Warmup(iterations = 5)
@Measurement(iterations = 5)
@BenchmarkMode(Mode.AverageTime)
@OutputTimeUnit(TimeUnit.MICROSECONDS)
@State(Scope.Thread)
open class Remove {
    @Param(BM_1, BM_4, BM_10, BM_15, BM_20, BM_25, BM_50,
            BM_100, BM_1000, BM_10000, BM_100000, BM_1000000, BM_10000000)
    var listSize: Int = 0

    var preparedVector = ScalaPVector.emptyPVector<String>()

    @Setup(Level.Trial)
    fun prepare() {
        preparedVector = ScalaPVector.emptyPVector<String>()
        repeat(times = listSize) {
            preparedVector = preparedVector.plus("some element")
        }
    }

    @Benchmark
    fun removeLast(): ScalaPVector<String> {
        var vector = preparedVector
        for (i in 0 until listSize) {
            vector = vector.minus(vector.size - 1)
        }
        return vector
    }
}