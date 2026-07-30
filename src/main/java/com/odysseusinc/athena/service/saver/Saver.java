package com.odysseusinc.athena.service.saver;

import com.odysseusinc.athena.service.DownloadBundleService;
import com.odysseusinc.athena.service.writer.FileHelper;
import com.odysseusinc.athena.util.CDMVersion;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;

import javax.sql.DataSource;
import java.util.Collections;
import java.util.List;

public abstract class Saver {
    @Autowired
    protected DownloadBundleService bundleService;
    @Autowired
    @Qualifier("dataSourceAthenaV5")
    protected DataSource v5DataSource;
    @Autowired
    @Qualifier("dataSourceAthenaV4")
    protected DataSource v4DataSource;
    @Autowired
    FileHelper fileHelper;

    public abstract String fileName();

    protected abstract String query();

    protected DataSource getDataSource(CDMVersion currentVersion) {
        return CDMVersion.V4_5 == currentVersion ? v4DataSource : v5DataSource;
    }

    protected <T> List<T> filter(List<T> ids) {

        return ids;
    }

    public boolean containCpt4(List ids) {

        return false;
    }

    public boolean includedInBundle(List ids) {

        return true;
    }


    public List getIds() {

        return Collections.emptyList();
    }
}
