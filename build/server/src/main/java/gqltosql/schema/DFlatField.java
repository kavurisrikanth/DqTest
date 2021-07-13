package gqltosql.schema;

import java.util.Set;
import java.util.function.Function;

public class DFlatField<T, R> extends DField<T, Set<R>> {

	private Function<T, Set<R>> getter;

	private String[] flatPaths;

	public DFlatField(DModel<T> decl, String name, String column, String collTable, DModel<?> ref,
			Function<T, Set<R>> getter, String... flatPaths) {
		super(decl, name, column);
		this.flatPaths = flatPaths;
		setCollTable(collTable);
		setRef(ref);
		this.getter = getter;
	}

	public String[] getFlatPaths() {
		return flatPaths;
	}

	@Override
	public FieldType getType() {
		return FieldType.ReferenceCollection;
	}

	@Override
	public Set<R> getValue(T _this) {
		return getter.apply(_this);
	}

	@Override
	public Object fetchValue(T _this, IDataFetcher fetcher) {
		return fetcher.onFlatValue(getValue(_this));
	}

	@Override
	public void setValue(T _this, Set<R> value) {
		throw new RuntimeException("Can not set value to flat field");
	}
}
