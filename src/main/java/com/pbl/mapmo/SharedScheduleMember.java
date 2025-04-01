package com.pbl.mapmo;

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

    @ManyToOne
    @MapsId("sharedScheduleId")
    @JoinColumn(name = "shared_schedule_id", referencedColumnName = "id", foreignKey = @ForeignKey(name = "shared_schedule_member_ibfk_1"))
    private SharedSchedule sharedSchedule;

    @ManyToOne
    @MapsId("userIdMember")
    @JoinColumn(name = "user_id_member", referencedColumnName = "id", foreignKey = @ForeignKey(name = "shared_schedule_member_ibfk_2"))
    private User userMember;
}
