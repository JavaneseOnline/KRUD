package online.javanese.krud.kwery

import com.github.andrewoma.kwery.mapper.Column
import online.javanese.krud.crud.*
import online.javanese.krud.template.control.*

fun <OWNR : Any, ID> IdCol(
        column: Column<OWNR, ID>,
        title: String = "ID",
        toString: (ID) -> String = Any?::toString
): TextCol<OWNR, ID> =
        IdCol({ toString(column.property(it)) }, column.name, title)

fun <OWNR : Any, T> TextCol(
        column: Column<OWNR, T>,
        title: String = column.name.capitalize(),
        toString: (T) -> String = Any?::toString,
        createControlFactory: (name: String, title: String) -> Control = TextInput.Editable,
        editControlFactory: (name: String, title: String) -> Control = createControlFactory
): TextCol<OWNR, T> =
        TextCol({ toString(column.property(it)) }, column.name, title, createControlFactory, editControlFactory)

fun <OWNR : Any> BooleanCol(
        column: Column<OWNR, Boolean>,
        title: String = column.name.capitalize(),
        toString: (Boolean) -> String = Any?::toString,
        createControlFactory: (name: String, title: String) -> Control = CheckBox,
        editControlFactory: (name: String, title: String) -> Control = createControlFactory
): BooleanCol<OWNR> =
        BooleanCol({ toString(column.property(it)) }, column.name, title, createControlFactory, editControlFactory)

fun <OWNR : Any, T, TID : Any> EnumeratedCol(
        column: Column<OWNR, TID>,
        adapter: EnumeratedColAdapter<T, TID>,
        title: String = column.name.capitalize(),
        idToString: (TID) -> String = Any::toString,
        createControlFactory: (name: String, title: String, names: List<String>, titles: List<String>) -> Control = ComboBox,
        editControlFactory: (name: String, title: String, names: List<String>, titles: List<String>) -> Control = createControlFactory
): EnumeratedCol<OWNR, T, TID> =
        EnumeratedCol(column.property, column.name, adapter, title, idToString, createControlFactory, editControlFactory)
