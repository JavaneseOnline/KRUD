package online.javanese.krud.crud

import online.javanese.krud.template.control.*
import kotlin.reflect.KProperty1

/**
 * Primary key column. Read-only <input type=text>
 */
fun <OWNR : Any, ID> IdCol(
        getStringValue: (OWNR) -> String,
        name: String,
        title: String = "ID"
): TextCol<OWNR, ID> = TextCol(
        getStringValue, name, title, { _, _ -> EmptyControl }, TextInput.ReadOnly
)

/**
 * Primary key column from property.
 */
fun <OWNR : Any, ID> IdCol(
        property: KProperty1<OWNR, ID>,
        title: String = "ID",
        toString: (ID) -> String = Any?::toString
): TextCol<OWNR, ID> = TextCol(
        { toString(property.get(it)) }, property.name, title, { _, _ -> EmptyControl }, TextInput.ReadOnly
)

/**
 * Ordinary text column. Editable <input type=text>
 */
class TextCol<OWNR : Any, T>(
        private val getStringValue: (OWNR) -> String,
        override val name: String,
        title: String = name.capitalize(),
        createControlFactory: (name: String, title: String) -> Control = TextInput.Editable,
        editControlFactory: (name: String, title: String) -> Control = createControlFactory
) : Column<OWNR> {

    constructor(
            property: KProperty1<OWNR, T>,
            title: String = property.name.capitalize(),
            toString: (T) -> String = Any?::toString,
            createControlFactory: (name: String, title: String) -> Control = TextInput.Editable,
            editControlFactory: (name: String, title: String) -> Control = createControlFactory
    ) : this(
            { ownr -> toString(property.get(ownr)) }, property.name, title, createControlFactory, editControlFactory
    )

    override fun getValues(owner: OWNR): List<String> = listOf(getStringValue(owner))

    override val createControl: Control = createControlFactory(name, title)
    override val editControl: Control = editControlFactory(name, title)
}

class BooleanCol<OWNR : Any>(
        private val getStringValue: (OWNR) -> String,
        override val name: String,
        title: String = name.capitalize(),
        createControlFactory: (name: String, title: String) -> Control = CheckBox,
        editControlFactory: (name: String, title: String) -> Control = createControlFactory
) : Column<OWNR> {

    constructor(
            property: KProperty1<OWNR, Boolean>,
            title: String = property.name.capitalize(),
            toString: (Boolean) -> String = Any::toString,
            createControlFactory: (name: String, title: String) -> Control = CheckBox,
            editControlFactory: (name: String, title: String) -> Control = createControlFactory
    ) : this(
            { ownr -> toString(property.get(ownr)) }, property.name, title, createControlFactory, editControlFactory
    )

    override fun getValues(owner: OWNR): List<String> = listOf(getStringValue.invoke(owner))
    override val createControl: Control = createControlFactory(name, title)
    override val editControl: Control = editControlFactory(name, title)
}
