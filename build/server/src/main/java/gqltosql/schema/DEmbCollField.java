package gqltosql.schema;

import java.util.List;
import java.util.function.BiConsumer;
import java.util.function.Function;

public class DEmbCollField<T, R> extends DRefCollField<T, R> {

	public DEmbCollField(DModel<T> decl, String name, String column, String collTable, DModel<?> ref,
			Function<T, List<R>> getter, BiConsumer<T, List<R>> setter) {
		super(decl, name, column, collTable, ref, getter, setter);
	}

	@Override
	public String getCollTableName(String parentTable) {
		return parentTable + super.getCollTableName(parentTable);
	}
}
