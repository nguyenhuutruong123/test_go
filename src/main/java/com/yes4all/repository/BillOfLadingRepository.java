package com.yes4all.repository;

import com.yes4all.domain.BillOfLading;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Optional;

/**
 * Spring Data JPA repository for the PurchaseOrders entity.
 */
@SuppressWarnings("unused")
@Repository
public interface BillOfLadingRepository extends JpaRepository<BillOfLading, Integer> {
    @Query(value = "SELECT p.* FROM bill_of_lading p" +
        " left join jhi_user ju_login on ju_login.login=:userId " +
        " where ( (p.broker_id is not null and exists (select 1 from jhi_user_authority jua where jua.user_id=ju_login.login and jua.authority_name='POMS-BROKER') " +
        " and p.broker_id=:userId) " +
        " or ( ju_login.vendor is null and exists (select 1 from jhi_user_authority jua where jua.user_id=ju_login.login and jua.authority_name='POMS-USER') ) )" +
        " and ( upper(p.bill_of_lading_no) like '%'||:bolNo||'%' or :bolNo='') " +
        " and ( (p.id in (select bill_of_lading_id from booking a where upper(a.booking_confirmation) like '%'||:bookingNo||'%' )) or :bookingNo='') ",
        nativeQuery = true)
    Page<BillOfLading> findByCondition(@Param("bolNo") String bolNo
        , @Param("bookingNo") String bookingNo
        , @Param("userId") String userId
        , Pageable pageable);

    Optional<BillOfLading> findOneByBillOfLadingNo(String billOfLadingNo);
}
