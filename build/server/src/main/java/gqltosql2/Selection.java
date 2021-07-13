package gqltosql2;

import java.util.List;

import gqltosql.schema.DModel;

public class Selection {
	private DModel<?> type;
	private List<Field> fields;

	public DModel<?> getType() {
		return type;
	}

	public void setType(DModel<?> type) {
		this.type = type;
	}

	public List<Field> getFields() {
		return fields;
	}

	public void setFields(List<Field> fields) {
		this.fields = fields;
	}
}
