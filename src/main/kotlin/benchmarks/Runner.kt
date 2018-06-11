package benchmarks

import org.openjdk.jmh.results.RunResult
import org.openjdk.jmh.runner.Runner
import org.openjdk.jmh.runner.options.OptionsBuilder
import org.openjdk.jmh.runner.options.TimeValue
import java.io.FileWriter

fun main(args: Array<String>) {
    val millisInSecond = 1000L
    val secondsInMinute = 60L
    val fiveMinutes = 5 * secondsInMinute * millisInSecond
    Thread.sleep(fiveMinutes)

    for (implementation in args) {
        val outputFile = "teamcityArtifacts/$implementation.csv"
        val options = OptionsBuilder()
                .jvmArgs("-Xms2048m", "-Xmx2048m")
//                .include("$implementation.Add.addLast$")
//                .include("$implementation.Get")
//                .include("$implementation.Set")
//                .include("$implementation.Remove")
//                .include("$implementation.Iterate")
                .include(implementation)
                .warmupIterations(10)
                .measurementIterations(10)
                .warmupTime(TimeValue.milliseconds(500))
                .measurementTime(TimeValue.milliseconds(500))
//                .param("listSize", BM_1000000/*, BM_10000000*/)
                .addProfiler("gc")

        val runResults = Runner(options.build()).run()
        printResults(runResults, implementation, outputFile)
    }
}

fun printResults(runResults: Collection<RunResult>, implementation: String, outputFile: String) {
    val csvHeader = "Implementation,Method,listSize,Score,Score Error,Allocation Rate"

    val fileWriter = FileWriter(outputFile)

    fileWriter.appendln(csvHeader)

    runResults.forEach {
        fileWriter.appendln(csvRowFrom(it, implementation))
    }

    fileWriter.flush()
    fileWriter.close()
}

fun csvRowFrom(result: RunResult, implementation: String): String {
    val nanosInMicros = 1000
    val method = result.primaryResult.getLabel()
    val listSize = result.params.getParam("listSize").toInt()
    val score = result.primaryResult.getScore() * nanosInMicros / listSize
    val scoreError = result.primaryResult.getScoreError() * nanosInMicros / listSize
    val allocationRate = result.secondaryResults["·gc.alloc.rate.norm"]!!.getScore() / listSize

    return "$implementation,$method,$listSize,%.3f,%.3f,%.3f".format(score, scoreError, allocationRate)
}