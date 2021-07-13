package gqltosql.schema;

import java.util.function.BiConsumer;
import java.util.function.Function;

public class DPrimField<T, R> extends DField<T, R> {

	private Function<T, R> getter;

	private BiConsumer<T, R> setter;

	public DPrimField(DModel<T> decl, String name, String column, Function<T, R> getter, BiConsumer<T, R> setter) {
		super(decl, name, column);
		this.getter = getter;
		this.setter = setter;
	}

	@Override
	public FieldType getType() {
		return FieldType.Primitive;
	}

	@Override
	public R getValue(T _this) {
		return getter.apply(_this);
	}

	@Override
	public Object fetchValue(T _this, IDataFetcher fetcher) {
		return fetcher.onPrimitiveValue(getValue(_this));
	}

	@Override
	public void setValue(T _this, R value) {
		setter.accept(_this, value);
	}
}
