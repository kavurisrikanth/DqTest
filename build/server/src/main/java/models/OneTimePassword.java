package models;

import d3e.core.CloneContext;
import java.time.LocalDateTime;
import java.util.List;
import java.util.function.Consumer;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.ManyToOne;
import javax.validation.constraints.NotNull;
import org.apache.solr.client.solrj.beans.Field;
import org.hibernate.annotations.ColumnDefault;
import org.springframework.data.solr.core.mapping.SolrDocument;
import store.DatabaseObject;
import store.ICloneable;

@SolrDocument(collection = "OneTimePassword")
@Entity
public class OneTimePassword extends CreatableObject {
  @Field @NotNull private String input;
  @Field @NotNull private String inputType;
  @Field @NotNull private String userType;

  @Field
  @ColumnDefault("false")
  private boolean success = false;

  @Field private String errorMsg;
  @Field private String token;
  @Field private String code;

  @Field
  @ManyToOne(fetch = FetchType.LAZY)
  private User user;

  @Field private LocalDateTime expiry;
  private transient OneTimePassword old;

  public void updateMasters(Consumer<DatabaseObject> visitor) {
    super.updateMasters(visitor);
  }

  public String getInput() {
    return this.input;
  }

  public void setInput(String input) {
    onPropertySet();
    this.input = input;
  }

  public String getInputType() {
    return this.inputType;
  }

  public void setInputType(String inputType) {
    onPropertySet();
    this.inputType = inputType;
  }

  public String getUserType() {
    return this.userType;
  }

  public void setUserType(String userType) {
    onPropertySet();
    this.userType = userType;
  }

  public boolean isSuccess() {
    return this.success;
  }

  public void setSuccess(boolean success) {
    onPropertySet();
    this.success = success;
  }

  public String getErrorMsg() {
    return this.errorMsg;
  }

  public void setErrorMsg(String errorMsg) {
    onPropertySet();
    this.errorMsg = errorMsg;
  }

  public String getToken() {
    return this.token;
  }

  public void setToken(String token) {
    onPropertySet();
    this.token = token;
  }

  public String getCode() {
    return this.code;
  }

  public void setCode(String code) {
    onPropertySet();
    this.code = code;
  }

  public User getUser() {
    return this.user;
  }

  public void setUser(User user) {
    onPropertySet();
    this.user = user;
  }

  public LocalDateTime getExpiry() {
    return this.expiry;
  }

  public void setExpiry(LocalDateTime expiry) {
    onPropertySet();
    this.expiry = expiry;
  }

  public OneTimePassword getOld() {
    return this.old;
  }

  public void setOld(DatabaseObject old) {
    this.old = ((OneTimePassword) old);
  }

  public String displayName() {
    return "OneTimePassword";
  }

  @Override
  public boolean equals(Object a) {
    return a instanceof OneTimePassword && super.equals(a);
  }

  public OneTimePassword deepClone(boolean clearId) {
    CloneContext ctx = new CloneContext(clearId);
    return ctx.startClone(this);
  }

  public void deepCloneIntoObj(ICloneable dbObj, CloneContext ctx) {
    super.deepCloneIntoObj(dbObj, ctx);
    OneTimePassword _obj = ((OneTimePassword) dbObj);
    _obj.setInput(input);
    _obj.setInputType(inputType);
    _obj.setUserType(userType);
    _obj.setSuccess(success);
    _obj.setErrorMsg(errorMsg);
    _obj.setToken(token);
    _obj.setCode(code);
    _obj.setUser(user);
    _obj.setExpiry(expiry);
  }

  public OneTimePassword cloneInstance(OneTimePassword cloneObj) {
    if (cloneObj == null) {
      cloneObj = new OneTimePassword();
    }
    super.cloneInstance(cloneObj);
    cloneObj.setInput(this.getInput());
    cloneObj.setInputType(this.getInputType());
    cloneObj.setUserType(this.getUserType());
    cloneObj.setSuccess(this.isSuccess());
    cloneObj.setErrorMsg(this.getErrorMsg());
    cloneObj.setToken(this.getToken());
    cloneObj.setCode(this.getCode());
    cloneObj.setUser(this.getUser());
    cloneObj.setExpiry(this.getExpiry());
    return cloneObj;
  }

  public OneTimePassword createNewInstance() {
    return new OneTimePassword();
  }

  public void collectCreatableReferences(List<Object> _refs) {
    super.collectCreatableReferences(_refs);
    _refs.add(this.user);
  }

  @Override
  public boolean _isEntity() {
    return true;
  }
}
