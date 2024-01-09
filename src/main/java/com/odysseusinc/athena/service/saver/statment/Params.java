package com.odysseusinc.athena.service.saver.statment;

import lombok.AllArgsConstructor;
import lombok.Getter;

import java.sql.JDBCType;
import java.util.List;

@AllArgsConstructor
@Getter
public class Params {
    private List<Object> ids;
    private Integer version;
    private Integer versionDelta;

    public Object getValue(PARAM_TYPE type) {
        switch (type) {
            case VOCABULARY_IDS:
                return ids;
            case VERSION:
                return version;
            case VERSION_DELTA:
                return versionDelta;
            default:
                throw new IllegalArgumentException("Unsupported PARAM_TYPE: " + type);
        }
    }

    public List<Object> getList(PARAM_TYPE type) {
        switch (type) {
            case VOCABULARY_IDS:
                return ids;
            default:
                throw new IllegalArgumentException("Unsupported PARAM_TYPE for getList: " + type);
        }
    }

    @AllArgsConstructor
    @Getter
    public enum PARAM_TYPE {
        VOCABULARY_IDS(":vocabularyIds", JDBCType.ARRAY),
        VERSION(":version", JDBCType.VARCHAR),
        VERSION_DELTA(":versionDelta", JDBCType.VARCHAR);

        private final String placeholder;
        private final JDBCType type;
    }
}