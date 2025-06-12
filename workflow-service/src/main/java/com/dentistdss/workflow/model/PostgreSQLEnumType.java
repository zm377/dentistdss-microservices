package com.dentistdss.workflow.model;

import jakarta.persistence.AttributeConverter;
import jakarta.persistence.Converter;

/**
 * PostgreSQL Enum Type converter for Hibernate
 *
 * This class provides enum handling for PostgreSQL databases.
 *
 * @author zhifeimi
 * @email zm377@uowmail.edu.au
 * @github https://github.com/zm377
 */
@Converter
public class PostgreSQLEnumType implements AttributeConverter<Enum<?>, String> {

    @Override
    public String convertToDatabaseColumn(Enum<?> attribute) {
        return attribute != null ? attribute.name() : null;
    }

    @Override
    public Enum<?> convertToEntityAttribute(String dbData) {
        // This is a generic converter, specific enum converters should be created for each enum type
        return null;
    }
}
