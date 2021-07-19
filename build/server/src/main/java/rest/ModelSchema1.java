package rest;

import gqltosql.schema.DModel;
import java.util.HashMap;
import java.util.Map;
import models.AnonymousUser;
import models.Avatar;
import models.Customer;
import models.D3EImage;
import models.D3EMessage;
import models.EmailMessage;
import models.OneTimePassword;
import models.PushNotification;
import models.ReportConfig;
import models.ReportConfigOption;
import models.SMSMessage;
import models.Transaction;
import models.User;
import models.UserSession;

public class ModelSchema1 {
  private Map<String, DModel<?>> allTypes = new HashMap<>();

  public ModelSchema1(Map<String, DModel<?>> allTypes) {
    this.allTypes = allTypes;
  }

  public void createAllTables() {
    addAnonymousUserFields();
    addAvatarFields();
    addCustomerFields();
    addD3EImageFields();
    addD3EMessageFields();
    addEmailMessageFields();
    addOneTimePasswordFields();
    addPushNotificationFields();
    addReportConfigFields();
    addReportConfigOptionFields();
    addSMSMessageFields();
    addTransactionFields();
    addUserFields();
    addUserSessionFields();
  }

  public DModel<?> getType(String type) {
    return allTypes.get(type);
  }

  public <T> DModel<T> getType2(String type) {
    return ((DModel<T>) allTypes.get(type));
  }

  private void addAnonymousUserFields() {
    DModel<AnonymousUser> m = getType2("AnonymousUser");
    m.setParent(getType("User"));
    m.addPrimitive("id", "_id", (s) -> s.getId(), (s, v) -> s.setId(v));
  }

  private void addAvatarFields() {
    DModel<Avatar> m = getType2("Avatar");
    m.addPrimitive("id", "_id", (s) -> s.getId(), (s, v) -> s.setId(v));
    m.addReference(
        "image", "_image_id", getType("D3EImage"), (s) -> s.getImage(), (s, v) -> s.setImage(v));
    m.addPrimitive(
        "createFrom", "_create_from", (s) -> s.getCreateFrom(), (s, v) -> s.setCreateFrom(v));
  }

  private void addCustomerFields() {
    DModel<Customer> m = getType2("Customer");
    m.addPrimitive("id", "_id", (s) -> s.getId(), (s, v) -> s.setId(v));
    m.addPrimitive("name", "_name", (s) -> s.getName(), (s, v) -> s.setName(v));
    m.addPrimitive("gender", "_gender", (s) -> s.getGender(), (s, v) -> s.setGender(v));
    m.addPrimitive("age", "_age", (s) -> s.getAge(), (s, v) -> s.setAge(v));
    m.addInverseCollection(
        "transactions", "_customer_id", getType("Transaction"), (s) -> s.getTransactions());
  }

  private void addD3EImageFields() {
    DModel<D3EImage> m = getType2("D3EImage");
    m.addPrimitive("size", "_size", (s) -> s.getSize(), (s, v) -> s.setSize(v));
    m.addPrimitive("width", "_width", (s) -> s.getWidth(), (s, v) -> s.setWidth(v));
    m.addPrimitive("height", "_height", (s) -> s.getHeight(), (s, v) -> s.setHeight(v));
    m.addReference(
        "file", "_file_id", getType("DFile"), (s) -> s.getFile(), (s, v) -> s.setFile(v));
  }

  private void addD3EMessageFields() {
    DModel<D3EMessage> m = getType2("D3EMessage");
    m.addPrimitive("id", "_id", (s) -> s.getId(), (s, v) -> s.setId(v));
    m.addPrimitive("from", "_from", (s) -> s.getFrom(), (s, v) -> s.setFrom(v));
    m.addPrimitiveCollection("to", "_to", "_d3emessage_to", (s) -> s.getTo(), (s, v) -> s.setTo(v));
    m.addPrimitive("body", "_body", (s) -> s.getBody(), (s, v) -> s.setBody(v));
    m.addPrimitive(
        "createdOn", "_created_on", (s) -> s.getCreatedOn(), (s, v) -> s.setCreatedOn(v));
  }

  private void addEmailMessageFields() {
    DModel<EmailMessage> m = getType2("EmailMessage");
    m.setParent(getType("D3EMessage"));
    m.addPrimitive("id", "_id", (s) -> s.getId(), (s, v) -> s.setId(v));
    m.addPrimitiveCollection(
        "bcc", "_bcc", "_email_message_bcc", (s) -> s.getBcc(), (s, v) -> s.setBcc(v));
    m.addPrimitiveCollection(
        "cc", "_cc", "_email_message_cc", (s) -> s.getCc(), (s, v) -> s.setCc(v));
    m.addPrimitive("subject", "_subject", (s) -> s.getSubject(), (s, v) -> s.setSubject(v));
    m.addPrimitive("html", "_html", (s) -> s.isHtml(), (s, v) -> s.setHtml(v));
    m.addReferenceCollection(
        "inlineAttachments",
        "_inline_attachments_id",
        "_email_message_inline_attachments",
        getType("DFile"),
        (s) -> s.getInlineAttachments(),
        (s, v) -> s.setInlineAttachments(v));
    m.addReferenceCollection(
        "attachments",
        "_attachments_id",
        "_email_message_attachments",
        getType("DFile"),
        (s) -> s.getAttachments(),
        (s, v) -> s.setAttachments(v));
  }

