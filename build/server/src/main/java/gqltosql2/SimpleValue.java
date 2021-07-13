package gqltosql2;

public class SimpleValue implements IValue {

	private String field;
	private int index;

	public SimpleValue(String field, int index) {
		this.field = field;
		this.index = index;
	}

	@Override
	public OutPrimitive read(Object[] row, OutObject obj) throws Exception {
		OutPrimitive val = new OutPrimitive(row[index]);
		obj.add(field, val);
		return val;
	}

	@Override
	public String toString() {
		return field;
	}
}
