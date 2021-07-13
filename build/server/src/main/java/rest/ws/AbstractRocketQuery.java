package rest.ws;

import graphql.language.Field;
import graphql.language.Selection;

public class AbstractRocketQuery {


	protected QueryResult singleResult(String type, boolean external, Object value) {
		QueryResult r = new QueryResult();
		r.type = type;
		r.external = external;
		r.value = value;
		return r;
	}
	
	protected QueryResult listResult(String type, boolean external, Object value) {
		QueryResult r = new QueryResult();
		r.type = type;
		r.external = external;
		r.isList = true;
		r.value = value;
		return r;
	}


	protected static Field inspect(Field field, String path) {
		if (path.isEmpty()) {
			return field;
		}
		String[] subFields = path.split("\\.");
		return inspect(field, 0, subFields);
	}

	protected static Field inspect(Field field, int i, String... subFields) {
		if (i == subFields.length) {
			return field;
		}
		for (Selection<?> s : field.getSelectionSet().getSelections()) {
			if (s instanceof Field) {
				Field f = (Field) s;
				if (f.getName().equals(subFields[i])) {
					return inspect(f, i + 1, subFields);
				}
			}
		}
		return null;
	}
}