  private void addOneTimePasswordFields() {
    DModel<OneTimePassword> m = getType2("OneTimePassword");
    m.addPrimitive("id", "_id", (s) -> s.getId(), (s, v) -> s.setId(v));
    m.addPrimitive("input", "_input", (s) -> s.getInput(), (s, v) -> s.setInput(v));
    m.addPrimitive(
        "inputType", "_input_type", (s) -> s.getInputType(), (s, v) -> s.setInputType(v));
    m.addPrimitive("userType", "_user_type", (s) -> s.getUserType(), (s, v) -> s.setUserType(v));
    m.addPrimitive("success", "_success", (s) -> s.isSuccess(), (s, v) -> s.setSuccess(v));
    m.addPrimitive("errorMsg", "_error_msg", (s) -> s.getErrorMsg(), (s, v) -> s.setErrorMsg(v));
    m.addPrimitive("token", "_token", (s) -> s.getToken(), (s, v) -> s.setToken(v));
    m.addPrimitive("code", "_code", (s) -> s.getCode(), (s, v) -> s.setCode(v));
    m.addReference("user", "_user_id", getType("User"), (s) -> s.getUser(), (s, v) -> s.setUser(v));
    m.addPrimitive("expiry", "_expiry", (s) -> s.getExpiry(), (s, v) -> s.setExpiry(v));
  }

  private void addPushNotificationFields() {
    DModel<PushNotification> m = getType2("PushNotification");
    m.addPrimitive("id", "_id", (s) -> s.getId(), (s, v) -> s.setId(v));
    m.addPrimitiveCollection(
        "deviceTokens",
        "_device_tokens",
        "_push_notification_device_tokens",
        (s) -> s.getDeviceTokens(),
        (s, v) -> s.setDeviceTokens(v));
    m.addPrimitive("title", "_title", (s) -> s.getTitle(), (s, v) -> s.setTitle(v));
    m.addPrimitive("body", "_body", (s) -> s.getBody(), (s, v) -> s.setBody(v));
    m.addPrimitive("path", "_path", (s) -> s.getPath(), (s, v) -> s.setPath(v));
  }

  private void addReportConfigFields() {
    DModel<ReportConfig> m = getType2("ReportConfig");
    m.addPrimitive("id", "_id", (s) -> s.getId(), (s, v) -> s.setId(v));
    m.addPrimitive("identity", "_identity", (s) -> s.getIdentity(), (s, v) -> s.setIdentity(v));
    m.addReferenceCollection(
        "values",
        "_values_id",
        "_report_config_values",
        getType("ReportConfigOption"),
        (s) -> s.getValues(),
        (s, v) -> s.setValues(v));
  }

  private void addReportConfigOptionFields() {
    DModel<ReportConfigOption> m = getType2("ReportConfigOption");
    m.addPrimitive("id", "_id", (s) -> s.getId(), (s, v) -> s.setId(v));
    m.addPrimitive("identity", "_identity", (s) -> s.getIdentity(), (s, v) -> s.setIdentity(v));
    m.addPrimitive("value", "_value", (s) -> s.getValue(), (s, v) -> s.setValue(v));
  }

  private void addSMSMessageFields() {
    DModel<SMSMessage> m = getType2("SMSMessage");
    m.setParent(getType("D3EMessage"));
    m.addPrimitive("id", "_id", (s) -> s.getId(), (s, v) -> s.setId(v));
    m.addPrimitive(
        "dltTemplateId",
        "_dlt_template_id",
        (s) -> s.getDltTemplateId(),
        (s, v) -> s.setDltTemplateId(v));
  }

  private void addTransactionFields() {
    DModel<Transaction> m = getType2("Transaction");
    m.addPrimitive("id", "_id", (s) -> s.getId(), (s, v) -> s.setId(v));
    m.addReference(
        "customer",
        "_customer_id",
        getType("Customer"),
        (s) -> s.getCustomer(),
        (s, v) -> s.setCustomer(v));
    m.addPrimitive("amount", "_amount", (s) -> s.getAmount(), (s, v) -> s.setAmount(v));
  }

  private void addUserFields() {
    DModel<User> m = getType2("User");
    m.addPrimitive("id", "_id", (s) -> s.getId(), (s, v) -> s.setId(v));
    m.addPrimitive("isActive", "_is_active", (s) -> s.isIsActive(), (s, v) -> s.setIsActive(v));
    m.addPrimitive(
        "deviceToken", "_device_token", (s) -> s.getDeviceToken(), (s, v) -> s.setDeviceToken(v));
  }

  private void addUserSessionFields() {
    DModel<UserSession> m = getType2("UserSession");
    m.addPrimitive("id", "_id", (s) -> s.getId(), (s, v) -> s.setId(v));
    m.addPrimitive(
        "userSessionId",
        "_user_session_id",
        (s) -> s.getUserSessionId(),
        (s, v) -> s.setUserSessionId(v));
  }
}
