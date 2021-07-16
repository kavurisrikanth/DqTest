package lists;

public interface ChangesConsumer {
  public void writeListChange(ListChange change);
  
  public void writeObjectChange(ObjectChange change);
}
