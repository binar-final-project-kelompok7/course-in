package com.github.k7.coursein.listener;

import com.github.k7.coursein.event.CreatedAtAware;
import com.github.k7.coursein.util.TimeUtil;

import javax.persistence.PrePersist;
import java.time.LocalDateTime;

public class CreatedAtListener {

    @PrePersist
    void setLastCreatedAt(Object entity) {
        LocalDateTime localDateTime = TimeUtil.getFormattedLocalDateTimeNow();
        if (entity instanceof CreatedAtAware) {
            CreatedAtAware createdAtAware = (CreatedAtAware) entity;
            createdAtAware.setCreatedAt(localDateTime);
        }
    }

}
