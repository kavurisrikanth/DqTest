package gqltosql.schema;

import java.util.List;
import java.util.function.BiConsumer;
import java.util.function.Function;

public class DPrimCollField<T, R> extends DField<T, List<R>> {

	private Function<T, List<R>> getter;

	private BiConsumer<T, List<R>> setter;

	public DPrimCollField(DModel<T> decl, String name, String column, String collTable, Function<T, List<R>> getter,
			BiConsumer<T, List<R>> setter) {
		super(decl, name, column);
		setCollTable(collTable);
		this.setter = setter;
		this.getter = getter;
	}

	@Override
	public FieldType getType() {
		return FieldType.PrimitiveCollection;
	}

	@Override
	public List<R> getValue(T _this) {
		return getter.apply(_this);
	}

	@Override
	public Object fetchValue(T _this, IDataFetcher fetcher) {
		return fetcher.onPrimitiveList(getValue(_this));
	}

	@Override
	public void setValue(T _this, List<R> value) {
		setter.accept(_this, value);
	}
}
