package com.kaki.doctrack.authservice.config.converter;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.convert.CustomConversions;
import org.springframework.data.r2dbc.convert.R2dbcCustomConversions;
import org.springframework.data.r2dbc.dialect.DialectResolver;
import org.springframework.data.r2dbc.dialect.R2dbcDialect;
import io.r2dbc.spi.ConnectionFactory;
import org.springframework.core.convert.converter.Converter;
import org.springframework.data.r2dbc.repository.config.EnableR2dbcRepositories;

import java.util.ArrayList;
import java.util.List;

@Configuration
@EnableR2dbcRepositories(basePackages = "com.kaki.doctrack.authservice.repository")
public class R2DBCConfig {

    @Bean
    public R2dbcCustomConversions r2dbcCustomConversions(ConnectionFactory connectionFactory, RoleReadConverter roleReadConverter, RoleWriteConverter roleWriteConverter) {
        R2dbcDialect dialect = DialectResolver.getDialect(connectionFactory);
        List<Converter<?, ?>> converters = new ArrayList<>();
        dialect.getConverters().forEach(converter -> converters.add((Converter<?, ?>) converter));
        converters.add(new ByteBufferToBooleanConverter()); // Add your custom converter here
        converters.add(roleReadConverter); // Add your custom converter here
        converters.add(roleWriteConverter);
        return new R2dbcCustomConversions(CustomConversions.StoreConversions.of(dialect.getSimpleTypeHolder(), converters), converters);
    }
}
