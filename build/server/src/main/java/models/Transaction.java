package models;

import d3e.core.CloneContext;
import java.util.List;
import java.util.function.Consumer;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.ManyToOne;
import org.apache.solr.client.solrj.beans.Field;
import org.hibernate.annotations.ColumnDefault;
import org.springframework.data.solr.core.mapping.SolrDocument;
import store.DatabaseObject;
import store.ICloneable;

@SolrDocument(collection = "Transaction")
@Entity
public class Transaction extends CreatableObject {
  @Field
  @ManyToOne(fetch = FetchType.LAZY)
  private Customer customer;

  @Field
  @ColumnDefault("0.0")
  private double amount = 0.0d;

  private transient Transaction old;

  public void updateMasters(Consumer<DatabaseObject> visitor) {
    super.updateMasters(visitor);
  }

  public Customer getCustomer() {
    return this.customer;
  }

  public void setCustomer(Customer customer) {
    onPropertySet();
    this.customer = customer;
    if (!(isOld) && customer != null && !(customer.getTransactions().contains(this))) {
      customer.addToTransactions(this, -1);
    }
  }

  public double getAmount() {
    return this.amount;
  }

  public void setAmount(double amount) {
    onPropertySet();
    this.amount = amount;
  }

  public Transaction getOld() {
    return this.old;
  }

  public void setOld(DatabaseObject old) {
    this.old = ((Transaction) old);
  }

  public String displayName() {
    return "Transaction";
  }

  @Override
  public boolean equals(Object a) {
    return a instanceof Transaction && super.equals(a);
  }

  public Transaction deepClone(boolean clearId) {
    CloneContext ctx = new CloneContext(clearId);
    return ctx.startClone(this);
  }

  public void deepCloneIntoObj(ICloneable dbObj, CloneContext ctx) {
    super.deepCloneIntoObj(dbObj, ctx);
    Transaction _obj = ((Transaction) dbObj);
    _obj.setCustomer(customer);
    _obj.setAmount(amount);
  }

  public Transaction cloneInstance(Transaction cloneObj) {
    if (cloneObj == null) {
      cloneObj = new Transaction();
    }
    super.cloneInstance(cloneObj);
    cloneObj.setCustomer(this.getCustomer());
    cloneObj.setAmount(this.getAmount());
    return cloneObj;
  }

  public Transaction createNewInstance() {
    return new Transaction();
  }

  public boolean needOldObject() {
    return true;
  }

  public void collectCreatableReferences(List<Object> _refs) {
    super.collectCreatableReferences(_refs);
    _refs.add(this.customer);
  }

  @Override
  public boolean _isEntity() {
    return true;
  }
}
