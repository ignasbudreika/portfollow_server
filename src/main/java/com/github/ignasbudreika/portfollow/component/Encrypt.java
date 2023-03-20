package com.github.ignasbudreika.portfollow.component;

import org.springframework.beans.factory.annotation.Autowired;

import javax.persistence.AttributeConverter;

public class Encrypt implements AttributeConverter<String,String> {
    @Autowired
    EncryptionUtil encryptionUtil;

    @Override
    public String convertToDatabaseColumn(String s) {
        return encryptionUtil.encrypt(s);
    }

    @Override
    public String convertToEntityAttribute(String s) {
        return encryptionUtil.decrypt(s);
    }
}
