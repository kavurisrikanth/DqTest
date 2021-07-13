package models;

import d3e.core.CloneContext;
import d3e.core.DFile;
import java.util.function.Consumer;
import javax.persistence.Embeddable;
import javax.persistence.FetchType;
import javax.persistence.ManyToOne;
import org.apache.solr.client.solrj.beans.Field;
import org.hibernate.annotations.ColumnDefault;
import store.DatabaseObject;
import store.ICloneable;

@Embeddable
public class D3EImage implements ICloneable {
  @Field
  @ColumnDefault("0")
  private long size = 0l;

  @Field
  @ColumnDefault("0")
  private long width = 0l;

  @Field
  @ColumnDefault("0")
  private long height = 0l;

  @Field
  @ManyToOne(fetch = FetchType.LAZY)
  private DFile file;

  private transient Avatar masterAvatar;

  public DatabaseObject _masterObject() {
    if (masterAvatar != null) {
      return masterAvatar;
    }
    return null;
  }

  public void updateMasters(Consumer<DatabaseObject> visitor) {}

  public long getSize() {
    return this.size;
  }

  public void setSize(long size) {
    this.size = size;
  }

  public long getWidth() {
    return this.width;
  }

  public void setWidth(long width) {
    this.width = width;
  }

  public long getHeight() {
    return this.height;
  }

  public void setHeight(long height) {
    this.height = height;
  }

  public DFile getFile() {
    return this.file;
  }

  public void setFile(DFile file) {
    this.file = file;
  }

  public Avatar getMasterAvatar() {
    return this.masterAvatar;
  }

  public void setMasterAvatar(Avatar masterAvatar) {
    this.masterAvatar = masterAvatar;
  }

  public String displayName() {
    return "D3EImage";
  }

  @Override
  public boolean equals(Object a) {
    return a instanceof D3EImage && super.equals(a);
  }

  public D3EImage deepClone(boolean clearId) {
    CloneContext ctx = new CloneContext(clearId);
    return ctx.startClone(this);
  }

  public void deepCloneIntoObj(ICloneable dbObj, CloneContext ctx) {
    D3EImage _obj = ((D3EImage) dbObj);
    _obj.setSize(size);
    _obj.setWidth(width);
    _obj.setHeight(height);
    _obj.setFile(file);
  }

  public D3EImage cloneInstance(D3EImage cloneObj) {
    if (cloneObj == null) {
      cloneObj = new D3EImage();
    }
    cloneObj.setSize(this.getSize());
    cloneObj.setWidth(this.getWidth());
    cloneObj.setHeight(this.getHeight());
    cloneObj.setFile(this.getFile());
    return cloneObj;
  }

  public void clear() {
    this.size = 0l;
    this.width = 0l;
    this.height = 0l;
    this.file = null;
  }

  public boolean emptyEmbeddedModel() {
    if (this.size != 0l) {
      return false;
    }
    if (this.width != 0l) {
      return false;
    }
    if (this.height != 0l) {
      return false;
    }
    if (this.file != null) {
      return false;
    }
    return true;
  }

  public D3EImage createNewInstance() {
    return new D3EImage();
  }
}
