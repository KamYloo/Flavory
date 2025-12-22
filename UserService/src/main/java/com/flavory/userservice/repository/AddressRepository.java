package com.flavory.userservice.repository;

import com.flavory.userservice.entity.Address;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface AddressRepository extends JpaRepository<Address, Long> {

    List<Address> findByUserId(Long userId);

    Optional<Address> findByIdAndUserId(Long id, Long userId);

    @Query("SELECT a FROM Address a JOIN FETCH a.user WHERE a.user.auth0Id = :auth0Id AND a.isDefault = true")
    Optional<Address> findDefaultAddressByAuth0Id(@Param("auth0Id") String auth0Id);

    @Modifying
    @Query("UPDATE Address a SET a.isDefault = false WHERE a.user.id = :userId")
    void clearDefaultAddress(@Param("userId") Long userId);
}