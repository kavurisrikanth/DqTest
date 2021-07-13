package lists;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.Date;
import java.util.Map;

import javax.persistence.Query;
import javax.persistence.TemporalType;

import d3e.core.D3ELogger;
import d3e.core.MapExt;
import store.DatabaseObject;

public abstract class AbsDataQueryImpl {
	protected void setParameter(Query query, String name, DatabaseObject value) {
		if (value == null) {
			query.setParameter(name, 0l);
		} else {
			query.setParameter(name, value.getId());
		}
	}

	protected void setParameter(Query query, String name, Enum<?> value) {
		if (value == null) {
			query.setParameter(name, "");
		} else {
			query.setParameter(name, value.name());
		}
	}

	protected void setParameter(Query query, String name, Object value) {
		query.setParameter(name, value);
	}

	protected void setParameter(Query query, String name, LocalDate value) {
		if(value == null) {
			query.setParameter(name, (Date)null, TemporalType.DATE);
		} else {
			query.setParameter(name, value);
			
		}
	}
	
	protected void setParameter(Query query, String name, LocalDateTime value) {
		if(value == null) {
			query.setParameter(name, (Date)null, TemporalType.TIMESTAMP);
		} else {
			query.setParameter(name, value);
		}
	}
		
	protected void setParameter(Query query, String name, LocalTime value) {
		if(value == null) {
			query.setParameter(name, (Date)null, TemporalType.TIME);
		} else {
			query.setParameter(name, value);
		}
	}
	
	protected void setParameter(Query query, String name, String value) {
		if (value == null) {
			query.setParameter(name, "");
			return;
		}
		query.setParameter(name, value);
	}
	
	protected void assertLimitNotNegative(long limit) {
		if (limit < 0) {
			throw new RuntimeException("Limit is negative.");
		}
	}

	protected void logQuery(String sql, Query query) {
		D3ELogger.query(sql, query);
	}
}
