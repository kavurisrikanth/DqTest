package classes;

import org.hibernate.Hibernate;
import store.DatabaseObject;

public class ClassUtils {
  public static Class<?> getClass(Object obj) {
    if (obj instanceof DatabaseObject) {
      return Hibernate.getClass(obj);
    }
    return obj.getClass();
  }
}
