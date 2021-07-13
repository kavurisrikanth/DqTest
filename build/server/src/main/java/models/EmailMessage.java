package models;

import d3e.core.CloneContext;
import d3e.core.DFile;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;
import org.apache.solr.client.solrj.beans.Field;
import store.DatabaseObject;
import store.ICloneable;

public class EmailMessage extends D3EMessage {
  @Field private List<String> bcc = new ArrayList<>();
  @Field private List<String> cc = new ArrayList<>();
  @Field private String subject;
  @Field private boolean html = false;
  @Field private List<DFile> inlineAttachments = new ArrayList<>();
  @Field private List<DFile> attachments = new ArrayList<>();
  private transient EmailMessage old;

  public void addToBcc(String val, long index) {
    onPropertySet();
    if (index == -1) {
      this.bcc.add(val);
    } else {
      this.bcc.add(((int) index), val);
    }
  }

  public void removeFromBcc(String val) {
    onPropertySet();
    this.bcc.remove(val);
  }

  public void addToCc(String val, long index) {
    onPropertySet();
    if (index == -1) {
      this.cc.add(val);
    } else {
      this.cc.add(((int) index), val);
    }
  }

  public void removeFromCc(String val) {
    onPropertySet();
    this.cc.remove(val);
  }

  public void addToInlineAttachments(DFile val, long index) {
    onPropertySet();
    if (index == -1) {
      this.inlineAttachments.add(val);
    } else {
      this.inlineAttachments.add(((int) index), val);
    }
  }

  public void removeFromInlineAttachments(DFile val) {
    onPropertySet();
    this.inlineAttachments.remove(val);
  }

  public void addToAttachments(DFile val, long index) {
    onPropertySet();
    if (index == -1) {
      this.attachments.add(val);
    } else {
      this.attachments.add(((int) index), val);
    }
  }

  public void removeFromAttachments(DFile val) {
    onPropertySet();
    this.attachments.remove(val);
  }

  public void updateMasters(Consumer<DatabaseObject> visitor) {
    super.updateMasters(visitor);
  }

  public List<String> getBcc() {
    return this.bcc;
  }

  public void setBcc(List<String> bcc) {
    onPropertySet();
    this.bcc.clear();
    this.bcc.addAll(bcc);
  }

  public List<String> getCc() {
    return this.cc;
  }

  public void setCc(List<String> cc) {
    onPropertySet();
    this.cc.clear();
    this.cc.addAll(cc);
  }

  public String getSubject() {
    return this.subject;
  }

  public void setSubject(String subject) {
    onPropertySet();
    this.subject = subject;
  }

  public boolean isHtml() {
    return this.html;
  }

  public void setHtml(boolean html) {
    onPropertySet();
    this.html = html;
  }

  public List<DFile> getInlineAttachments() {
    return this.inlineAttachments;
  }

  public void setInlineAttachments(List<DFile> inlineAttachments) {
    onPropertySet();
    this.inlineAttachments.clear();
    this.inlineAttachments.addAll(inlineAttachments);
  }

  public List<DFile> getAttachments() {
    return this.attachments;
  }

  public void setAttachments(List<DFile> attachments) {
    onPropertySet();
    this.attachments.clear();
    this.attachments.addAll(attachments);
  }

  public EmailMessage getOld() {
    return this.old;
  }

  public void setOld(DatabaseObject old) {
    this.old = ((EmailMessage) old);
  }

  @Override
  public boolean equals(Object a) {
    return a instanceof EmailMessage && super.equals(a);
  }

  public EmailMessage deepClone(boolean clearId) {
    CloneContext ctx = new CloneContext(clearId);
    return ctx.startClone(this);
  }

  public void deepCloneIntoObj(ICloneable dbObj, CloneContext ctx) {
    super.deepCloneIntoObj(dbObj, ctx);
    EmailMessage _obj = ((EmailMessage) dbObj);
    _obj.setBcc(bcc);
    _obj.setCc(cc);
    _obj.setSubject(subject);
    _obj.setHtml(html);
    _obj.setInlineAttachments(inlineAttachments);
    _obj.setAttachments(attachments);
  }

  public EmailMessage cloneInstance(EmailMessage cloneObj) {
    if (cloneObj == null) {
      cloneObj = new EmailMessage();
    }
    super.cloneInstance(cloneObj);
    cloneObj.setBcc(new ArrayList<>(this.getBcc()));
    cloneObj.setCc(new ArrayList<>(this.getCc()));
    cloneObj.setSubject(this.getSubject());
    cloneObj.setHtml(this.isHtml());
    cloneObj.setInlineAttachments(new ArrayList<>(this.getInlineAttachments()));
    cloneObj.setAttachments(new ArrayList<>(this.getAttachments()));
    return cloneObj;
  }

  public boolean transientModel() {
    return true;
  }

  public EmailMessage createNewInstance() {
    return new EmailMessage();
  }

  public void collectCreatableReferences(List<Object> _refs) {
    super.collectCreatableReferences(_refs);
    _refs.addAll(this.inlineAttachments);
    _refs.addAll(this.attachments);
  }
}
