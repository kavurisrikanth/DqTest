package rest.ws;

import java.io.ByteArrayOutputStream;

import org.springframework.web.socket.WebSocketSession;

import gqltosql.schema.DField;
import gqltosql.schema.DModel;

public class ClientSession {

	WebSocketSession session;
	ByteArrayOutputStream stream = new ByteArrayOutputStream();
	Template template;
	long userId;
	String userType;
	int[][] types = new int[10][];

	public ClientSession(WebSocketSession session) {
		this.session = session;
	}

	public String getId() {
		// TODO Auto-generated method stub
		return null;
	}

	public DModel<?> getType(int type) {
		// TODO Auto-generated method stub
		return null;
	}

	public DField<?, ?> getField(int type, int field) {
		// TODO Auto-generated method stub
		return null;
	}

	public int getClientIndex(DModel<?> actualType) {
		// TODO Auto-generated method stub
		return 0;
	}
}
