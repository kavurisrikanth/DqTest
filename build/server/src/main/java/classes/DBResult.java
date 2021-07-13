package classes;

import java.util.List;

public class DBResult {
  private long status;
  private List<String> errors;
  private long id;
  private long localId;
  
  public long getStatus() {
    return status;
  }
  
  public void setStatus(long status) {
    this.status = status;
  }
  
  public List<String> getErrors() {
    return errors;
  }
  
  public void setErrors(List<String> errors) {
    this.errors = errors;
  }
  
  public long getId() {
    return id;
  }
  
  public void setId(long id) {
    this.id = id;
  }
  
  public long getLocalId() {
    return localId;
  }
  
  public void setLocalId(long localId) {
    this.localId = localId;
  }
}
