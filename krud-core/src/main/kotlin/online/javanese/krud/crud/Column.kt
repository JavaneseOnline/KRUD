package online.javanese.krud.crud

import online.javanese.krud.template.control.Control

/**
 * Represents a table column.
 */
interface Column<OWNR : Any> {

    /**
     * Returns value(s) which user will see and, if acceptable, edit
     */
    fun getValues(owner: OWNR): List<String>

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
