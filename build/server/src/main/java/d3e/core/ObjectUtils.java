package d3e.core;

import classes.ClassUtils;
import store.DatabaseObject;

public class ObjectUtils {

	static boolean isEquals(Object a, Object b) {
		if (a == b) {
			return true;
		}
		if (a == null || b == null) {
			return false;
		}
		if (ClassUtils.getClass(a) == ClassUtils.getClass(a) && a instanceof DatabaseObject) {
			return ((DatabaseObject) a).getId() == ((DatabaseObject) b).getId();
		}
		return false;
	}

	static boolean isNotEquals(Object a, Object b) {
		return !isEquals(a, b);
	}
}
