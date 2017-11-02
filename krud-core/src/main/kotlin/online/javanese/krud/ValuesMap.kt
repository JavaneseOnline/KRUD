package online.javanese.krud

import org.jetbrains.ktor.util.ValuesMap
import org.jetbrains.ktor.util.toMap

internal fun ValuesMap.toStringMap() = toMap().mapValues { (_, v) -> v.single() }
