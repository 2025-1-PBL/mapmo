package com.pbl.mapmo;

import jakarta.persistence.Embeddable;
import lombok.*;

import java.io.Serializable;
import java.util.Objects;

@Embeddable
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class SharedScheduleMemberId implements Serializable {

    private Integer sharedScheduleId;
    private Integer userIdMember;

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        SharedScheduleMemberId that = (SharedScheduleMemberId) o;
        return Objects.equals(sharedScheduleId, that.sharedScheduleId) &&
                Objects.equals(userIdMember, that.userIdMember);
    }

    @Override
    public int hashCode() {
        return Objects.hash(sharedScheduleId, userIdMember);
    }
}
