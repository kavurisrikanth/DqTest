package models;

import d3e.core.CloneContext;
import java.util.function.Consumer;
import javax.persistence.Entity;
import javax.validation.constraints.NotNull;
import org.apache.solr.client.solrj.beans.Field;
import org.hibernate.annotations.Type;
import org.springframework.data.solr.core.mapping.SolrDocument;
import store.DatabaseObject;
import store.ICloneable;

@SolrDocument(collection = "UserSession")
@Entity
public abstract class UserSession extends CreatableObject {
  @Field
  @NotNull
  @Type(type = "text")
  private String userSessionId;

  public void updateMasters(Consumer<DatabaseObject> visitor) {
    super.updateMasters(visitor);
  }

  public String getUserSessionId() {
    return this.userSessionId;
  }

  public void setUserSessionId(String userSessionId) {
    onPropertySet();
    this.userSessionId = userSessionId;
  }

  public String displayName() {
    return "UserSession";
  }

  @Override
  public boolean equals(Object a) {
    return a instanceof UserSession && super.equals(a);
  }

  public UserSession deepClone(boolean clearId) {
    CloneContext ctx = new CloneContext(clearId);
    return ctx.startClone(this);
  }

  public void deepCloneIntoObj(ICloneable dbObj, CloneContext ctx) {
    super.deepCloneIntoObj(dbObj, ctx);
    UserSession _obj = ((UserSession) dbObj);
    _obj.setUserSessionId(userSessionId);
  }

  public UserSession cloneInstance(UserSession cloneObj) {
    super.cloneInstance(cloneObj);
    cloneObj.setUserSessionId(this.getUserSessionId());
    return cloneObj;
  }

  @Override
  public boolean _isEntity() {
    return true;
  }
}
