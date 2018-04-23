package benchmarks

import org.openjdk.jmh.results.RunResult
import org.openjdk.jmh.runner.Runner
import org.openjdk.jmh.runner.options.OptionsBuilder
import org.openjdk.jmh.runner.options.TimeValue
import java.io.FileWriter

fun main(args: Array<String>) {
    val implementation = "persistentVector"
    val outputFile = "teamcityArtifacts/$implementation.csv"
    val options = OptionsBuilder()
            .jvmArgs("-Xms3072m", "-Xmx3072m")
            .include(implementation)
            .warmupIterations(10)
            .measurementIterations(10)
            .warmupTime(TimeValue.milliseconds(500))
            .measurementTime(TimeValue.milliseconds(500))
            .addProfiler("gc")

    val runResults = Runner(options.build()).run()
    printResults(runResults, implementation, outputFile)
}

fun printResults(runResults: Collection<RunResult>, implementation: String, outputFile: String) {
    val csvHeader = "Implementation,Method,listSize,heightType,bufferType,bufferSize,Score,Score Error,Allocation Rate"

    val fileWriter = FileWriter(outputFile)

    fileWriter.appendln(csvHeader)

    runResults.forEach {
        fileWriter.appendln(csvRowFrom(it, implementation))
    }

    fileWriter.flush()
    fileWriter.close()
}

fun csvRowFrom(result: RunResult, implementation: String): String {
    val nanosInMillis = 1000
    val method = result.primaryResult.getLabel()
    val listSize = result.params.getParam("listSize").toInt()
    val score = result.primaryResult.getScore() * nanosInMillis / listSize
    val scoreError = result.primaryResult.getScoreError() * nanosInMillis / listSize
    val allocationRate = result.secondaryResults["·gc.alloc.rate.norm"]!!.getScore() / listSize

    val impl = result.params.getParam("impl")
    val (heightType, bufferType, bufferSize) = extractInfo(impl)

    return "$implementation,$method,$listSize,$heightType,$bufferType,$bufferSize,%.3f,%.3f,%.3f"
            .format(score, scoreError, allocationRate)
}

fun extractInfo(impl: String): Triple<String, String, String> {
    val lastDelimiter = impl.indexOfLast { it == '_' }
    val bufferSize = impl.substring(lastDelimiter + 1)

    if (impl.startsWith("FIXED_H_")) {
        val delimiter = impl.substring(8).indexOfFirst { it == '_' }
        return Triple("FIXED", impl.substring(8, 8 + delimiter), bufferSize)
    }
    assert(impl.startsWith("VARIABLE_H_"))
    val delimiter = impl.substring(11).indexOfFirst { it == '_' }
    return Triple("VARIABLE", impl.substring(11, 11 + delimiter), bufferSize)
}