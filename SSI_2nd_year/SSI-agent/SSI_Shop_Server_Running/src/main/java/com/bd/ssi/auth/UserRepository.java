package com.bd.ssi.auth;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

/**
 * User 테이블 Repository
 */
@Repository
public interface UserRepository extends JpaRepository<User, String> {

}
