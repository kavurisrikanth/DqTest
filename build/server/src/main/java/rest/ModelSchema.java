package rest;

import d3e.core.DFile;
import gqltosql.schema.DModel;
import gqltosql.schema.DModelType;
import gqltosql.schema.IModelSchema;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import javax.annotation.PostConstruct;
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

@org.springframework.stereotype.Service
public class ModelSchema implements IModelSchema {
  private Map<String, DModel<?>> allTypes = new HashMap<>();

  @PostConstruct
  public void init() {
    createAllTables();
    addFields();
  }

  public List<DModel<?>> getAllTypes() {
    return new ArrayList<>(allTypes.values());
  }

  public DModel<?> getType(String type) {
    return allTypes.get(type);
  }

  public <T> DModel<T> getType2(String type) {
    return ((DModel<T>) allTypes.get(type));
  }

  private void addTable(DModel<?> model) {
    allTypes.put(model.getType(), model);
  }

  private void createAllTables() {
    addTable(new DModel<DFile>("DFile", "_dfile", DModelType.ENTITY));
    addTable(new DModel<AnonymousUser>("AnonymousUser", "_anonymous_user", DModelType.ENTITY));
    addTable(new DModel<Avatar>("Avatar", "_avatar", DModelType.ENTITY));
    addTable(new DModel<Customer>("Customer", "_customer", DModelType.ENTITY));
    addTable(new DModel<D3EImage>("D3EImage", "_d3eimage", DModelType.EMBEDDED));
    addTable(new DModel<D3EMessage>("D3EMessage", "_d3emessage", DModelType.TRANSIENT));
    addTable(new DModel<EmailMessage>("EmailMessage", "_email_message", DModelType.TRANSIENT));
    addTable(
        new DModel<OneTimePassword>("OneTimePassword", "_one_time_password", DModelType.ENTITY));
    addTable(
        new DModel<PushNotification>(
            "PushNotification", "_push_notification", DModelType.TRANSIENT));
    addTable(new DModel<ReportConfig>("ReportConfig", "_report_config", DModelType.ENTITY));
    addTable(
        new DModel<ReportConfigOption>(
            "ReportConfigOption", "_report_config_option", DModelType.ENTITY));
    addTable(new DModel<SMSMessage>("SMSMessage", "_smsmessage", DModelType.TRANSIENT));
    addTable(new DModel<Transaction>("Transaction", "_transaction", DModelType.ENTITY));
    addTable(new DModel<User>("User", "_user", DModelType.ENTITY));
    addTable(new DModel<UserSession>("UserSession", "_user_session", DModelType.ENTITY));
    addDFileFields();
  }

  private void addDFileFields() {
    DModel<DFile> m = getType2("DFile");
    m.addPrimitive("id", "_id", (s) -> s.getId(), (s, v) -> s.setId(v));
    m.addPrimitive("name", "_name", (s) -> s.getName(), (s, v) -> s.setName(v));
    m.addPrimitive("size", "_size", (s) -> s.getSize(), (s, v) -> s.setSize(v));
  }

  public void addFields() {
    new ModelSchema1(allTypes).createAllTables();
  }
}
