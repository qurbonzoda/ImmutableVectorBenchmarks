package benchmarks.builder

import benchmarks.*
import kotlinx.collections.immutable.ImmutableList
import kotlinx.collections.immutable.implementations.immutableList.persistentVectorOf
import org.openjdk.jmh.annotations.*
import org.openjdk.jmh.infra.Blackhole
import java.util.concurrent.TimeUnit

@Fork(1)
@Warmup(iterations = 5)
@Measurement(iterations = 5)
@BenchmarkMode(Mode.AverageTime)
@OutputTimeUnit(TimeUnit.MICROSECONDS)
@State(Scope.Thread)
open class Add {
    @Param(BM_1, BM_10,  BM_100, BM_1000, BM_10000, BM_100000, BM_1000000, BM_10000000)
    var listSize: Int = 0

    private var emptyVector: ImmutableList<String> = persistentVectorOf()

    @Benchmark
    fun addLast(): ImmutableList.Builder<String> {
        val vector = this.emptyVector.builder()
        repeat(times = this.listSize) {
            vector.add("some element")
        }
        return vector
    }

    @Benchmark
    fun addLastAndIterate(bh: Blackhole): ImmutableList.Builder<String> {
        val vector = this.emptyVector.builder()
        repeat(times = this.listSize) {
            vector.add("some element")
        }

        for (e in vector) {
            bh.consume(e)
        }

        return vector
    }

    @Benchmark
    fun addLastAndGet(bh: Blackhole): ImmutableList.Builder<String> {
        val vector = this.emptyVector.builder()
        repeat(times = this.listSize) {
            vector.add("some element")
        }

        for (i in 0 until vector.size) {
            bh.consume(vector.get(i))
        }

        return vector
    }
}