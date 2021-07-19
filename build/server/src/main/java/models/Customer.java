package models;

import classes.Gender;
import d3e.core.CloneContext;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;
import javax.persistence.Entity;
import javax.persistence.Enumerated;
import javax.persistence.ManyToMany;
import org.apache.solr.client.solrj.beans.Field;
import org.hibernate.annotations.ColumnDefault;
import org.springframework.data.solr.core.mapping.SolrDocument;
import store.DatabaseObject;
import store.ICloneable;

@SolrDocument(collection = "Customer")
@Entity
public class Customer extends CreatableObject {
  @Field private String name;

  @Field
  @Enumerated(javax.persistence.EnumType.STRING)
  private Gender gender = Gender.Male;

  @Field
  @ColumnDefault("0")
  private long age = 0l;

  @Field
  @ManyToMany(mappedBy = "customer")
  private List<Transaction> transactions = new ArrayList<>();

  private transient Customer old;

  public void addToTransactions(Transaction val, long index) {
    if (index == -1) {
      this.transactions.add(val);
    } else {
      this.transactions.add(((int) index), val);
    }
  }

  public void removeFromTransactions(Transaction val) {
    this.transactions.remove(val);
  }

  public void updateMasters(Consumer<DatabaseObject> visitor) {
    super.updateMasters(visitor);
  }

  public String getName() {
    return this.name;
  }

  public void setName(String name) {
    onPropertySet();
    this.name = name;
  }

  public Gender getGender() {
    return this.gender;
  }

  public void setGender(Gender gender) {
    onPropertySet();
    this.gender = gender;
  }

  public long getAge() {
    return this.age;
  }

  public void setAge(long age) {
    onPropertySet();
    this.age = age;
  }

  public List<Transaction> getTransactions() {
    return this.transactions;
  }

  public void setTransactions(List<Transaction> transactions) {
    this.transactions.clear();
    this.transactions.addAll(transactions);
  }

  public Customer getOld() {
    return this.old;
  }

  public void setOld(DatabaseObject old) {
    this.old = ((Customer) old);
  }

  public String displayName() {
    return "Customer";
  }

  @Override
  public boolean equals(Object a) {
    return a instanceof Customer && super.equals(a);
  }

  public Customer deepClone(boolean clearId) {
    CloneContext ctx = new CloneContext(clearId);
    return ctx.startClone(this);
  }

  public void deepCloneIntoObj(ICloneable dbObj, CloneContext ctx) {
    super.deepCloneIntoObj(dbObj, ctx);
    Customer _obj = ((Customer) dbObj);
    _obj.setName(name);
    _obj.setGender(gender);
    _obj.setAge(age);
    _obj.setTransactions(transactions);
  }

  public Customer cloneInstance(Customer cloneObj) {
    if (cloneObj == null) {
      cloneObj = new Customer();
    }
    super.cloneInstance(cloneObj);
    cloneObj.setName(this.getName());
    cloneObj.setGender(this.getGender());
    cloneObj.setAge(this.getAge());
    cloneObj.setTransactions(new ArrayList<>(this.getTransactions()));
    return cloneObj;
  }

  public Customer createNewInstance() {
    return new Customer();
  }

  public boolean needOldObject() {
    return true;
  }

  @Override
  public boolean _isEntity() {
    return true;
  }
}
