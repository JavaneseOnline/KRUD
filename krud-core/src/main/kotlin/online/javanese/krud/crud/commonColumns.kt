package online.javanese.krud.crud

import online.javanese.krud.template.control.*
import kotlin.reflect.KProperty1

/**
 * Primary key column. Read-only <input type=text>
 */
class IdCol<OWNR : Any, ID>(
        private val getStringValue: (OWNR) -> String,
        override val name: String,
        title: String = "ID"
): Column<OWNR> {

    constructor(
            property: KProperty1<OWNR, ID>,
            title: String = "ID",
            toString: (ID) -> String = Any?::toString
    ) : this(
            { ownr -> toString(property.get(ownr)) }, property.name, title
    )

    override fun getValue(owner: OWNR): String = getStringValue(owner)
    override val createControl: Control get() = EmptyControl
    override val editControl: Control = TextInput(name, title, editable = false)
}

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

    override fun getValue(owner: OWNR): String = getStringValue(owner)

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

    override fun getValue(owner: OWNR): String = getStringValue.invoke(owner)
    override val createControl: Control = createControlFactory(name, title)
    override val editControl: Control = editControlFactory(name, title)
}
