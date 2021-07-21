package lists;

import classes.FilteredTransactions2;
import classes.FilteredTransactions2In;
import classes.FilteredTransactions2Request;
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
public class FilteredTransactions2Impl extends AbsDataQueryImpl {
  @Autowired private EntityManager em;
  @Autowired private GqlToSql gqlToSql;

  public FilteredTransactions2Request inputToRequest(FilteredTransactions2In inputs) {
    FilteredTransactions2Request request = new FilteredTransactions2Request();
    request.amount = inputs.amount;
    return request;
  }

  public FilteredTransactions2 get(FilteredTransactions2In inputs) {
    FilteredTransactions2Request request = inputToRequest(inputs);
    return get(request);
  }

  public FilteredTransactions2 get(FilteredTransactions2Request request) {
    List<NativeObj> rows = getNativeResult(request);
    List<Transaction> result = new ArrayList<>();
    for (NativeObj _r1 : rows) {
      result.add(NativeSqlUtil.get(em, _r1.getRef(3), Transaction.class));
    }
    FilteredTransactions2 wrap = new FilteredTransactions2();
    wrap.items = result;
    return wrap;
  }

  public JSONObject getAsJson(Field field, FilteredTransactions2In inputs) throws Exception {
    FilteredTransactions2Request request = inputToRequest(inputs);
    return getAsJson(field, request);
  }

  public JSONObject getAsJson(Field field, FilteredTransactions2Request request) throws Exception {
    List<NativeObj> rows = getNativeResult(request);
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

  public List<NativeObj> getNativeResult(FilteredTransactions2Request request) {
    String sql =
        "select a._amount a0, b._gender a1, a._customer_id a2, a._id a3 from _transaction a left join _customer b on b._id = a._customer_id where a._amount >= :param_0 and b._gender = :param_1";
    Query query = em.createNativeQuery(sql);
    setParameter(query, "param_0", request.amount);
    setParameter(query, "param_1", Gender.Female);
    this.logQuery(sql, query);
    List<NativeObj> result = NativeSqlUtil.createNativeObj(query.getResultList(), 3);
    return result;
  }
}
