package online.javanese.krud

import io.ktor.util.ValuesMap
import io.ktor.util.toMap

fun ValuesMap.toStringMap() =
        toMap().mapValues { (_, v) -> v.single() }
