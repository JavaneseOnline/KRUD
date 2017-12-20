package online.javanese.krud.crud

import online.javanese.krud.template.CheckBox
import online.javanese.krud.template.Control
import online.javanese.krud.template.EmptyControl
import online.javanese.krud.template.TextInput
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
        controlFactory: (name: String, title: String) -> Control = TextInput
) : Column<OWNR> {

    constructor(
            property: KProperty1<OWNR, T>,
            title: String = property.name.capitalize(),
            toString: (T) -> String = Any?::toString,
            controlFactory: (name: String, title: String) -> Control = TextInput
    ) : this(
            { ownr -> toString(property.get(ownr)) }, property.name, title, controlFactory
    )

    override fun getValue(owner: OWNR): String = getStringValue(owner)
    private val control: Control = controlFactory(name, title)
    override val createControl: Control get() = control
    override val editControl: Control get() = control
}

class BooleanCol<OWNR : Any>(
        private val getValue: (OWNR) -> Boolean,
        override val name: String,
        title: String = name.capitalize(),
        controlFactory: (name: String, title: String) -> Control = CheckBox
) : Column<OWNR> {

    constructor(
            property: KProperty1<OWNR, Boolean>,
            title: String = property.name.capitalize(),
            controlFactory: (name: String, title: String) -> Control = CheckBox
    ) : this(
            { ownr -> property.get(ownr) }, property.name, title, controlFactory
    )

    override fun getValue(owner: OWNR): String = getValue.invoke(owner).toString()
    private val control: Control = controlFactory(name, title)
    override val createControl: Control get() = control
    override val editControl: Control get() = control
}
