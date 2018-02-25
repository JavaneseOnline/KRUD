package online.javanese.krud

import io.ktor.util.StringValues
import io.ktor.util.StringValuesBuilder

infix fun StringValues.updatedWith(patch: StringValues): StringValues =
        StringValuesBuilder()
                .also {
                    it.appendAll(patch)
                    this.forEach { key, values ->
                        if (key !in patch) {
                            it.appendAll(key, values)
                        }
                    }
                }
                .build()
