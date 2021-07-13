package d3e.core;

import org.hibernate.boot.model.naming.Identifier;
import org.hibernate.engine.jdbc.env.spi.JdbcEnvironment;
import org.springframework.boot.orm.jpa.hibernate.SpringPhysicalNamingStrategy;

public class D3ESpringPhysicalNamingStrategy extends SpringPhysicalNamingStrategy {

  @Override
  protected Identifier getIdentifier(String name, boolean quoted, JdbcEnvironment jdbcEnvironment) {
    Identifier identifier = super.getIdentifier(name, quoted, jdbcEnvironment);
    return new Identifier("_" + identifier.getText(), identifier.isQuoted());
  }
}
