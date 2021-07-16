package lists;

import java.util.BitSet;
import java.util.function.BiConsumer;

import org.springframework.stereotype.Service;

import io.reactivex.rxjava3.disposables.Disposable;
import rest.ws.UsageType;
import store.DBObject;
import store.StoreEventType;

@Service
public class DataChangeTracker {
  Disposable listen(DBObject obj, UsageType usage, BiConsumer<DBObject, StoreEventType> listener) {
    return null;
  }
  Disposable listen(long type, BitSet fields, BiConsumer<DBObject, StoreEventType> listener) {
    return null;
  }
  void fire(DBObject object, StoreEventType changeType) {
  }
}
