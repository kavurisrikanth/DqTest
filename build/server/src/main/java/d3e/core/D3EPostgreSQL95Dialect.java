package d3e.core;

import org.hibernate.dialect.Dialect;
import org.hibernate.dialect.PostgreSQL95Dialect;
import org.hibernate.dialect.function.StandardSQLFunction;
import org.hibernate.dialect.unique.DefaultUniqueDelegate;
import org.hibernate.dialect.unique.UniqueDelegate;
import org.hibernate.mapping.UniqueKey;
import org.hibernate.type.StringType;

public class D3EPostgreSQL95Dialect extends PostgreSQL95Dialect {

	private UniqueDelegate unique;

	public D3EPostgreSQL95Dialect() {
		super();
		unique = new D3EDefaultUniqueDelegate(this);
		registerFunction("array_agg", new StandardSQLFunction("array_agg", StringType.INSTANCE));
	}

	@Override
	public UniqueDelegate getUniqueDelegate() {
		return unique;
	}

	@Override
	public String getAddForeignKeyConstraintString(String constraintName, String foreignKeyDefinition) {
		return super.getAddForeignKeyConstraintString(constraintName, foreignKeyDefinition)
				+ " DEFERRABLE INITIALLY DEFERRED";
	}

	@Override
	public String getAddForeignKeyConstraintString(String constraintName, String[] foreignKey, String referencedTable,
			String[] primaryKey, boolean referencesPrimaryKey) {
		return super.getAddForeignKeyConstraintString(constraintName, foreignKey, referencedTable, primaryKey,
				referencesPrimaryKey) + " DEFERRABLE INITIALLY DEFERRED";
	}
}

class D3EDefaultUniqueDelegate extends DefaultUniqueDelegate {

	public D3EDefaultUniqueDelegate(Dialect dialect) {
		super(dialect);
	}

	@Override
	protected String uniqueConstraintSql(UniqueKey uniqueKey) {
		return super.uniqueConstraintSql(uniqueKey) + " DEFERRABLE INITIALLY DEFERRED";
	}

}
