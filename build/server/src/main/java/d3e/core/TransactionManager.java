package d3e.core;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;

import store.DataStoreEvent;
import store.StoreEventType;

public class TransactionManager {

	private static ThreadLocal<TransactionManager> threadLocal = new ThreadLocal<>();
	
	private List<Object> added = new ArrayList<>();
	private List<Object> updated = new ArrayList<>();
	private List<Object> deleted = new ArrayList<>();

	public void add(Object obj) {
		if (added.contains(obj)) {
			return;
		}
		if (updated.contains(obj)) {
			return;
		}
		if (deleted.contains(obj)) {
			deleted.remove(obj);
		}
		added.add(obj);
	}

	public void update(Object obj) {
		if (added.contains(obj)) {
			return;
		}
		if (updated.contains(obj)) {
			return;
		}
		if (deleted.contains(obj)) {
			throw new RuntimeException("Object was deleted");
		}
		updated.add(obj);
	}

	public void delete(Object obj) {
		if (added.contains(obj)) {
			added.remove(obj);
			return;
		}
		if (updated.contains(obj)) {
			updated.remove(obj);
			return;
		}
		deleted.add(obj);
	}

	public void commit(Consumer<DataStoreEvent> onEvent) {
		List<Object> addList = new ArrayList<>(added);
		List<Object> updateList = new ArrayList<>(updated);
		List<Object> deleteList = new ArrayList<>(deleted);

		addList.forEach(a -> {
			DataStoreEvent event = new DataStoreEvent(a);
			event.setEntity(a);
			event.setType(StoreEventType.Insert);
			onEvent.accept(event);
		});

		updateList.forEach(a -> {
			DataStoreEvent event = new DataStoreEvent(a);
			event.setEntity(a);
			event.setType(StoreEventType.Update);
			onEvent.accept(event);
		});

		deleteList.forEach(a -> {
			DataStoreEvent event = new DataStoreEvent(a);
			event.setEntity(a);
			event.setType(StoreEventType.Delete);
			onEvent.accept(event);
		});
	}

	public boolean isEmpty() {
		return added.isEmpty() && updated.isEmpty() && deleted.isEmpty();
	}

	public boolean getIsEmpty() {
		return isEmpty();
	}

	public static TransactionManager get() {
		return threadLocal.get();
	}
	
	public static void set(TransactionManager manager) {
		threadLocal.set(manager);
	}
	
	public static void remove() {
		threadLocal.remove();
	}

}
