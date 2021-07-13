package models;

import d3e.core.CloneContext;
import java.util.function.Consumer;
import org.apache.solr.client.solrj.beans.Field;
import store.DatabaseObject;
import store.ICloneable;

public class SMSMessage extends D3EMessage {
  @Field private String dltTemplateId;
  private transient SMSMessage old;

  public void updateMasters(Consumer<DatabaseObject> visitor) {
    super.updateMasters(visitor);
  }

  public String getDltTemplateId() {
    return this.dltTemplateId;
  }

  public void setDltTemplateId(String dltTemplateId) {
    onPropertySet();
    this.dltTemplateId = dltTemplateId;
  }

  public SMSMessage getOld() {
    return this.old;
  }

  public void setOld(DatabaseObject old) {
    this.old = ((SMSMessage) old);
  }

  @Override
  public boolean equals(Object a) {
    return a instanceof SMSMessage && super.equals(a);
  }

  public SMSMessage deepClone(boolean clearId) {
    CloneContext ctx = new CloneContext(clearId);
    return ctx.startClone(this);
  }

  public void deepCloneIntoObj(ICloneable dbObj, CloneContext ctx) {
    super.deepCloneIntoObj(dbObj, ctx);
    SMSMessage _obj = ((SMSMessage) dbObj);
    _obj.setDltTemplateId(dltTemplateId);
  }

  public SMSMessage cloneInstance(SMSMessage cloneObj) {
    if (cloneObj == null) {
      cloneObj = new SMSMessage();
    }
    super.cloneInstance(cloneObj);
    cloneObj.setDltTemplateId(this.getDltTemplateId());
    return cloneObj;
  }

  public boolean transientModel() {
    return true;
  }

  public SMSMessage createNewInstance() {
    return new SMSMessage();
  }
}
