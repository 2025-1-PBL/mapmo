package com.pbl.mapmo.domain.sharedschedulemember;

import com.pbl.mapmo.domain.sharedschedule.SharedSchedule;
import com.pbl.mapmo.domain.user.User;
import com.pbl.mapmo.domain.sharedschedulememberid.SharedScheduleMemberId;
import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "shared_schedule_member")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@ToString
public class SharedScheduleMember {

    @EmbeddedId
    private SharedScheduleMemberId id;

    @MapsId("sharedScheduleId")
    @ManyToOne
    @JoinColumn(name = "shared_schedule_id")
    private SharedSchedule sharedSchedule;

    @MapsId("userIdMember")
    @ManyToOne
    @JoinColumn(name = "user_id_member")
    private User userMember;
}
