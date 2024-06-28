package com.containerstore.prestonintegrations.proposal.webhook;

import org.springframework.stereotype.Service;

import java.util.Collection;

@Service
public interface WebHookService<T> {

    <E extends BaseWebHookEntity> Collection<E> getWebhookPersistedDataByKeyAndApp(String key, WebHookConsumers app);


    void handleRequest(T request);

    <E extends BaseWebHookEntity> void syncDatabase(String eventId,E entity);

    void log(T request);

    int deleteEntry(String key, WebHookConsumers app);
}
