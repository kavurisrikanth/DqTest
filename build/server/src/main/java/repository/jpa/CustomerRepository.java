package repository.jpa;

import java.util.List;
import models.Customer;
import models.Transaction;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

@Repository
public interface CustomerRepository extends JpaRepository<Customer, Long> {
  @Query("From models.Customer customer where :transactions member customer.transactions")
  public List<Customer> findByTransactions(@Param("transactions") Transaction transactions);
}
