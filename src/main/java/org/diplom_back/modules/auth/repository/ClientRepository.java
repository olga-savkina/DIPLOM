package org.diplom_back.modules.auth.repository;

import org.diplom_back.modules.auth.entity.Client;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.Optional;

public interface ClientRepository extends JpaRepository<Client, String> {
    Optional<Client> findByUserUserId(String userId);
    Optional<Client> findByUserEmail(String email);
}