package dev.clowiance.mickey.repository;

import dev.clowiance.mickey.entity.WarnEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface WarnRepository extends JpaRepository<WarnEntity, Long> {
    List<WarnEntity> findByUserIdAndActiveTrue(Long userId);
    List<WarnEntity> findByActiveTrue();
}
