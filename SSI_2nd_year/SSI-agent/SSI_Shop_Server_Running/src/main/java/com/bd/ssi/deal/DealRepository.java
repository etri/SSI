package com.bd.ssi.deal;

import com.bd.ssi.auth.User;
import com.bd.ssi.common.BuyListVO;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;

import java.util.List;

public interface DealRepository extends JpaRepository<Deal, Integer>, JpaSpecificationExecutor<Deal> {
    List<Deal> findByBuyer(User buyer);

    List<Deal>  findByDid(String did);

//    @Query("Select a from Deal as a inner join Product as b on a.didSeller = b.did where b.did = ?1")
    List<Deal>  findByDidSeller(String did);

    List<Deal> findByDidAndBuyNSell(String did, String buyNSell);
}
