package gqltosql.schema;

import java.util.List;

public interface IModelSchema {

	public List<DModel<?>> getAllTypes();
	
	public DModel<?> getType(String type);
}
