package d3e.core;

import javax.annotation.PostConstruct;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class Services {
  private static Services instance;
  @Autowired private EmailService emailService;

  @PostConstruct
  public void init() {
    instance = this;
  }

  public static Services get() {
    return instance;
  }

  public EmailService getEmailService() {
    return this.emailService;
  }
}
