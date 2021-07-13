package models;

import d3e.core.CloneContext;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;
import java.util.stream.Collectors;
import javax.persistence.CascadeType;
import javax.persistence.Entity;
import javax.persistence.OneToMany;
import javax.persistence.OrderColumn;
import javax.validation.constraints.NotNull;
import org.apache.solr.client.solrj.beans.Field;
import org.springframework.data.solr.core.mapping.ChildDocument;
import store.Database;
import store.DatabaseObject;
import store.ICloneable;

@Entity
public class ReportConfig extends DatabaseObject {
  @Field @NotNull private String identity;

  @Field
  @ChildDocument
  @OrderColumn
  @OneToMany(cascade = CascadeType.ALL, orphanRemoval = true)
  private List<ReportConfigOption> values = new ArrayList<>();

  private transient ReportConfig old;

  public void addToValues(ReportConfigOption val, long index) {
    onPropertySet();
    val.setMasterReportConfig(this);
    if (index == -1) {
      this.values.add(val);
    } else {
      this.values.add(((int) index), val);
    }
  }

  public void removeFromValues(ReportConfigOption val) {
    onPropertySet();
    this.values.remove(val);
  }

  public void updateMasters(Consumer<DatabaseObject> visitor) {
    super.updateMasters(visitor);
    for (ReportConfigOption obj : this.getValues()) {
      visitor.accept(obj);
      obj.setMasterReportConfig(this);
      obj.updateMasters(visitor);
    }
  }

  public String getIdentity() {
    return this.identity;
  }

  public void setIdentity(String identity) {
    onPropertySet();
    this.identity = identity;
  }

  public List<ReportConfigOption> getValues() {
    return this.values;
  }

  public void setValues(List<ReportConfigOption> values) {
    onPropertySet();
    this.values.clear();
    this.values.addAll(values);
  }

  public ReportConfig getOld() {
    return this.old;
  }

  public void setOld(DatabaseObject old) {
    this.old = ((ReportConfig) old);
  }

  public void recordOld(CloneContext ctx) {
    this.setOld(ctx.getFromCache(this));
    this.getValues().forEach((one) -> one.recordOld(ctx));
  }

  public String displayName() {
    return "ReportConfig";
  }

  @Override
  public boolean equals(Object a) {
    return a instanceof ReportConfig && super.equals(a);
  }

  public ReportConfig deepClone(boolean clearId) {
    CloneContext ctx = new CloneContext(clearId);
    return ctx.startClone(this);
  }

  public void collectChildValues(CloneContext ctx) {
    super.collectChildValues(ctx);
    ctx.collectChilds(values);
  }

  public void deepCloneIntoObj(ICloneable dbObj, CloneContext ctx) {
    super.deepCloneIntoObj(dbObj, ctx);
    ReportConfig _obj = ((ReportConfig) dbObj);
    _obj.setIdentity(identity);
    ctx.cloneChildList(values, (v) -> _obj.setValues(v));
  }

  public ReportConfig cloneInstance(ReportConfig cloneObj) {
    if (cloneObj == null) {
      cloneObj = new ReportConfig();
    }
    super.cloneInstance(cloneObj);
    cloneObj.setIdentity(this.getIdentity());
    cloneObj.setValues(
        this.getValues().stream()
            .map((ReportConfigOption colObj) -> colObj.cloneInstance(null))
            .collect(Collectors.toList()));
    return cloneObj;
  }

  public ReportConfig createNewInstance() {
    return new ReportConfig();
  }

  public boolean needOldObject() {
    return true;
  }

  public void collectCreatableReferences(List<Object> _refs) {
    super.collectCreatableReferences(_refs);
    Database.collectCollctionCreatableReferences(_refs, this.values);
  }

  @Override
  public boolean _isEntity() {
    return true;
  }
}
