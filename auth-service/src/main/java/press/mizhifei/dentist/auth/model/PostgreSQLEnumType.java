package press.mizhifei.dentist.auth.model;

import jakarta.persistence.AttributeConverter;
import jakarta.persistence.Converter;

/**
 *
 * @author zhifeimi
 * @email zm377@uowmail.edu.au
 * @github https://github.com/zm377
 *
 */
/**
 * Converter for PostgreSQL enum types.
 * This converter will properly handle the conversion between Java enum values and 
 * PostgreSQL custom enum types during SQL operations.
 */
@Converter
public class PostgreSQLEnumType implements AttributeConverter<User.ApprovalStatus, String> {

    @Override
    public String convertToDatabaseColumn(User.ApprovalStatus attribute) {
        if (attribute == null) {
            return null;
        }
        return attribute.name();
    }

    @Override
    public User.ApprovalStatus convertToEntityAttribute(String dbData) {
        if (dbData == null) {
            return null;
        }
        return User.ApprovalStatus.valueOf(dbData);
    }
} 