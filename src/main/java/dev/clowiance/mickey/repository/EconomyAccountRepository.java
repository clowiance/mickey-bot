package dev.clowiance.mickey.repository;

import dev.clowiance.mickey.entity.EconomyAccount;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface EconomyAccountRepository extends JpaRepository<EconomyAccount, Long> {

    List<EconomyAccount> findAllByOrderByBalanceDesc();
}
