package online.javanese.krud.kwery

import com.github.andrewoma.kwery.mapper.Column
import com.github.andrewoma.kwery.mapper.Dao
import com.github.andrewoma.kwery.mapper.Table
import online.javanese.krud.crud.EnumeratedColAdapter

/**
 * Describes foreign key column
 *
 * @param T element type
 * @param ID T's primary key type
 */
class KweryForeignEnumeratedColAdapter<T : Any, ID : Any>(
        table: Table<T, ID>,
        private val dao: Dao<T, ID>,
        private val getTitle: (T) -> String,
        private val columns: Set<Column<T, *>> = dao.defaultColumns
) : EnumeratedColAdapter<T, ID> {

    private val getId = table.idColumns.single().property as (T) -> ID

    override fun elements(): Iterable<T> = dao.findAll(columns)
    override fun idOf(t: T): ID = getId(t)
    override fun element(id: ID): T = dao.findById(id, columns)!!
    override fun titleOf(element: T): String = getTitle(element)

}
