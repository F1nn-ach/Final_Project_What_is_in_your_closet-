package com.matching.cloth.repositories;

import com.matching.cloth.models.LuckyColorType;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface LuckyColorTypeRepository extends JpaRepository<LuckyColorType, Integer> {
    List<LuckyColorType> findAll();

    List<LuckyColorType> findAllByOrderByLuckyColorTypeNameAsc();
}
