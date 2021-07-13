package repository.jpa;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import d3e.core.DFile;

@Repository
public interface DFileRepository extends JpaRepository<DFile, String> {
}
