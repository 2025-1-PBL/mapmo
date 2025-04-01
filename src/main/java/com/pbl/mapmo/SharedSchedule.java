package com.pbl.mapmo;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "shared_schedule")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@ToString
public class SharedSchedule {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @ManyToOne
    @JoinColumn(name = "schedule_id", referencedColumnName = "id", foreignKey = @ForeignKey(name = "shared_schedule_ibfk_1"))
    private Schedule schedule;

    @ManyToOne
    @JoinColumn(name = "user_id_master", referencedColumnName = "id", foreignKey = @ForeignKey(name = "shared_schedule_ibfk_2"))
    private User userMaster;
}
