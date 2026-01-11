package com.example.serverpublishingapp.repository;

import com.example.serverpublishingapp.entity.Edition;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface EditionRepository extends JpaRepository<Edition, Long> {

    @Query("SELECT DISTINCT e FROM Edition e JOIN e.genres g WHERE g.name = :genreName")
    List<Edition> findByGenreName(@Param("genreName") String genreName);

    @Query("SELECT e FROM Edition e WHERE LOWER(e.authorFirstName) LIKE LOWER(CONCAT('%', :query, '%')) OR " +
            "LOWER(e.authorLastName) LIKE LOWER(CONCAT('%', :query, '%'))")
    List<Edition> findByAuthorName(@Param("query") String query);

    @Query("SELECT e FROM Edition e WHERE LOWER(e.title) LIKE LOWER(CONCAT('%', :query, '%'))")
    List<Edition> findByTitle(@Param("query") String query);

    @Query("SELECT DISTINCT g.name FROM Genre g ORDER BY g.name")
    List<String> findAllGenreNames();

    @Query("SELECT e FROM Edition e LEFT JOIN FETCH e.genres WHERE e.id = :id")
    Optional<Edition> findByIdWithGenres(@Param("id") Long id);
}