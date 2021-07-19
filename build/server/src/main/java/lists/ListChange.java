package lists;

import store.DBObject;

public class ListChange {
  long id;
  int type; //type of id struct
  int field; //list field, some objects have multiple lists
  Change change; // ADD/REVMOVE/CHANGE
  ListChangeType changeType;
  int changeFromIndex;
  int changeToIndex;
  DBObject obj;
  
  ListChange() {
    
  }
  
  ListChange(long id, int type, int field, ListChangeType changeType, DBObject obj) {
    
  }
  
  static ListChange forPathChange(long id, int type, int field, ListChangeType changeType, int oldIndex, int index) {
    ListChange ins = new ListChange();
    ins.id = id;
    ins.type = type;  // AllTransactions type
    ins.field = field; // items field
    ins.changeType = changeType;
    ins.changeFromIndex = oldIndex;
    ins.changeToIndex = index;
    return ins;
  }
}
