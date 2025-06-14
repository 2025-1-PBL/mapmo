package com.pbl.mapmo.domain.sharedschedule;

import com.fasterxml.jackson.annotation.JsonBackReference;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.pbl.mapmo.domain.schedule.Schedule;
import com.pbl.mapmo.domain.user.User;
import com.pbl.mapmo.domain.sharedschedulemember.SharedScheduleMember;
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

    @JsonBackReference
    @OneToOne
    @JoinColumn(name = "schedule_id")
    private Schedule schedule;

    @JsonBackReference
    @ManyToOne
    @JoinColumn(name = "user_id_master")
    private User userMaster;

    @JsonIgnore
    @OneToMany(mappedBy = "sharedSchedule")
    private List<SharedScheduleMember> sharedMembers;
}
