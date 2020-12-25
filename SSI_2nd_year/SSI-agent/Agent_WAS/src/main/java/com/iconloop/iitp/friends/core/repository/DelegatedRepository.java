package com.iconloop.iitp.friends.core.repository;

import com.iconloop.iitp.friends.core.model.Delegated;
import org.springframework.data.jpa.repository.JpaRepository;

public interface DelegatedRepository extends JpaRepository<Delegated, Long> {
    Delegated findDelegatedByToken(String token);
}
