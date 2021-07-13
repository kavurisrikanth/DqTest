package gqltosql2;

import java.util.Collection;
import java.util.List;
import java.util.Set;

import org.json.JSONException;

import classes.ClassUtils;
import d3e.core.DFile;
import d3e.core.ListExt;
import gqltosql.schema.DField;
import gqltosql.schema.DModel;
import gqltosql.schema.FieldType;
import gqltosql.schema.IDataFetcher;
import gqltosql.schema.IModelSchema;
import store.DatabaseObject;

public class SqlOutObjectFetcher {

	private IModelSchema schema;

	public SqlOutObjectFetcher(IModelSchema schema) {
		this.schema = schema;
	}

	public IOutValue fetchValue(Field field, Object value, DModel<?> type) {
		if (value == null) {
			return null;
		}
		if (value instanceof DFile) {
			return fetchDFile(field, (DFile) value);
		}
		if (value instanceof DatabaseObject) {
			return fetchReference(field, value);
		}
		if (value instanceof Collection) {
			DField<?, ?> df = field.getField();
			List list = df.getType() == FieldType.PrimitiveCollection ? new OutPrimitiveList() : new OutObjectList();
			Collection<?> coll = (Collection<?>) value;
			coll.forEach(v -> list.add(fetchValue(field, v, type)));
			return (IOutValue) list;
		}
		return new OutPrimitive(value);
	}

	private OutObject fetchReference(Field field, Object value) {
		OutObject res = new OutObject();
		DModel<?> parent = schema.getType(ClassUtils.getClass(value).getSimpleName());
		while (parent != null) {
			DModel<?> type = parent;
			Selection selec = ListExt.firstWhere(field.getSelections(), s -> s.getType() == type);
			if (selec != null) {
				fetchReferenceInternal(selec, res, type, value);
			}
			parent = parent.getParent();
		}
		return res;
	}

	private OutObject fetchDFile(Field field, DFile value) {
		OutObject res = new OutObject();
		List<Selection> selections = field.getSelections();
		Selection selec = selections.get(0);
		for (Field f : selec.getFields()) {
			try {
				DField<?, ?> df = f.getField();
				if (df.getName().equals("id")) {
					res.add("id", new OutPrimitive(value.getId()));
				} else if (df.getName().equals("name")) {
					res.add("name", new OutPrimitive(value.getName()));
				} else if (df.getName().equals("size")) {
					res.add("size", new OutPrimitive(value.getSize()));
				}
			} catch (JSONException e) {
				throw new RuntimeException(e);
			}
		}
		return res;
	}

	private void fetchReferenceInternal(Selection set, OutObject res, DModel<?> type, Object value) {
		for (Field s : set.getFields()) {
			DField df = s.getField();
			try {
				IOutValue val = (IOutValue) df.fetchValue(value, new DataFetcherImpl(s, df));
				res.add(df.getName(), val);
			} catch (JSONException e) {
				throw new RuntimeException(e);
			}
		}
	}

	private class DataFetcherImpl implements IDataFetcher {

		private Field field;

		public DataFetcherImpl(Field field, DField<?, ?> df) {
			this.field = field;
		}

		@Override
		public IOutValue onPrimitiveValue(Object value) {
			if (value == null) {
				return null;
			} else if (value instanceof DFile) {
				return fetchDFile(field, (DFile) value);
			} else {
				return new OutPrimitive(value);
			}
		}

		@Override
		public OutObject onReferenceValue(Object value) {
			if (value == null) {
				return null;
			}
			return fetchReference(field, value);
		}

		@Override
		public Object onEmbeddedValue(Object value) {
			return onReferenceValue(value);
		}

		@Override
		public Object onPrimitiveList(List<?> value) {
			OutPrimitiveList list = new OutPrimitiveList();
			value.forEach(v -> list.add((OutPrimitive) onPrimitiveValue(v)));
			return list;
		}

		@Override
		public OutObjectList onReferenceList(List<?> value) {
			OutObjectList list = new OutObjectList();
			value.forEach(v -> list.add(onReferenceValue(v)));
			return list;
		}

		@Override
		public OutObjectList onFlatValue(Set<?> value) {
			OutObjectList list = new OutObjectList();
			value.forEach(v -> list.add(onReferenceValue(v)));
			return list;
		}

		@Override
		public Object onInverseValue(List<?> value) {
			return onReferenceList(value);
		}
	}
}
