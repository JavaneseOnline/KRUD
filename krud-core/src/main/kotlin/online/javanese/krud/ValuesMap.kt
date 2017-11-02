package online.javanese.krud

import io.ktor.util.ValuesMap
import io.ktor.util.toMap

internal fun ValuesMap.toStringMap() =
        toMap().mapValues { (_, v) -> v.single() }
