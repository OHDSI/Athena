package org.odhsi.athena.dbstored;

import org.springframework.jdbc.core.SqlParameter;
import org.springframework.jdbc.object.SqlFunction;

import javax.sql.DataSource;
import java.sql.Types;

/**
 * Created by GMalikov on 15.05.2015.
 */
public class SfGetCurrentStatus extends SqlFunction<String>{
    private static final String SQL = "select DEV_TIMUR.PKG_VOCABULARY.GetCurrentStatusString(?) from dual";

    public SfGetCurrentStatus(DataSource dataSource){
        super(dataSource, SQL);
        declareParameter(new SqlParameter(Types.VARCHAR));
        compile();
    }
}