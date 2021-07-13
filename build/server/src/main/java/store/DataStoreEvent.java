package store;

import org.springframework.context.ApplicationEvent;

public class DataStoreEvent extends ApplicationEvent {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	
	private StoreEventType type;
	private Object entity;
	
	public DataStoreEvent(Object source) {
		super(source);
		this.entity = source;
	}

	public StoreEventType getType() {
		return type;
	}

	public void setType(StoreEventType type) {
		this.type = type;
	}

	public Object getEntity() {
		return entity;
	}

	public void setEntity(Object entity) {
		this.entity = entity;
	}


}
