package com.example.smartair.repository.airQualityRepository.airQualityDataRepository;

import com.example.smartair.entity.airData.fineParticlesData.FineParticlesDataPt2;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface FineParticlesDataPt2Repository extends JpaRepository<FineParticlesDataPt2, Long> {
}
