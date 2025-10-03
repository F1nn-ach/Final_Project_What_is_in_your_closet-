package com.matching.cloth.repositories;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.matching.cloth.models.Astrologer;

@Repository
public interface AstrologerRepository extends JpaRepository<Astrologer, Integer> {
    Astrologer findByAstrologerId(int astrologerId);

    Astrologer findByAstrologerName(String astrologerName);

    boolean existsByAstrologerName(String astrologerName);

    List<Astrologer> findAll();

}
