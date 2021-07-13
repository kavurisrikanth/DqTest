package gqltosql2;

public class OutPrimitive implements IOutValue {
	private Object val;

	public OutPrimitive(Object val) {
		this.val = val;
	}

	public Object getVal() {
		return val;
	}
}
