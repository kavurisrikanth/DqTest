package repository.jpa;

import models.ReportConfigOption;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface ReportConfigOptionRepository extends JpaRepository<ReportConfigOption, Long> {}
