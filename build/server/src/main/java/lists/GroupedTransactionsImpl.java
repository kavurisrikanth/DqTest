package lists;

import classes.GTxn;
import classes.GroupedTransactions;
import classes.GroupedTransactionsIn;
import classes.GroupedTransactionsRequest;
import d3e.core.IterableExt;
import d3e.core.ListExt;
import gqltosql.GqlToSql;
import gqltosql.SqlRow;
import graphql.language.Field;
import java.util.ArrayList;
import java.util.List;
import javax.persistence.EntityManager;
import javax.persistence.Query;
import models.Customer;
import models.Transaction;
import org.json.JSONArray;
import org.json.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import rest.AbstractQueryService;

@Service
public class GroupedTransactionsImpl extends AbsDataQueryImpl {
  @Autowired private EntityManager em;
  @Autowired private GqlToSql gqlToSql;

  public GroupedTransactionsRequest inputToRequest(GroupedTransactionsIn inputs) {
    GroupedTransactionsRequest request = new GroupedTransactionsRequest();
    request.amount = inputs.amount;
    return request;
  }

  public GroupedTransactions get(GroupedTransactionsIn inputs) {
    GroupedTransactionsRequest request = inputToRequest(inputs);
    return get(request);
  }

  public GroupedTransactions get(GroupedTransactionsRequest request) {
    List<NativeObj> rows = getNativeResult(request);
    List<GTxn> result = new ArrayList<>();
    for (NativeObj _r1 : rows) {
      GTxn _o1 = new GTxn();
      result.add(_o1);
      _o1.customer = NativeSqlUtil.get(em, _r1.getRef(0), Customer.class);
      _o1.txns = NativeSqlUtil.getList(em, _r1.getListRef(1), Transaction.class);
    }
    GroupedTransactions wrap = new GroupedTransactions();
    wrap.items = result;
    return wrap;
  }

  public JSONObject getAsJson(Field field, GroupedTransactionsIn inputs) throws Exception {
    GroupedTransactionsRequest request = inputToRequest(inputs);
    return getAsJson(field, request);
  }

  public JSONObject getAsJson(Field field, GroupedTransactionsRequest request) throws Exception {
    List<NativeObj> rows = getNativeResult(request);
    return getAsJson(field, rows);
  }

  public JSONObject getAsJson(Field field, List<NativeObj> rows) throws Exception {
    JSONArray array = new JSONArray();
    List<SqlRow> sqlDecl0 = new ArrayList<>();
    List<SqlRow> sqlDecl1 = new ArrayList<>();
    for (NativeObj _r1 : rows) {
      JSONObject _o1 = new JSONObject();
      array.put(_o1);
      _o1.put("customer", NativeSqlUtil.getJSONObject(_r1.getRef(0), sqlDecl0));
      _o1.put("txns", NativeSqlUtil.getJSONArray(_r1.getListRef(1), sqlDecl1));
    }
    gqlToSql.execute("Customer", AbstractQueryService.inspect(field, "customer"), sqlDecl0);
    gqlToSql.execute("Transaction", AbstractQueryService.inspect(field, "txns"), sqlDecl1);
    JSONObject result = new JSONObject();
    result.put("items", array);
    return result;
  }

  public List<NativeObj> processMap(List<NativeObj> result, GroupedTransactionsRequest inputs) {
    return IterableExt.toList(
        ListExt.map(
            result,
            (s) -> {
              NativeObj o = new NativeObj(2);
              o.set(0, s.getRef(0));
              o.set(
                  1,
                  ListExt.orderBy(
                      NativeObj.getListStruct(s.getListRef(1), s.getListDouble(2)),
                      (t) -> {
                        return t.getDouble(1);
                      },
                      true));
              return o;
            }));
  }

  public List<NativeObj> getNativeResult(GroupedTransactionsRequest request) {
    String sql =
        "select a._customer_id a0, cast(array_agg(a._id) as text) a1, cast(array_agg(a._amount) as text) a2 from _transaction a where a._amount >= :param_0 group by a._customer_id";
    Query query = em.createNativeQuery(sql);
    setParameter(query, "param_0", request.amount);
    this.logQuery(sql, query);
    List<NativeObj> result = NativeSqlUtil.createNativeObj(query.getResultList(), 0);
    result = processMap(result, request);
    return result;
  }
}
