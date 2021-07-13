package lists;

import java.util.List;

import org.json.JSONObject;

import classes.SubscriptionChangeType;

public class DataQueryChange<T> {
  public SubscriptionChangeType changeType;
  public String path;
  public String oldPath;
  public List<T> nativeData;
  public JSONObject data;
  public int index;
}
