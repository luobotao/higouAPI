package models;

import utils.StringCodec;

import javax.persistence.AttributeConverter;
import javax.persistence.Converter;

@Converter
public class EncryptStringConvert implements AttributeConverter<String, String> {
    @Override
    public String convertToDatabaseColumn(String attribute) {
        return StringCodec.encode(attribute);
    }

    @Override
    public String convertToEntityAttribute(String dbData) {
        return StringCodec.decode(dbData);
    }
}
