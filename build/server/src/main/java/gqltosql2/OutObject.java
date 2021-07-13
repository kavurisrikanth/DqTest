package gqltosql2;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import org.json.JSONException;

public class OutObject implements IOutValue {
	private long id;
	private Set<String> types = new HashSet<>();
	private Map<String, IOutValue> fields = new HashMap<>();
	private OutObject dup;

	public long getId() {
		return id;
	}

	public void addType(String type) {
		types.add(type);
	}

	public void setId(Long id) {
		this.id = id;
	}

	public int length() {
		return fields.size();
	}

	public void add(String field, IOutValue value) {
		fields.put(field, value);
	}

	public Map<String, IOutValue> getFields() {
		return fields;
	}

	public String getString(String name) {
		// TODO Auto-generated method stub
		return null;
	}

	public Long getLong(String field) {
		// TODO Auto-generated method stub
		return null;
	}

	public OutObject getObject(String fieldName) {
		// TODO Auto-generated method stub
		return null;
	}

	public void remove(String field) {
		// TODO Auto-generated method stub

	}

	public boolean isOfType(String type) {
		return types.contains(type);
	}

	public void duplicate(OutObject dup) {
		if (this.dup == dup) {
			return;
		}
		if (this.dup != null) {
			this.dup.duplicate(dup);
		} else {
			this.dup = dup;
		}
	}

	public OutObject getDuplicate() {
		return dup;
	}

	public void addCollectionField(String field, OutObjectList val) throws JSONException {
		add(field, val);
		if (dup != null) {
			dup.addCollectionField(field, val);
		}
	}

	public OutPrimitive getPrimitive(String fieldName) {
		// TODO Auto-generated method stub
		return null;
	}

	public boolean has(String name) {
		return fields.containsKey(name);
	}

	public IOutValue get(String name) {
		// TODO Auto-generated method stub
		return null;
	}
}
