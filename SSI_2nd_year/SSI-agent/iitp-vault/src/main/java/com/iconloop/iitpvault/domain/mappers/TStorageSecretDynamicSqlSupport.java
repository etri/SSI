package com.iconloop.iitpvault.domain.mappers;

import java.sql.JDBCType;
import java.util.Date;
import javax.annotation.Generated;
import org.mybatis.dynamic.sql.SqlColumn;
import org.mybatis.dynamic.sql.SqlTable;

public final class TStorageSecretDynamicSqlSupport {
    @Generated(value="org.mybatis.generator.api.MyBatisGenerator", date="2020-09-14T10:48:45.815+09:00", comments="Source Table: t_storage_secret")
    public static final TStorageSecret TStorageSecret = new TStorageSecret();

    @Generated(value="org.mybatis.generator.api.MyBatisGenerator", date="2020-09-14T10:48:45.816+09:00", comments="Source field: t_storage_secret.id")
    public static final SqlColumn<Integer> id = TStorageSecret.id;

    /**
     * Database Column Remarks:
     *   email or sms
     */
    @Generated(value="org.mybatis.generator.api.MyBatisGenerator", date="2020-09-14T10:48:45.816+09:00", comments="Source field: t_storage_secret.auth_id")
    public static final SqlColumn<String> authId = TStorageSecret.authId;

    @Generated(value="org.mybatis.generator.api.MyBatisGenerator", date="2020-09-14T10:48:45.816+09:00", comments="Source field: t_storage_secret.shared_clue")
    public static final SqlColumn<String> sharedClue = TStorageSecret.sharedClue;

    @Generated(value="org.mybatis.generator.api.MyBatisGenerator", date="2020-09-14T10:48:45.816+09:00", comments="Source field: t_storage_secret.created")
    public static final SqlColumn<Date> created = TStorageSecret.created;

    @Generated(value="org.mybatis.generator.api.MyBatisGenerator", date="2020-09-14T10:48:45.815+09:00", comments="Source Table: t_storage_secret")
    public static final class TStorageSecret extends SqlTable {
        public final SqlColumn<Integer> id = column("id", JDBCType.INTEGER);

        public final SqlColumn<String> authId = column("auth_id", JDBCType.VARCHAR);

        public final SqlColumn<String> sharedClue = column("shared_clue", JDBCType.VARCHAR);

        public final SqlColumn<Date> created = column("created", JDBCType.TIMESTAMP);

        public TStorageSecret() {
            super("t_storage_secret");
        }
    }
}