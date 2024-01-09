package com.odysseusinc.athena.service.saver;

import com.odysseusinc.athena.model.athena.DownloadBundle;

import java.util.List;
import java.util.zip.ZipOutputStream;

public abstract class SQLSaver extends Saver implements ISaver {

    @Override
    public <T> void save(ZipOutputStream zos, DownloadBundle bundle, List<T> ids) {

        if (!includedInBundle(ids)) {
            return;
        }
        List<T> vocabularyIds = filter(ids);
//todo dev code for save SQL file here
    }

}
