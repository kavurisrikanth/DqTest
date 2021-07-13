package gqltosql.schema;

import java.util.List;
import java.util.Set;

public interface IDataFetcher {

	Object onPrimitiveValue(Object value);

	Object onReferenceValue(Object value);

	Object onEmbeddedValue(Object value);

	Object onPrimitiveList(List<?> value);

	Object onReferenceList(List<?> value);

	Object onFlatValue(Set<?> value);

	Object onInverseValue(List<?> value);
}
