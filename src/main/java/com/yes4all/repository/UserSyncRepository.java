package com.yes4all.repository;

import com.yes4all.domain.UserSync;
import com.yes4all.domain.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

/**
 * Spring Data JPA repository for the {@link User} entity.
 */
@Repository
public interface UserSyncRepository extends JpaRepository<UserSync, String> {

}
