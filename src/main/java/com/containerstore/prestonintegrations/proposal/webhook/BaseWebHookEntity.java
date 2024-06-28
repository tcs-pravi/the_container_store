package com.containerstore.prestonintegrations.proposal.webhook;

import lombok.Getter;
import org.hibernate.annotations.GenericGenerator;

import javax.persistence.*;
import java.time.ZonedDateTime;
import java.util.UUID;


@MappedSuperclass
@Getter
public abstract class BaseWebHookEntity {

    @Id
    @GeneratedValue(generator = "UUID")
    @GenericGenerator(
            name = "UUID",
            strategy = "org.hibernate.id.UUIDGenerator"
    )
    @Column(name = "id")
    private UUID id;

    private String eventId;

    private ZonedDateTime createdTime;

    private ZonedDateTime modifiedTime;

    @Enumerated(value = EnumType.STRING)
    private WebHookConsumers app;

    @Version
    private int version;

    public void setCreatedTime(ZonedDateTime createdTime) {
        this.createdTime = createdTime;
    }

    public void setModifiedTime(ZonedDateTime modifiedTime) {
        this.modifiedTime = modifiedTime;
    }

    public void setApp(WebHookConsumers app) {
        this.app = app;
    }

    public void setEventId(String eventId) {
        this.eventId = eventId;
    }
}
