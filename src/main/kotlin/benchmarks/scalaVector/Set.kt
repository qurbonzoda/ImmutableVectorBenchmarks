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
@State(Scope.Benchmark)
open class Set {
    @Param(BM_1, BM_10,  BM_100, BM_1000, BM_10000, BM_100000, BM_1000000, BM_10000000)
    var listSize: Int = 0

    private var vector = `Vector$`.`MODULE$`.empty<String>()

    private val randomIndices = mutableListOf<Int>()

    @Setup(Level.Trial)
    fun prepare() {
        this.vector = `Vector$`.`MODULE$`.empty<String>()
        randomIndices.clear()
        repeat(times = listSize) {
            vector = vector.appendBack("some element")
            randomIndices.add(it)
        }
        randomIndices.shuffle()
    }

    @Benchmark
    fun setByIndex(): Vector<String> {
        for (i in 0 until vector.length()) {
            vector = vector.updateAt(i, "another element")
        }
        return vector
    }

    @Benchmark
    fun setByRandomIndex(): Vector<String> {
        for (i in 0 until vector.length()) {
            vector = vector.updateAt(randomIndices[i], "another element")
        }
        return vector
    }
}