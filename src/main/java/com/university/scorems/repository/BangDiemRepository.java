package com.university.scorems.repository;

import com.university.scorems.model.BangDiem;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface BangDiemRepository extends JpaRepository<BangDiem, Long> {
    Optional<BangDiem> findByHocSinhIdAndMonHocId(Long hocSinhId, Long monHocId);

    List<BangDiem> findByMonHocIdOrderByHocSinhHoTenAsc(Long monHocId);
}
