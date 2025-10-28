package com.matching.cloth.repositories;

import com.matching.cloth.models.Clothing;
import com.matching.cloth.models.User;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.Optional;

@Repository
public interface ClothRepository extends JpaRepository<Clothing, Integer> {
    List<Clothing> findByUser(User user);
    Optional<Clothing> findByUserAndClothingImage(User user, String clothingImage);
}
