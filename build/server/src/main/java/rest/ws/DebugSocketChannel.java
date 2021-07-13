package rest.ws;

public class DebugSocketChannel implements SocketChannel {

	@Override
	public void _message(ClientSession client, RocketMessage msg) {
		String method = msg.readString();
		switch (method) {
		case "onBreakPoint":
			onBreakPoint(client, 0, 0);
			break;
		}
	}

	public void onBreakPoint(ClientSession client, long line, int c) {

	}
}
