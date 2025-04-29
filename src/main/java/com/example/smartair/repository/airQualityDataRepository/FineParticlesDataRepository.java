package com.example.smartair.repository.airQualityDataRepository;

import com.example.smartair.entity.airData.FineParticlesData;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface FineParticlesDataRepository extends JpaRepository<FineParticlesData, Integer> {
}
