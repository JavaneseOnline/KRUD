package online.javanese.krud.stat

import io.ktor.application.ApplicationCallPipeline
import io.ktor.application.call
import io.ktor.routing.Route

fun Route.installHitStatInterceptor(stat: HitStat) {
    intercept(ApplicationCallPipeline.Call) {
        stat.trackVisit(call)
    }
}
