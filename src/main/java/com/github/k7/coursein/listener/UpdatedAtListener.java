package com.github.k7.coursein.listener;

import com.github.k7.coursein.event.UpdatedAtAware;
import com.github.k7.coursein.util.TimeUtil;

import javax.persistence.PrePersist;
import javax.persistence.PreUpdate;
import java.time.LocalDateTime;

public class UpdatedAtListener {

    @PrePersist
    @PreUpdate
    void setLastUpdatedAt(Object entity) {
        LocalDateTime localDateTime = TimeUtil.getFormattedLocalDateTimeNow();
        if (entity instanceof UpdatedAtAware) {
            UpdatedAtAware updatedAtAware = (UpdatedAtAware) entity;
            updatedAtAware.setUpdatedAt(localDateTime);
        }
    }

}
