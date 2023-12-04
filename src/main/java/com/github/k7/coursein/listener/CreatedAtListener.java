package com.github.k7.coursein.listener;

import com.github.k7.coursein.event.CreatedAtAware;
import com.github.k7.coursein.util.TimeUtil;

import javax.persistence.PrePersist;
import javax.persistence.PreUpdate;
import java.time.LocalDateTime;

public class CreatedAtListener {

    @PrePersist
    @PreUpdate
    void setLastCreatedAt(Object entity) {
        LocalDateTime localDateTime = TimeUtil.getFormattedLocalDateTimeNow();
        if (entity instanceof CreatedAtAware) {
            CreatedAtAware createdAtAware = (CreatedAtAware) entity;
            createdAtAware.setCreatedAt(localDateTime);
        }
    }

}
