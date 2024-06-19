package com.kaki.doctrack.authservice.config.converter;

import org.springframework.core.convert.converter.Converter;
import java.nio.ByteBuffer;

public class ByteBufferToBooleanConverter implements Converter<ByteBuffer, Boolean> {

    @Override
    public Boolean convert(ByteBuffer source) {
        if (source.remaining() == 0) {
            return null;
        }
        return source.get(0) != 0;
    }
}
