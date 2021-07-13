package gqltosql2;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.Function;

import javax.persistence.EntityManager;
import javax.persistence.Query;

import org.json.JSONException;

import d3e.core.D3ELogger;
import gqltosql.schema.DModel;
import gqltosql.schema.IModelSchema;

public class SqlAstNode {

	private Map<String, SqlTable> tables = new HashMap<>();
	private String type;
	private String table;
	private boolean needType;
	private String path;
	private boolean embedded;
	private IModelSchema schema;

	public SqlAstNode(IModelSchema schema, String path, String type, String table, boolean embedded) {
		this.schema = schema;
		this.path = path;
		this.type = type;
		this.table = table;
		this.embedded = embedded;
		tables.put(type, new SqlTable(type, table, embedded));
	}

	public SqlQueryContext createCtx() {
		SqlQueryContext ctx = new SqlQueryContext(this, -1);
		ctx.getQuery().setFrom(getTableName(), ctx.getFrom());
		ctx.getQuery().addWhere(ctx.getFrom() + "._id in ?1");
		return ctx;
	}

	public boolean isEmbedded() {
		return embedded;
	}

	public void setNeedType(boolean needType) {
		this.needType = needType;
	}

	public boolean needType() {
		return needType;
	}

	public void addColumn(DModel<?> type, ISqlColumn column) {
		SqlTable tbl = tables.get(type.getType());
		if (tbl == null) {
			tables.put(type.getType(), tbl = new SqlTable(type.getType(), type.getTableName(), type.isEmbedded()));
		}
		tbl.addColumn(column);
	}

	public String getTableName() {
		return table;
	}

	public String getType() {
		return type;
	}

	public Map<String, SqlTable> getTables() {
		return tables;
	}

	public String getPath() {
		return path;
	}

	@Override
	public String toString() {
		return type;
	}

	public void selectColumns(SqlQueryContext ctx) {
		if (needType()) {
			StringBuilder b = new StringBuilder();
			b.append("(case");
			List<String> allTypes = new ArrayList<>(getTables().keySet());
			sortTypes(allTypes);
			allTypes.forEach((t) -> b.append(" when ").append(ctx.getTableAlias(t)).append("._id is not null then '")
					.append(t).append('\''));
			b.append(" else 'no-type' end)");
			ctx.addSelection(b.toString(), "__typename");
		}
		SqlQueryContext typeCtx = ctx.subType(getType());
		getTables().get(getType()).addSelections(typeCtx);

		getTables().forEach((type, table) -> {
			if (type.equals(getType())) {
				return;
			}
			SqlQueryContext sub = ctx.subType(type);
			String join = sub.getFrom();
			sub.addJoin(table.getTableName(), join, join + "._id = " + typeCtx.getFrom() + "._id");
			table.addSelections(sub);
		});
	}

	private void sortTypes(List<String> types) {
		Map<String, String> byType = new HashMap<>();
		for (String type : types) {
			DModel<?> dm = schema.getType(type);
			String path = getPath(dm);
			byType.put(type, path);
		}
		types.sort((a, b) -> byType.get(b).compareTo(byType.get(a)));
	}

	private String getPath(DModel<?> dm) {
		if (dm == null) {
			return "";
		}
		DModel<?> parent = dm.getParent();
		return getPath(parent) + "." + dm.getType();
	}

	public OutObjectList executeQuery(EntityManager em, Set<Long> ids, Map<Long, OutObject> byId) throws Exception {
		if (ids.isEmpty()) {
			return new OutObjectList();
		}
		SqlQueryContext ctx = createCtx();
		ctx.addSqlColumns(this);
		SqlQuery query = ctx.getQuery();
		String sql = query.createSQL();
		D3ELogger.displaySql(getPath(), sql, ids);
		Query q = em.createNativeQuery(sql);
		q.setParameter(1, ids);
		List<?> rows = q.getResultList();
		QueryReader reader = query.getReader();
		OutObjectList result = new OutObjectList();
		for (Object r : rows) {
			OutObject obj = reader.read(r, byId);
			result.add(obj);
		}
		if (!result.isEmpty()) {
			executeSubQuery(em, (t) -> result);
		}
		return result;
	}

	public void executeSubQuery(EntityManager em, Function<String, List<OutObject>> listSupplier) throws Exception {
		for (Map.Entry<String, SqlTable> e : tables.entrySet()) {
			String type = e.getKey();
			SqlTable table = e.getValue();
			List<OutObject> apply = listSupplier.apply(type);
			for (ISqlColumn c : table.getColumns()) {
				c.extractDeepFields(em, schema, type, apply);
				SqlAstNode sub = c.getSubQuery();
				if (sub != null) {
					Map<Long, OutObject> objById = new HashMap<>();
					apply.forEach(o -> {
						try {
							OutObject row = objById.get(o.getId());
							if (row != null) {
								row.duplicate(o);
							} else {
								objById.put(o.getId(), o);
							}
						} catch (JSONException ex) {
						}
					});
					OutObjectList array = sub.executeQuery(em, objById.keySet(), objById);
					c.updateSubField(objById, array);
				}
			}

		}
	}
}
