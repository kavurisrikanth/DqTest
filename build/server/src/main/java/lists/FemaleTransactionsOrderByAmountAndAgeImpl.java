package lists;

import classes.FemaleTransactionsOrderByAmountAndAge;
import classes.Gender;
import gqltosql.GqlToSql;
import gqltosql.SqlRow;
import graphql.language.Field;
import java.util.ArrayList;
import java.util.List;
import javax.persistence.EntityManager;
import javax.persistence.Query;
import models.Transaction;
import org.json.JSONArray;
import org.json.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import rest.AbstractQueryService;

@Service
public class FemaleTransactionsOrderByAmountAndAgeImpl extends AbsDataQueryImpl {
  @Autowired private EntityManager em;
  @Autowired private GqlToSql gqlToSql;

  public FemaleTransactionsOrderByAmountAndAge get() {
    List<NativeObj> rows = getNativeResult();
    List<Transaction> result = new ArrayList<>();
    for (NativeObj _r1 : rows) {
      result.add(NativeSqlUtil.get(em, _r1.getRef(4), Transaction.class));
    }
    FemaleTransactionsOrderByAmountAndAge wrap = new FemaleTransactionsOrderByAmountAndAge();
    wrap.items = result;
    return wrap;
  }

  public JSONObject getAsJson(Field field) throws Exception {
    List<NativeObj> rows = getNativeResult();
    return getAsJson(field, rows);
  }

  public JSONObject getAsJson(Field field, List<NativeObj> rows) throws Exception {
    JSONArray array = new JSONArray();
    List<SqlRow> sqlDecl0 = new ArrayList<>();
    for (NativeObj _r1 : rows) {
      array.put(NativeSqlUtil.getJSONObject(_r1, sqlDecl0));
    }
    gqlToSql.execute("Transaction", AbstractQueryService.inspect(field, ""), sqlDecl0);
    JSONObject result = new JSONObject();
    result.put("items", array);
    return result;
  }

  public List<NativeObj> getNativeResult() {
    String sql =
        "select b._gender a0, a._amount a1, b._age a2, a._customer_id a3, a._id a4 from _transaction a left join _customer b on b._id = a._customer_id where b._gender = :param_0 order by a._amount, b._age desc";
    Query query = em.createNativeQuery(sql);
    setParameter(query, "param_0", Gender.Female);
    this.logQuery(sql, query);
    List<NativeObj> result = NativeSqlUtil.createNativeObj(query.getResultList(), 4);
    return result;
  }
}
