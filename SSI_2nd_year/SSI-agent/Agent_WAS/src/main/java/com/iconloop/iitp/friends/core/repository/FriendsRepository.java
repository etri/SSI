package com.iconloop.iitp.friends.core.repository;

import com.iconloop.iitp.friends.core.model.Friend;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

public interface FriendsRepository extends JpaRepository<Friend, Long> {
    Friend findFriendByDid(String did);

    List<Friend> findFriendsByDidIsNot(String did);

    @Transactional
    long deleteFriendByDid(String did);
}
