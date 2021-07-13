package repository.jpa;

import models.ReportConfig;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface ReportConfigRepository extends JpaRepository<ReportConfig, Long> {}
