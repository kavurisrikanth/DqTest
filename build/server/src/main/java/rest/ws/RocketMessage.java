package rest.ws;

import java.io.ByteArrayOutputStream;
import java.nio.ByteBuffer;
import java.util.List;

import graphql.language.Field;

public class RocketMessage {

	private StringTable table;
	private ByteBuffer stream;

	public RocketMessage(StringTable table, ByteArrayOutputStream stream) {
		this.table = table;
		this.stream = ByteBuffer.wrap(stream.toByteArray());
	}

	public int readByte() {
		// TODO Auto-generated method stub
		return 0;
	}

	public String readString() {
		// TODO Auto-generated method stub
		return null;
	}

	public Field readField() {
		// TODO Auto-generated method stub
		return null;
	}

	public int readInt() {
		// TODO Auto-generated method stub
		return 0;
	}

	public void writeInt(int val) {
		// TODO Auto-generated method stub

	}

	public void writeByte(int val) {
		// TODO Auto-generated method stub

	}

	public void writeStringList(List<String> list) {
		// TODO Auto-generated method stub

	}

	public void writeString(String val) {
		// TODO Auto-generated method stub

	}

	public int readShort() {
		// TODO Auto-generated method stub
		return 0;
	}

	public boolean readBoolean() {
		// TODO Auto-generated method stub
		return false;
	}

	public Object readObject() {
		// TODO Auto-generated method stub
//		0/more -> Index of the item; continue object reading
//		-1 -> add to end if the list; continue object reading
//		-2 -> complete list. count and elements 
//		-3 -> remove, next id
//		-4 -> No changes in collection, continue object reading
		return null;
	}

	public void writeLong(long k) {
		// TODO Auto-generated method stub

	}

	public long readLong() {
		// TODO Auto-generated method stub
		return 0;
	}

	public String readType() {
		// TODO Auto-generated method stub
		return null;
	}

	public void writeNull() {
		// TODO Auto-generated method stub

	}

	public void writeField(String name) {
		// TODO Auto-generated method stub

	}

	public void writeIntegerList(List<Integer> ints) {
		// TODO Auto-generated method stub

	}

	public ByteBuffer getOutput() {
		// TODO Auto-generated method stub
		return null;
	}

}
