package com.matching.cloth.repositories;

import com.matching.cloth.models.LuckyListColor;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface LuckyListColorRepository extends JpaRepository<LuckyListColor, Integer> {
    List<LuckyListColor> findAll();
}
