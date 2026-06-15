package com.vinncorp.erp.modules.crm.repository;

import com.vinncorp.erp.modules.crm.entity.Customer;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface CustomerRepository extends JpaRepository<Customer, Long> {
    Optional<Customer> findByIdAndWorkspaceId(Long id, Long workspaceId);
    List<Customer> findAllByWorkspaceId(Long workspaceId);
    boolean existsByNameAndWorkspaceId(String name, Long workspaceId);

    @Query("SELECT c FROM Customer c WHERE c.workspace.id = :wsId AND (LOWER(c.name) LIKE LOWER(CONCAT('%', :q, '%')) OR LOWER(c.industry) LIKE LOWER(CONCAT('%', :q, '%'))) ORDER BY c.createdAt DESC")
    List<Customer> search(@Param("wsId") Long workspaceId, @Param("q") String query);
}
