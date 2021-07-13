package rest.ws;

import gqltosql.schema.DField;
import gqltosql.schema.DModel;

public class TemplateType {

	private DModel<?> model;
	private DField<?, ?> fields[];

	public TemplateType(DModel<?> model, int length) {
		this.model = model;
		this.fields = new DField<?, ?>[length];
	}

	public void addField(int idx, DField<?, ?> field) {
		fields[idx] = field;
	}
}
