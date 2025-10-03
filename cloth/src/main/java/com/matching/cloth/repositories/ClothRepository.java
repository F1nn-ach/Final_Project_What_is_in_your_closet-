package com.matching.cloth.repositories;

import com.matching.cloth.models.Clothing;
import com.matching.cloth.models.MainColorCategory;
import com.matching.cloth.models.SubColorCategory;
import com.matching.cloth.models.User;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.Optional;

@Repository
public interface ClothRepository extends JpaRepository<Clothing, Integer> {
    List<Clothing> findByUser(User user);
    Optional<Clothing> findByUserAndClothImage(User user, String clothImage);
    List<Clothing> findBySubColorCategory(SubColorCategory subColorCategory);
    List<Clothing> findBySubColorCategoryMainColorCategory(MainColorCategory mainColorCategory);
}
