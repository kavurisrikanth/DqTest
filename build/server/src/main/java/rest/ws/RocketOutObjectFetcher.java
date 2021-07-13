package rest.ws;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import org.json.JSONException;

import d3e.core.DFile;
import gqltosql.schema.DField;
import gqltosql.schema.IModelSchema;
import gqltosql2.IOutValue;
import gqltosql2.OutObject;
import gqltosql2.OutPrimitive;

public class RocketOutObjectFetcher {

	private ClientSession session;
	private RocketMessage msg;

	public RocketOutObjectFetcher(IModelSchema schema, RocketMessage msg) {
		// TODO Auto-generated constructor stub
	}

	public void fetch(UsageType usage, Object value) {
		fetchValue(usage, value);
	}

	private void fetchValue(UsageType usage, Object value) {
		if (value == null) {
			msg.writeNull();
		} else if (value instanceof OutPrimitive) {
			fetchPrimitive(usage, (OutPrimitive) value);
		} else if (value instanceof Collection) {
			List<?> coll = new ArrayList<>((Collection<?>) value);
			msg.writeInt(coll.size());
			coll.forEach(v -> fetchValue(usage, v));
		} else if (value instanceof OutObject) {
			OutObject db = (OutObject) value;
			fetchReferenceInternal(usage, db);
		} else {
			throw new RuntimeException("Unsupported type. " + value.getClass());
		}
	}

	private void fetchPrimitive(UsageType usage, OutPrimitive value) {
		Object val = value.getVal();
		if (val == null) {
			msg.writeNull();
		} else if (val instanceof DFile) {
			fetchDFile(usage, (DFile) val);
		} else if (val instanceof String) {
			msg.writeString((String) val);
		} else {
			throw new RuntimeException("Unsupported type. " + val.getClass());
		}
	}

	private void fetchDFile(UsageType usage, DFile value) {
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
	}

	private void fetchReferenceInternal(UsageType usage, OutObject value) {
		for (UsageField f : usage.getFields()) {
			msg.writeInt(f.getField());
			DField df = session.getField(usage.getType(), f.getField());
			IOutValue val = value.get(df.getName());
			fetchFieldValue(f, val);
		}
		msg.writeInt(-1);
	}

	private void fetchFieldValue(UsageField field, IOutValue value) {
		if (value == null) {
			msg.writeNull();
		} else if (value instanceof OutPrimitive) {
			UsageType[] types = field.getTypes();
			fetchPrimitive(types.length == 0 ? null : types[0], (OutPrimitive) value);
		} else if (value instanceof Collection) {
			List<?> coll = new ArrayList<>((Collection<?>) value);
			msg.writeInt(coll.size());
			coll.forEach(v -> fetchFieldValue(field, (IOutValue) v));
		} else if (value instanceof OutObject) {
			OutObject db = (OutObject) value;
			UsageType[] types = field.getTypes();
			for (UsageType type : types) {
				fetchReferenceInternal(type, db);
			}
		} else {
			throw new RuntimeException("Unsupported type. " + value.getClass());
		}
	}
}
