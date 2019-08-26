package com.odysseusinc.athena.api.v1.controller.converter.vocabulary;

import com.odysseusinc.athena.api.v1.controller.dto.vocabulary.VocabularyVersionDTO;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.convert.converter.Converter;
import org.springframework.core.convert.support.GenericConversionService;
import org.springframework.stereotype.Component;

@Component
public class StringToVocabularyVersionDTOConverter implements Converter<String, VocabularyVersionDTO>, InitializingBean {

    private final GenericConversionService conversionService;

    @Autowired
    public StringToVocabularyVersionDTOConverter(GenericConversionService conversionService) {

        this.conversionService = conversionService;
    }

    @Override
    public void afterPropertiesSet() {

        conversionService.addConverter(this);

    }

    @Override
    public VocabularyVersionDTO convert(String version) {

        return new VocabularyVersionDTO(version);
    }
}
