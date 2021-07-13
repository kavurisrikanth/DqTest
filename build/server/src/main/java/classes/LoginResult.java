package classes;

import models.User;

public class LoginResult {
  public boolean success;
  public User userObject;
  public String token;
  public String failureMessage;

  public LoginResult() {}

  public LoginResult(boolean success, User userObject, String token, String failureMessage) {
    this.success = success;
    this.userObject = userObject;
    this.token = token;
    this.failureMessage = failureMessage;
  }
}
