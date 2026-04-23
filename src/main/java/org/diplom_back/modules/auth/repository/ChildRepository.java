package org.diplom_back.modules.auth.repository;

import org.diplom_back.modules.auth.entity.Child;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface ChildRepository extends JpaRepository<Child, Long> {
    List<Child> findByUserUserId(String userId);
}