package rest.ws;

import java.util.Map;

import store.EntityHelperService;

public class RocketInputContext {

	private RocketMessage msg;
	private EntityHelperService helperService;

	public RocketInputContext(EntityHelperService helperService, RocketMessage msg) {
		this.helperService = helperService;
		this.msg = msg;
	}

	/**
	 * Read all the fields of given type and put them in Map by field name;
	 * 
	 * @param type
	 * @return
	 */
	public Map<String, Object> readFields(String type) {
		// TODO Auto-generated method stub
		return null;
	}

	public long readLong() {
		// TODO Auto-generated method stub
		return 0;
	}

	public <T> T readObject(Class<T> cls) {
		// TODO Auto-generated method stub
		return null;
	}

	public String readString() {
		// TODO Auto-generated method stub
		return null;
	}

}
