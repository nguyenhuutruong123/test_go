package com.yes4all.repository;

import com.yes4all.domain.User;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

/**
 * Spring Data JPA repository for the {@link User} entity.
 */
@Repository
public interface UserRepository extends JpaRepository<User, String> {
    Optional<User> findOneByLogin(String login);

    Optional<User> findOneByEmail(String email);
    Optional<User> findOneByVendor(String vendor);
    @EntityGraph(attributePaths = "authorities")
    Optional<User> findOneWithAuthoritiesByLogin(String login);

    @EntityGraph(attributePaths = "authorities")
    Optional<User> findOneWithAuthoritiesByEmail(String email);

    @Query(value = "SELECT us.* FROM jhi_user us left join jhi_user_authority auth " +
        " on auth.user_id=us.id where auth.authority_name =:role ", nativeQuery = true)
    List<User> findAllWithRole(@Param("role") String role);

    Page<User> findAllByIdNotNullAndActivatedIsTrue(Pageable pageable);

    List<User> findAllByIsYes4all(Boolean isYes4all);
    List<User> findAllBySourcing(Boolean sourcing);

}
