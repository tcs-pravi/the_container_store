package com.containerstore.common.jackson;

import com.containerstore.spring.module.rest.jackson2.ObjectMappers;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.databind.ObjectMapper;

public class OffersDefaultObjectMapper extends ObjectMapper {
  public OffersDefaultObjectMapper() {
    ObjectMappers.applyMappingRules(this);
  }

  public OffersDefaultObjectMapper(ObjectMapper src) {
    super(src);
    ObjectMappers.applyMappingRules(this);
  }

  @Override
  public ObjectMapper copy() {
    return new OffersDefaultObjectMapper(this);
  }

  public abstract static class ThrowableMixin {
    public ThrowableMixin() {
    }

    @JsonIgnore
    public abstract Throwable getCause();

    @JsonIgnore
    public abstract StackTraceElement[] getStackTrace();
  }
}
