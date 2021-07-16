package lists;

import store.DBObject;

public class ListChange {
  long id;
  int type; //type of id struct
  int field; //list field, some objects have multiple lists
  Change change; // ADD/REVMOVE/CHANGE
  int changeFromIndex;
  int changeToIndex;
  DBObject obj;
}
