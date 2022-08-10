package com.cy.readygo.core.serializer;

import org.springframework.core.serializer.support.SerializationFailedException;
import org.springframework.lang.Nullable;

public interface RedisXSerializer<T>{

   T deserialize(@Nullable byte[] bytes) throws  SerializationFailedException;

   byte[] sericalize(@Nullable T var1) throws SerializationFailedException;

}
