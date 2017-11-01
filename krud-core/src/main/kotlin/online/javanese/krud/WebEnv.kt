package online.javanese.krud

import online.javanese.krud.template.ModuleTemplate

/**
 * Holds route prefix and [ModuleTemplate].
 */
class WebEnv(
        val routePrefix: String,
        val template: ModuleTemplate
)
