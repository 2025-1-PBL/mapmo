package com.pbl.mapmo.domain.franchise;

import com.pbl.mapmo.domain.brand.Brand;
import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "franchise")
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@ToString
public class Franchise {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @Column(length = 100, nullable = false)
    private String name;

    @Column(length = 255)
    private String location;

    @Column
    private Double latitude;

    @Column
    private Double longitude;

    // 브랜드와 연결 (ManyToOne 관계)
    @ManyToOne
    @JoinColumn(name = "brand_id", referencedColumnName = "id", foreignKey = @ForeignKey(name = "franchise_ibfk_1"))
    private Brand brand;
}
