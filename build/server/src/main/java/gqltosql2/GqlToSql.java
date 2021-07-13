package gqltosql2;

import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.persistence.EntityManager;

import d3e.core.SetExt;
import gqltosql.schema.DField;
import gqltosql.schema.DFlatField;
import gqltosql.schema.DModel;
import gqltosql.schema.DModelType;
import gqltosql.schema.IModelSchema;

public class GqlToSql {

	private IModelSchema schema;
	private EntityManager em;

	public GqlToSql(EntityManager em, IModelSchema schema) {
		this.em = em;
		this.schema = schema;
	}

	public OutObjectList execute(DModel<?> type, Field field, List<OutObject> objs) throws Exception {
		if (objs.isEmpty()) {
			return new OutObjectList();
		}
		SqlAstNode sqlNode = prepareSqlNode(field.getSelections(), type);
		Set<Long> ids = new HashSet<>();
		Map<Long, OutObject> byId = new HashMap<>();
		for (OutObject obj : objs) {
			long id = obj.getLong("id");
			ids.add(id);
			byId.put(id, obj);
		}
		OutObjectList result = sqlNode.executeQuery(em, ids, byId);
		return result;
	}

	public OutObject execute(DModel<?> type, Field field, Long id) throws Exception {
		OutObjectList array = execute(type, field, SetExt.asSet(id));
		if (array.size() != 0) {
			return array.get(0);
		}
		return null;
	}

	public OutObjectList execute(DModel<?> type, Field field, Set<Long> ids) {
		try {
			SqlAstNode sqlNode = prepareSqlNode(field.getSelections(), type);
			OutObjectList result = sqlNode.executeQuery(em, ids, new HashMap<>());
			return result;
		} catch (Exception e) {
			e.printStackTrace();
			return new OutObjectList();
		}
	}

	private SqlAstNode prepareSqlNode(List<Selection> selections, DModel<?> type) {
		SqlAstNode node = new SqlAstNode(schema, "this", type.getType(), type.getTableName(), type.isEmbedded());
		addReferenceField(node, selections, type);
		return node;
	}

	private void addField(SqlAstNode node, Field field, DModel<?> parentType, DModel<?> nonEmbeddedModel) {
		DField<?, ?> df = field.getField();
		if (df == null) {
			return;
		}
		switch (df.getType()) {
		case Primitive:
			addPrimitiveField(node, field, df);
			break;
		case Reference:
			addReferenceField(node, field, df, nonEmbeddedModel);
			break;
		case PrimitiveCollection:
			addPrimitiveCollectionField(node, field, df);
			break;
		case ReferenceCollection:
			addReferenceCollectionField(node, field, df, nonEmbeddedModel);
			break;
		case InverseCollection:
			addInverseCollectionField(node, field, df, nonEmbeddedModel);
			break;
		default:
			break;
		}
	}

	private void addPrimitiveCollectionField(SqlAstNode node, Field field, DField<?, ?> df) {
		DModel<?> dcl = df.declType();
		SqlAstNode sub = new SqlPrimitiveCollAstNode(schema, node.getPath() + "." + df.getName(),
				df.getCollTableName(dcl.getTableName()),
				dcl.getTableName() + (dcl.getParent() != null ? "_" : "") + "_id", df.getColumnName(), df.getName());
		addColumn(node, df, new CollSqlColumn(sub, df.getName()));
	}

