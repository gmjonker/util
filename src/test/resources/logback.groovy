import ch.qos.logback.classic.encoder.PatternLayoutEncoder
import ch.qos.logback.core.ConsoleAppender

appender("STDOUT", ConsoleAppender) {
    encoder(PatternLayoutEncoder) {
        pattern = "T|%-5.-5level %-20.20logger{5} - %msg %n"
    }
}

//logger("gmjonker.math.Score", TRACE)
//logger("gmjonker.math.ScoreMath", TRACE)
//logger("gmjonker.math.IndicationMath", TRACE)
logger("gmjonker.math.IndicationStatistics", TRACE)

root(INFO, ["STDOUT"])