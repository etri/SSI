package com.iconloop.iitpvault.domain.mappers;

import java.sql.JDBCType;
import java.util.Date;
import javax.annotation.Generated;
import org.mybatis.dynamic.sql.SqlColumn;
import org.mybatis.dynamic.sql.SqlTable;

public final class TStorageRecoveryDynamicSqlSupport {
    @Generated(value="org.mybatis.generator.api.MyBatisGenerator", date="2020-09-14T10:48:45.812+09:00", comments="Source Table: t_storage_recovery")
    public static final TStorageRecovery TStorageRecovery = new TStorageRecovery();

    @Generated(value="org.mybatis.generator.api.MyBatisGenerator", date="2020-09-14T10:48:45.812+09:00", comments="Source field: t_storage_recovery.id")
    public static final SqlColumn<Integer> id = TStorageRecovery.id;

    /**
     * Database Column Remarks:
     *   email or sms
     */
    @Generated(value="org.mybatis.generator.api.MyBatisGenerator", date="2020-09-14T10:48:45.812+09:00", comments="Source field: t_storage_recovery.auth_id")
    public static final SqlColumn<String> authId = TStorageRecovery.authId;

    @Generated(value="org.mybatis.generator.api.MyBatisGenerator", date="2020-09-14T10:48:45.812+09:00", comments="Source field: t_storage_recovery.shared_clue")
    public static final SqlColumn<String> sharedClue = TStorageRecovery.sharedClue;

    @Generated(value="org.mybatis.generator.api.MyBatisGenerator", date="2020-09-14T10:48:45.813+09:00", comments="Source field: t_storage_recovery.created")
    public static final SqlColumn<Date> created = TStorageRecovery.created;

    @Generated(value="org.mybatis.generator.api.MyBatisGenerator", date="2020-09-14T10:48:45.812+09:00", comments="Source Table: t_storage_recovery")
    public static final class TStorageRecovery extends SqlTable {
        public final SqlColumn<Integer> id = column("id", JDBCType.INTEGER);

        public final SqlColumn<String> authId = column("auth_id", JDBCType.VARCHAR);

        public final SqlColumn<String> sharedClue = column("shared_clue", JDBCType.VARCHAR);

        public final SqlColumn<Date> created = column("created", JDBCType.TIMESTAMP);

        public TStorageRecovery() {
            super("t_storage_recovery");
        }
    }
}