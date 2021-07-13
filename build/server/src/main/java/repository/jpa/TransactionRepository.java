package repository.jpa;

import java.util.List;
import models.Customer;
import models.Transaction;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface TransactionRepository extends JpaRepository<Transaction, Long> {
  public List<Transaction> getByCustomer(Customer customer);
}