	private void addReferenceCollectionField(SqlAstNode node, Field field, DField<?, ?> df,
			DModel<?> nonEmbeddedModel) {
		DModel<?> dm = df.getReference();
		if (dm.getModelType() == DModelType.DOCUMENT) {
			addColumn(node, df, new DocumentFlatSqlColumn(field, (DFlatField<?, ?>) df));
		} else if (df.declType().getModelType() == DModelType.EMBEDDED) {
			DModel<?> dcl = nonEmbeddedModel;
			SqlCollAstNode sub = new SqlCollAstNode(schema, node.getPath() + "." + df.getName(), dm.getType(),
					dm.getTableName(), df.getCollTableName(dcl.getTableName()),
					dcl.getTableName() + (dcl.getParent() != null ? "_" : "") + "_id", df.getColumnName());
			addReferenceField(sub, field.getSelections(), nonEmbeddedModel);
			addColumn(node, df, new RefCollSqlColumn(sub, df.getName()));
		} else if (dm.getModelType() == DModelType.ENTITY) {
			DModel<?> dcl = df.declType();
			SqlCollAstNode sub = new SqlCollAstNode(schema, node.getPath() + "." + df.getName(), dm.getType(),
					dm.getTableName(), df.getCollTableName(dcl.getTableName()),
					dcl.getTableName() + (dcl.getParent() != null ? "_" : "") + "_id", df.getColumnName());
			addReferenceField(sub, field.getSelections(), df.getReference());
			addColumn(node, df, new RefCollSqlColumn(sub, df.getName()));
		}
	}

	private void addInverseCollectionField(SqlAstNode node, Field field, DField<?, ?> df, DModel<?> nonEmbeddedModel) {
		DModel<?> dm = df.getReference();
		if (dm.getModelType() == DModelType.DOCUMENT) {
			addColumn(node, df, new DocumentFlatSqlColumn(field, (DFlatField<?, ?>) df));
		} else if (dm.getModelType() == DModelType.EMBEDDED) {
			SqlInverseCollAstNode sub = new SqlInverseCollAstNode(schema, node.getPath() + "." + df.getName(),
					dm.getType(), dm.getTableName(), df.getColumnName());
			addReferenceField(sub, field.getSelections(), nonEmbeddedModel);
			addColumn(node, df, new RefCollSqlColumn(sub, df.getName()));
		} else if (dm.getModelType() == DModelType.ENTITY || dm.getModelType() == DModelType.EMBEDDED) {
			SqlInverseCollAstNode sub = new SqlInverseCollAstNode(schema, node.getPath() + "." + df.getName(),
					dm.getType(), dm.getTableName(), df.getColumnName());
			addReferenceField(sub, field.getSelections(), df.getReference());
			addColumn(node, df, new RefCollSqlColumn(sub, df.getName()));
		}
	}

	private void addReferenceField(SqlAstNode node, Field field, DField<?, ?> df, DModel<?> nonEmbeddedModel) {
		DModel<?> dm = df.getReference();
		if (dm.getModelType() == DModelType.DOCUMENT) {
			addColumn(node, df, new DocumentSqlColumn(field, df));
		} else if (dm.getModelType() == DModelType.EMBEDDED) {
			SqlAstNode sub = new SqlAstNode(schema, node.getPath() + "." + df.getName(), dm.getType(),
					dm.getTableName(), dm.isEmbedded());
			addReferenceField(sub, field.getSelections(), nonEmbeddedModel);
			addColumn(node, df, new RefSqlColumn(sub, df.getColumnName(), df.getName()));
		} else if (dm.getModelType() == DModelType.ENTITY) {
			SqlAstNode sub = new SqlAstNode(schema, node.getPath() + "." + df.getName(), dm.getType(),
					dm.getTableName(), dm.isEmbedded());
			addReferenceField(sub, field.getSelections(), df.getReference());
			addColumn(node, df, new RefSqlColumn(sub, df.getColumnName(), df.getName()));
		}
	}

	private void addColumn(SqlAstNode node, DField<?, ?> df, ISqlColumn column) {
		node.addColumn(df.declType(), column);
	}

	private void addReferenceField(SqlAstNode node, List<Selection> selections, DModel<?> nonEmbeddedModel) {
		node.setNeedType(true);
		for (Selection selection : selections) {
			DModel<?> parentType = selection.getType();
			for (Field field : selection.getFields()) {
				addField(node, field, parentType, nonEmbeddedModel);
			}
		}
	}

	private void addPrimitiveField(SqlAstNode node, Field field, DField<?, ?> df) {
		addColumn(node, df, new SqlColumn(df.getColumnName(), df.getName()));
	}
}
