package models;

import d3e.core.CloneContext;
import java.util.List;
import java.util.function.Consumer;
import javax.persistence.Embedded;
import javax.persistence.Entity;
import org.apache.solr.client.solrj.beans.Field;
import org.springframework.data.solr.core.mapping.ChildDocument;
import store.DatabaseObject;
import store.ICloneable;

@Entity
public class Avatar extends DatabaseObject {
  @Field @ChildDocument @Embedded private D3EImage image = new D3EImage();
  @Field private String createFrom;
  private transient Avatar old;

  public void updateMasters(Consumer<DatabaseObject> visitor) {
    super.updateMasters(visitor);
    if (image != null) {
      image.setMasterAvatar(this);
      image.updateMasters(visitor);
    }
  }

  public D3EImage getImage() {
    return this.image;
  }

  public void setImage(D3EImage image) {
    onPropertySet();
    if (image == null) {
      image = new D3EImage();
    }
    this.image = image;
  }

  public String getCreateFrom() {
    return this.createFrom;
  }

  public void setCreateFrom(String createFrom) {
    onPropertySet();
    this.createFrom = createFrom;
  }

  public Avatar getOld() {
    return this.old;
  }

  public void setOld(DatabaseObject old) {
    this.old = ((Avatar) old);
  }

  public String displayName() {
    return "Avatar";
  }

  @Override
  public boolean equals(Object a) {
    return a instanceof Avatar && super.equals(a);
  }

  public Avatar deepClone(boolean clearId) {
    CloneContext ctx = new CloneContext(clearId);
    return ctx.startClone(this);
  }

  public void collectChildValues(CloneContext ctx) {
    super.collectChildValues(ctx);
    ctx.collectChild(image);
  }

  public void deepCloneIntoObj(ICloneable dbObj, CloneContext ctx) {
    super.deepCloneIntoObj(dbObj, ctx);
    Avatar _obj = ((Avatar) dbObj);
    ctx.cloneChild(image, (v) -> _obj.setImage(v));
    _obj.setCreateFrom(createFrom);
  }

  public Avatar cloneInstance(Avatar cloneObj) {
    if (cloneObj == null) {
      cloneObj = new Avatar();
    }
    super.cloneInstance(cloneObj);
    cloneObj.setImage(this.getImage().cloneInstance(null));
    cloneObj.setCreateFrom(this.getCreateFrom());
    return cloneObj;
  }

  public Avatar createNewInstance() {
    return new Avatar();
  }

  public boolean needOldObject() {
    return true;
  }

  public void collectCreatableReferences(List<Object> _refs) {
    super.collectCreatableReferences(_refs);
    _refs.add(this.image.getFile());
  }

  @Override
  public boolean _isEntity() {
    return true;
  }
}
