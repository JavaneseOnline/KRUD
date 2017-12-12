package online.javanese.krud.crud

import online.javanese.krud.template.Control

/**
 * Represents a table column.
 */
interface Column<OWNR : Any> {
    /**
     * Returns value which user will see and, if acceptable, edit
     */
    fun getValue(owner: OWNR): String

    /**
     * Human-readable label
     */
    val name: String

    /**
     * UI control which will be user in Create form
     */
    val createControl: Control

    /**
     * UI control which will be user in Edit form
     */
    val editControl: Control
}
