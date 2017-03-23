import ch.qos.logback.classic.encoder.PatternLayoutEncoder

appender("STDOUT", ConsoleAppender) {
    encoder(PatternLayoutEncoder) {
        pattern = "T|%-5.-5level %-20.20logger{5} - %msg %n"
    }
}

//logger("gmjonker.math.Correlation", TRACE)
//logger("gmjonker.math.Score", TRACE)
//logger("gmjonker.math.ScoreMath", TRACE)
//logger("gmjonker.math.IndicationMath", TRACE)
logger("gmjonker.math.IndicationStats", TRACE)

root(INFO, ["STDOUT"])