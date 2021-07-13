package gqltosql2;

import java.util.List;
import java.util.Map;

import javax.persistence.EntityManager;

import gqltosql.schema.IModelSchema;

public interface ISqlColumn {

	String getFieldName();

	void addColumn(SqlTable table, SqlQueryContext ctx);

	SqlAstNode getSubQuery();

	void updateSubField(Map<Long, OutObject> parents, List<OutObject> all) throws Exception;

	void extractDeepFields(EntityManager em, IModelSchema schema, String type, List<OutObject> rows) throws Exception;
}
