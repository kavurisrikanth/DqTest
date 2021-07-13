package rest.ws;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Set;

import org.json.JSONException;

import classes.ClassUtils;
import d3e.core.DFile;
import gqltosql.schema.DField;
import gqltosql.schema.DModel;
import gqltosql.schema.IDataFetcher;
import gqltosql.schema.IModelSchema;
import store.DatabaseObject;

public class RocketObjectDataFetcher {

	private ClientSession session;
	private IModelSchema schema;
	private RocketMessage msg;

	public RocketObjectDataFetcher(IModelSchema schema, RocketMessage msg) {
		this.msg = msg;
	}

	public void fetch(UsageType usage, Object value) {
		fetchValue(usage, value);
	}

	private void fetchValue(UsageType usage, Object value) {
		if (value == null) {
			msg.writeNull();
		} else if (value instanceof DFile) {
			fetchDFile(usage, (DFile) value);
		} else if (value instanceof String) {
			msg.writeString((String) value);
		} else if (value instanceof Collection) {
			List<?> coll = new ArrayList<>((Collection<?>) value);
			msg.writeInt(coll.size());
			coll.forEach(v -> fetchValue(usage, v));
		} else if (value instanceof DatabaseObject) {
			DatabaseObject db = (DatabaseObject) value;
			fetchReferenceInternal(usage, db);
		} else {
			throw new RuntimeException("Unsupported type. " + value.getClass());
		}
	}

	private void fetchReference(UsageField field, Object value) {
		DModel<?> parent = schema.getType(ClassUtils.getClass(value).getSimpleName());
		int type = session.getClientIndex(parent);
		msg.writeInt(type);
		while (parent != null) {
			int idx = session.getClientIndex(parent);
			UsageType ut = field.getType(idx);
			if (ut != null) {
				fetchReferenceInternal(ut, value);
			}
			parent = parent.getParent();
		}
	}

	private void fetchReferenceInternal(UsageType usage, Object value) {
		for (UsageField f : usage.getFields()) {
			msg.writeInt(f.getField());
			DField df = session.getField(usage.getType(), f.getField());
			df.fetchValue(value, new DataFetcher(f));
		}
		msg.writeInt(-1);
	}

	private Object fetchDFile(UsageType usage, DFile value) {
		for (UsageField f : usage.getFields()) {
			try {
				msg.writeInt(f.getField());
				if (f.getField() == 0) {
					msg.writeString(value.getId());
				} else if (f.getField() == 1) {
					msg.writeString(value.getName());
				} else if (f.getField() == 2) {
					msg.writeLong(value.getSize());
				}
			} catch (JSONException e) {
				throw new RuntimeException(e);
			}
		}
		msg.writeInt(-1);
		return null;
	}

	private class DataFetcher implements IDataFetcher {

		private UsageField field;

		public DataFetcher(UsageField field) {
			this.field = field;
		}

		@Override
		public Object onPrimitiveValue(Object value) {
			if (value == null) {
				msg.writeNull();
			} else if (value instanceof DFile) {
				UsageType usage = field.getTypes()[0];
				fetchDFile(usage, (DFile) value);
			} else if (value instanceof String) {
				msg.writeString((String) value);
			} else {
				throw new RuntimeException("Unsupported type. " + value.getClass());
			}
			return null;
		}

		@Override
		public Object onReferenceValue(Object value) {
			if (value == null) {
				msg.writeNull();
				return null;
			}
			fetchReference(field, value);
			return null;
		}

		@Override
		public Object onEmbeddedValue(Object value) {
			return onReferenceValue(value);
		}

		@Override
		public Object onPrimitiveList(List<?> value) {
			msg.writeInt(value.size());
			value.forEach(v -> onPrimitiveValue(v));
			return null;
		}

		@Override
		public Object onReferenceList(List<?> value) {
			msg.writeInt(value.size());
			value.forEach(v -> onReferenceValue(v));
			return null;
		}

		@Override
		public Object onFlatValue(Set<?> value) {
			return onReferenceList(new ArrayList<>(value));
		}

		@Override
		public Object onInverseValue(List<?> value) {
			return onReferenceList(value);
		}
	}
}
