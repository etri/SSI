package com.iconloop.iitpvault.domain.mappers;

import java.sql.JDBCType;
import java.util.Date;
import javax.annotation.Generated;
import org.mybatis.dynamic.sql.SqlColumn;
import org.mybatis.dynamic.sql.SqlTable;

public final class TMasterStorageDynamicSqlSupport {
    @Generated(value="org.mybatis.generator.api.MyBatisGenerator", date="2020-09-14T10:48:45.774+09:00", comments="Source Table: t_master_storage")
    public static final TMasterStorage TMasterStorage = new TMasterStorage();

    @Generated(value="org.mybatis.generator.api.MyBatisGenerator", date="2020-09-14T10:48:45.774+09:00", comments="Source field: t_master_storage.id")
    public static final SqlColumn<Integer> id = TMasterStorage.id;

    @Generated(value="org.mybatis.generator.api.MyBatisGenerator", date="2020-09-14T10:48:45.775+09:00", comments="Source field: t_master_storage.did")
    public static final SqlColumn<String> did = TMasterStorage.did;

    @Generated(value="org.mybatis.generator.api.MyBatisGenerator", date="2020-09-14T10:48:45.775+09:00", comments="Source field: t_master_storage.recovery_key")
    public static final SqlColumn<String> recoveryKey = TMasterStorage.recoveryKey;

    @Generated(value="org.mybatis.generator.api.MyBatisGenerator", date="2020-09-14T10:48:45.776+09:00", comments="Source field: t_master_storage.created")
    public static final SqlColumn<Date> created = TMasterStorage.created;

    @Generated(value="org.mybatis.generator.api.MyBatisGenerator", date="2020-09-14T10:48:45.774+09:00", comments="Source Table: t_master_storage")
    public static final class TMasterStorage extends SqlTable {
        public final SqlColumn<Integer> id = column("id", JDBCType.INTEGER);

        public final SqlColumn<String> did = column("did", JDBCType.VARCHAR);

        public final SqlColumn<String> recoveryKey = column("recovery_key", JDBCType.VARCHAR);

        public final SqlColumn<Date> created = column("created", JDBCType.TIMESTAMP);

        public TMasterStorage() {
            super("t_master_storage");
        }
    }
}