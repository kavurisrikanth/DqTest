package models;

import d3e.core.CloneContext;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;
import org.apache.solr.client.solrj.beans.Field;
import store.DatabaseObject;
import store.ICloneable;

public class PushNotification extends CreatableObject {
  @Field private List<String> deviceTokens = new ArrayList<>();
  @Field private String title;
  @Field private String body;
  @Field private String path;
  private transient PushNotification old;

  public void addToDeviceTokens(String val, long index) {
    onPropertySet();
    if (index == -1) {
      this.deviceTokens.add(val);
    } else {
      this.deviceTokens.add(((int) index), val);
    }
  }

  public void removeFromDeviceTokens(String val) {
    onPropertySet();
    this.deviceTokens.remove(val);
  }

  public void updateMasters(Consumer<DatabaseObject> visitor) {
    super.updateMasters(visitor);
  }

  public List<String> getDeviceTokens() {
    return this.deviceTokens;
  }

  public void setDeviceTokens(List<String> deviceTokens) {
    onPropertySet();
    this.deviceTokens.clear();
    this.deviceTokens.addAll(deviceTokens);
  }

  public String getTitle() {
    return this.title;
  }

  public void setTitle(String title) {
    onPropertySet();
    this.title = title;
  }

  public String getBody() {
    return this.body;
  }

  public void setBody(String body) {
    onPropertySet();
    this.body = body;
  }

  public String getPath() {
    return this.path;
  }

  public void setPath(String path) {
    onPropertySet();
    this.path = path;
  }

  public PushNotification getOld() {
    return this.old;
  }

  public void setOld(DatabaseObject old) {
    this.old = ((PushNotification) old);
  }

  public String displayName() {
    return "PushNotification";
  }

  @Override
  public boolean equals(Object a) {
    return a instanceof PushNotification && super.equals(a);
  }

  public PushNotification deepClone(boolean clearId) {
    CloneContext ctx = new CloneContext(clearId);
    return ctx.startClone(this);
  }

  public void deepCloneIntoObj(ICloneable dbObj, CloneContext ctx) {
    super.deepCloneIntoObj(dbObj, ctx);
    PushNotification _obj = ((PushNotification) dbObj);
    _obj.setDeviceTokens(deviceTokens);
    _obj.setTitle(title);
    _obj.setBody(body);
    _obj.setPath(path);
  }

  public PushNotification cloneInstance(PushNotification cloneObj) {
    if (cloneObj == null) {
      cloneObj = new PushNotification();
    }
    super.cloneInstance(cloneObj);
    cloneObj.setDeviceTokens(new ArrayList<>(this.getDeviceTokens()));
    cloneObj.setTitle(this.getTitle());
    cloneObj.setBody(this.getBody());
    cloneObj.setPath(this.getPath());
    return cloneObj;
  }

  public boolean transientModel() {
    return true;
  }

  public PushNotification createNewInstance() {
    return new PushNotification();
  }
}
