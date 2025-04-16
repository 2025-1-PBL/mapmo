package com.pbl.mapmo.domain.brand;

import com.pbl.mapmo.domain.event.Event;
import com.pbl.mapmo.domain.franchise.Franchise;
import jakarta.persistence.*;
import lombok.*;

import java.util.List;

@Entity
@Table(name = "brand")
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@ToString(exclude = {"franchises", "events"})
public class Brand {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @Column(length = 100, nullable = false)
    private String name;

    @OneToMany(mappedBy = "brand")
    private List<Franchise> franchises;

    @OneToMany(mappedBy = "brand")
    private List<Event> events;
}
