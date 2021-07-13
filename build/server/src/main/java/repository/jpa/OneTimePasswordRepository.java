package repository.jpa;

import java.util.List;
import models.OneTimePassword;
import models.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

@Repository
public interface OneTimePasswordRepository extends JpaRepository<OneTimePassword, Long> {
  @Query(
      "SELECT CASE WHEN COUNT(x) > 0 THEN false ELSE true END from models.OneTimePassword x where x.token = :token and x.id != :id")
  public boolean checkTokenUnique(@Param("id") Long id, @Param("token") String token);

  public models.OneTimePassword getByToken(String token);

  public List<OneTimePassword> getByUser(User user);
}
