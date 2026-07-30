package com.odysseusinc.athena.service.saver.statment;

import lombok.AllArgsConstructor;
import lombok.Getter;

import java.util.List;
import java.util.Objects;

import static com.odysseusinc.athena.service.saver.statment.SqlStatementCreator.TYPE.*;

@AllArgsConstructor
@Getter
public class Placeholders {
    private List<Object> ids;
    private Integer version;
    private Integer versionDelta;

    public Object getValue(PLACEHOLDER_TYPE type) {
        switch (type) {
            case VOCABULARY_IDS:
                return ids;
            case VOCABULARY_ARR:
                return ids.toArray(new Object[0]);
            case VERSION:
                return version;
            case VERSION_DELTA:
                return versionDelta;
            default:
                throw new IllegalArgumentException("Unsupported PARAM_TYPE: " + type);
        }
    }

    public List<Object> getList(PLACEHOLDER_TYPE type) {
        if (Objects.requireNonNull(type) == PLACEHOLDER_TYPE.VOCABULARY_IDS) {
            return ids;
        }
        throw new IllegalArgumentException("Unsupported PARAM_TYPE for getList: " + type);
    }

    @AllArgsConstructor
    @Getter
    public enum PLACEHOLDER_TYPE {
        VOCABULARY_IDS(":vocabularyIds", IN_VALUES),
        VOCABULARY_ARR(":vocabularyArr", VARCHAR_ARRAY),
        VERSION(":version", VARCHAR),
        VERSION_DELTA(":versionDelta", VARCHAR);

        private final String placeholder;
        private final SqlStatementCreator.TYPE type;

    }


}