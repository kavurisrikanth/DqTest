package gqltosql.schema;

import java.util.function.BiConsumer;
import java.util.function.Function;

public class DRefField<T, R> extends DField<T, R> {

	private Function<T, R> getter;
	private BiConsumer<T, R> setter;

	public DRefField(DModel<T> decl, String name, String column, DModel<?> ref, Function<T, R> getter,
			BiConsumer<T, R> setter) {
		super(decl, name, column);
		setRef(ref);
		this.getter = getter;
		this.setter = setter;
	}

	@Override
	public FieldType getType() {
		return FieldType.Reference;
	}

	@Override
	public R getValue(T _this) {
		return getter.apply(_this);
	}

	@Override
	public Object fetchValue(T _this, IDataFetcher fetcher) {
		return fetcher.onReferenceValue(getValue(_this));
	}

	@Override
	public void setValue(T _this, R value) {
		setter.accept(_this, value);
	}
}
