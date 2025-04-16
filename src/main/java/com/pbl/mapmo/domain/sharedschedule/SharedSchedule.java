package com.pbl.mapmo.domain.sharedschedule;

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

    @OneToOne
    @JoinColumn(name = "schedule_id")
    private Schedule schedule;

    @ManyToOne
    @JoinColumn(name = "user_id_master")
    private User userMaster;

    @OneToMany(mappedBy = "sharedSchedule")
    private List<SharedScheduleMember> sharedMembers;
}
