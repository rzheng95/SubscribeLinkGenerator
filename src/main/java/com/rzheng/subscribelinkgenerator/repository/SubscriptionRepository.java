package com.rzheng.subscribelinkgenerator.repository;

import com.rzheng.subscribelinkgenerator.entity.Subscription;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Date;
import java.util.Optional;

/**
 * @author Richard
 */
public interface SubscriptionRepository extends JpaRepository<Subscription, String> {

    Optional<Subscription> findBySubKey(String subKey);

    @Query("SELECT CASE WHEN COUNT(l) > 0 THEN true ELSE false END FROM Subscription l WHERE l.subKey = :subKey AND l.expiration > :now")
    boolean isSubscriptionExpired(@Param("subKey") String subKey, @Param("now") Date now);

    default Subscription insertSubscription(Subscription subscription) {
        return save(subscription);
    }
}