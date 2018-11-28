package online.javanese.krud.kwery

import com.github.andrewoma.kwery.mapper.Dao
import com.github.andrewoma.kwery.mapper.Table
import online.javanese.krud.crud.Sort
import online.javanese.krud.kwery.KweryTable.Companion.TransformKweryColumn
import java.util.*
import com.github.andrewoma.kwery.mapper.Column as KwColumn
import online.javanese.krud.crud.Column as KrColumn

/**
 * A factory function for tables with [Uuid] IDs.
 */
fun <E : Any> KweryTable(
        route: String,
        table: Table<E, Uuid>,
        dao: Dao<E, Uuid>,
        getCount: () -> Int,
        displayName: String = table.name,
        getTitleOf: (E) -> String = Any::toString,
        columns: Set<KwColumn<E, *>> = table.defaultColumns,
        sort: Sort<Uuid> = Sort.NoneOrImplicit,
        transformColumn: (KwColumn<E, *>) -> KrColumn<E> = TransformKweryColumn(),
        fallback: Map<String, *> = emptyMap<String, Nothing>()
): KweryTable<E, Uuid> = KweryTable<E, Uuid>(
        route,
        table,
        dao,
        getCount,
        UUID::fromString,
        displayName,
        getTitleOf,
        columns,
        sort,
        transformColumn,
        fallback
)
