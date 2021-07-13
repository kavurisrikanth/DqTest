package gqltosql.schema;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.BiConsumer;
import java.util.function.Function;

public class DModel<T> {

	private String type;
	private String table;
	private Map<String, DField<T, ?>> fields = new HashMap<>();
	private DModel<?> parent;
	private boolean embedded;
	private boolean entity;
	private boolean document;
	private DModelType modelType;

	public DModel(String type, String table, DModelType modelType) {
		this.type = type;
		this.table = table;
		this.modelType = modelType;
	}

	public DModel(String type, String table) {
		this(type, table, false);
	}

	public DModel(String type, String table, boolean embedded) {
		this.type = type;
		this.table = table;
		this.embedded = embedded;
		this.entity = true;
	}

	public List<DField<?, ?>> getAllFields() {
		return new ArrayList<>(fields.values());
	}

	public DModelType getModelType() {
		return modelType;
	}

	public void setEntity(boolean entity) {
		this.entity = entity;
	}

	public boolean isEmbedded() {
		return modelType == DModelType.EMBEDDED;
	}

	public String getTableName() {
		return table;
	}

	public DField<?, ?> getField(String name) {
		DField<?, ?> f = fields.get(name);
		if (f != null) {
			return f;
		}
		if (parent != null) {
			return parent.getField(name);
		}
		return null;
	}

	public boolean hasField(String name) {
		return getField(name) != null;
	}

	public String getType() {
		return type;
	}

	public boolean hasDeclField(String name) {
		return fields.containsKey(name);
	}

	public void setParent(DModel<?> parent) {
		this.parent = parent;
	}

	public DModel<?> getParent() {
		return parent;
	}

	public void addField(DField<T, ?> field) {
		fields.put(field.getName(), field);
		field.setDecl(this);
	}

	public <R> void addPrimitive(String name, String column, Function<T, R> getter, BiConsumer<T, R> setter) {
		addField(new DPrimField<T, R>(this, name, column, getter, setter));
	}

	public <R> void addReference(String name, String column, DModel<?> ref, Function<T, R> getter,
			BiConsumer<T, R> setter) {
		addField(new DRefField<T, R>(this, name, column, ref, getter, setter));
	}

	public <R> void addPrimitiveCollection(String name, String column, String collTable, Function<T, List<R>> getter,
			BiConsumer<T, List<R>> setter) {
		addField(new DPrimCollField<T, R>(this, name, column, collTable, getter, setter));
	}

	public <R> void addReferenceCollection(String name, String column, String collTable, DModel<?> ref,
			Function<T, List<R>> getter, BiConsumer<T, List<R>> setter) {
		if (isEmbedded()) {
			addField(new DEmbCollField<T, R>(this, name, column, collTable, ref, getter, setter));
		} else {
			addField(new DRefCollField<T, R>(this, name, column, collTable, ref, getter, setter));
		}
	}

	public <R> void addFlatCollection(String name, String column, String collTable, DModel<?> ref,
			Function<T, Set<R>> getter, String... flatPaths) {
		addField(new DFlatField<T, R>(this, name, column, collTable, ref, getter, flatPaths));
	}

	public <R> void addInverseCollection(String name, String column, DModel<?> ref, Function<T, List<R>> getter) {
		addField(new DInverseCollField<T, R>(this, name, column, ref, getter));
	}

	public int getFieldsCount() {
		// TODO Auto-generated method stub
		return 0;
	}
}
