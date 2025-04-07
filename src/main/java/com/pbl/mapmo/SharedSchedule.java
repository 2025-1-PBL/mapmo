package com.pbl.mapmo;

import jakarta.persistence.*;
import lombok.*;

import java.util.List;

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

    @OneToOne
    @JoinColumn(name = "schedule_id")
    private Schedule schedule;

    @ManyToOne
    @JoinColumn(name = "user_id_master")
    private User userMaster;

    @OneToMany(mappedBy = "sharedSchedule")
    private List<SharedScheduleMember> sharedMembers;
}
