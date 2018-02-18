package online.javanese.krud

import io.ktor.util.StringValues
import io.ktor.util.toMap

fun StringValues.toStringMap() =
        toMap().mapValues { (_, v) -> v.single() }
