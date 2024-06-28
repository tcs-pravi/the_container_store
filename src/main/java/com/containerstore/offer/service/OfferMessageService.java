package com.containerstore.offer.service;

import com.containerstore.offer.domain.MessageType;
import com.containerstore.offer.domain.OfferMessage;

public interface OfferMessageService {
    OfferMessage getMessage(Long offerId, MessageType messageType);
}
