package com.odysseusinc.athena.service.saver.v5.history;

import com.odysseusinc.athena.model.athena.DownloadBundle;
import com.odysseusinc.athena.service.saver.CSVSaver;
import com.odysseusinc.athena.service.saver.statment.NamedStatementCreator;
import com.odysseusinc.athena.service.saver.statment.Placeholders;
import com.odysseusinc.athena.util.CDMVersion;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.List;

public abstract class HistorySaver extends CSVSaver {


    @Autowired
    @Qualifier("dataSourceAthenaV5History")
    protected DataSource v5HistoryDataSource;

    private final NamedStatementCreator statementCreator = new NamedStatementCreator();

    @Override
    protected DataSource getDataSource(CDMVersion currentVersion) {
        return v5HistoryDataSource;
    }

    @Override
    protected <T> PreparedStatement getStatement(List ids, Connection conn, DownloadBundle bundle) throws SQLException {
        return statementCreator.getStatement(conn, query(), new Placeholders(ids, bundle.getVocabularyVersion(),  bundle.getDeltaVersion()), bundle.getCdmVersion());
    }
}
