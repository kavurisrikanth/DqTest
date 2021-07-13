package gqltosql2;

import java.util.List;

import gqltosql.schema.DField;

public class Field {

	private DField<?, ?> field;
	private List<Selection> selections;

	public void setField(DField<?, ?> field) {
		this.field = field;
	}

	public DField<?, ?> getField() {
		return field;
	}

	public void setSelections(List<Selection> selections) {
		this.selections = selections;
	}

	public List<Selection> getSelections() {
		return selections;
	}
}
