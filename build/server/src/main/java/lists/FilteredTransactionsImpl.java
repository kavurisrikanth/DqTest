package lists;

import classes.FilteredTransactions;
import classes.FilteredTransactionsIn;
import classes.FilteredTransactionsRequest;
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
public class FilteredTransactionsImpl extends AbsDataQueryImpl {
  @Autowired private EntityManager em;
  @Autowired private GqlToSql gqlToSql;

  public FilteredTransactionsRequest inputToRequest(FilteredTransactionsIn inputs) {
    FilteredTransactionsRequest request = new FilteredTransactionsRequest();
    request.amount = inputs.amount;
    return request;
  }

  public FilteredTransactions get(FilteredTransactionsIn inputs) {
    FilteredTransactionsRequest request = inputToRequest(inputs);
    return get(request);
  }

  public FilteredTransactions get(FilteredTransactionsRequest request) {
    List<NativeObj> rows = getNativeResult(request);
    List<Transaction> result = new ArrayList<>();
    for (NativeObj _r1 : rows) {
      result.add(NativeSqlUtil.get(em, _r1.getRef(1), Transaction.class));
    }
    FilteredTransactions wrap = new FilteredTransactions();
    wrap.items = result;
    return wrap;
  }

  public JSONObject getAsJson(Field field, FilteredTransactionsIn inputs) throws Exception {
    FilteredTransactionsRequest request = inputToRequest(inputs);
    return getAsJson(field, request);
  }

  public JSONObject getAsJson(Field field, FilteredTransactionsRequest request) throws Exception {
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

  public List<NativeObj> getNativeResult(FilteredTransactionsRequest request) {
    String sql = "select a._amount a0, a._id a1 from _transaction a where a._amount >= :param_0";
    Query query = em.createNativeQuery(sql);
    setParameter(query, "param_0", request.amount);
    this.logQuery(sql, query);
    List<NativeObj> result = NativeSqlUtil.createNativeObj(query.getResultList(), 1);
    return result;
  }
}
