package com.iconloop.iitpvault.domain.mappers;

import static com.iconloop.iitpvault.domain.mappers.TMasterStorageDynamicSqlSupport.*;
import static org.mybatis.dynamic.sql.SqlBuilder.*;

import com.iconloop.iitpvault.domain.dao.TMasterStorage;
import java.util.Collection;
import java.util.List;
import java.util.Optional;
import javax.annotation.Generated;
import org.apache.ibatis.annotations.DeleteProvider;
import org.apache.ibatis.annotations.Insert;
import org.apache.ibatis.annotations.InsertProvider;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Options;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Result;
import org.apache.ibatis.annotations.ResultMap;
import org.apache.ibatis.annotations.Results;
import org.apache.ibatis.annotations.SelectProvider;
import org.apache.ibatis.annotations.UpdateProvider;
import org.apache.ibatis.type.JdbcType;
import org.mybatis.dynamic.sql.BasicColumn;
import org.mybatis.dynamic.sql.delete.DeleteDSLCompleter;
import org.mybatis.dynamic.sql.delete.render.DeleteStatementProvider;
import org.mybatis.dynamic.sql.insert.render.InsertStatementProvider;
import org.mybatis.dynamic.sql.insert.render.MultiRowInsertStatementProvider;
import org.mybatis.dynamic.sql.select.CountDSLCompleter;
import org.mybatis.dynamic.sql.select.SelectDSLCompleter;
import org.mybatis.dynamic.sql.select.render.SelectStatementProvider;
import org.mybatis.dynamic.sql.update.UpdateDSL;
import org.mybatis.dynamic.sql.update.UpdateDSLCompleter;
import org.mybatis.dynamic.sql.update.UpdateModel;
import org.mybatis.dynamic.sql.update.render.UpdateStatementProvider;
import org.mybatis.dynamic.sql.util.SqlProviderAdapter;
import org.mybatis.dynamic.sql.util.mybatis3.MyBatis3Utils;

@Mapper
public interface TMasterStorageMapper {
    @Generated(value="org.mybatis.generator.api.MyBatisGenerator", date="2020-09-14T10:48:45.801+09:00", comments="Source Table: t_master_storage")
    BasicColumn[] selectList = BasicColumn.columnList(id, did, recoveryKey, created);

    @Generated(value="org.mybatis.generator.api.MyBatisGenerator", date="2020-09-14T10:48:45.777+09:00", comments="Source Table: t_master_storage")
    @SelectProvider(type=SqlProviderAdapter.class, method="select")
    long count(SelectStatementProvider selectStatement);

    @Generated(value="org.mybatis.generator.api.MyBatisGenerator", date="2020-09-14T10:48:45.778+09:00", comments="Source Table: t_master_storage")
    @DeleteProvider(type=SqlProviderAdapter.class, method="delete")
    int delete(DeleteStatementProvider deleteStatement);

    @Generated(value="org.mybatis.generator.api.MyBatisGenerator", date="2020-09-14T10:48:45.779+09:00", comments="Source Table: t_master_storage")
    @InsertProvider(type=SqlProviderAdapter.class, method="insert")
    @Options(useGeneratedKeys=true,keyProperty="record.id")
    int insert(InsertStatementProvider<TMasterStorage> insertStatement);

    @Generated(value="org.mybatis.generator.api.MyBatisGenerator", date="2020-09-14T10:48:45.782+09:00", comments="Source Table: t_master_storage")
    @Insert({
        "${insertStatement}"
    })
    @Options(useGeneratedKeys=true,keyProperty="records.id")
    int insertMultiple(@Param("insertStatement") String insertStatement, @Param("records") List<TMasterStorage> records);

    @Generated(value="org.mybatis.generator.api.MyBatisGenerator", date="2020-09-14T10:48:45.79+09:00", comments="Source Table: t_master_storage")
    default int insertMultiple(MultiRowInsertStatementProvider<TMasterStorage> multipleInsertStatement) {
        return insertMultiple(multipleInsertStatement.getInsertStatement(), multipleInsertStatement.getRecords());
    }

    @Generated(value="org.mybatis.generator.api.MyBatisGenerator", date="2020-09-14T10:48:45.792+09:00", comments="Source Table: t_master_storage")
    @SelectProvider(type=SqlProviderAdapter.class, method="select")
    @ResultMap("TMasterStorageResult")
    Optional<TMasterStorage> selectOne(SelectStatementProvider selectStatement);

    @Generated(value="org.mybatis.generator.api.MyBatisGenerator", date="2020-09-14T10:48:45.792+09:00", comments="Source Table: t_master_storage")
    @SelectProvider(type=SqlProviderAdapter.class, method="select")
    @Results(id="TMasterStorageResult", value = {
        @Result(column="id", property="id", jdbcType=JdbcType.INTEGER, id=true),
        @Result(column="did", property="did", jdbcType=JdbcType.VARCHAR),
        @Result(column="recovery_key", property="recoveryKey", jdbcType=JdbcType.VARCHAR),
        @Result(column="created", property="created", jdbcType=JdbcType.TIMESTAMP)
    })
    List<TMasterStorage> selectMany(SelectStatementProvider selectStatement);

    @Generated(value="org.mybatis.generator.api.MyBatisGenerator", date="2020-09-14T10:48:45.794+09:00", comments="Source Table: t_master_storage")
    @UpdateProvider(type=SqlProviderAdapter.class, method="update")
    int update(UpdateStatementProvider updateStatement);

    @Generated(value="org.mybatis.generator.api.MyBatisGenerator", date="2020-09-14T10:48:45.794+09:00", comments="Source Table: t_master_storage")
    default long count(CountDSLCompleter completer) {
        return MyBatis3Utils.countFrom(this::count, TMasterStorage, completer);
    }

    @Generated(value="org.mybatis.generator.api.MyBatisGenerator", date="2020-09-14T10:48:45.795+09:00", comments="Source Table: t_master_storage")
    default int delete(DeleteDSLCompleter completer) {
        return MyBatis3Utils.deleteFrom(this::delete, TMasterStorage, completer);
    }

    @Generated(value="org.mybatis.generator.api.MyBatisGenerator", date="2020-09-14T10:48:45.796+09:00", comments="Source Table: t_master_storage")
    default int deleteByPrimaryKey(Integer id_) {
        return delete(c -> 
            c.where(id, isEqualTo(id_))
        );
    }

    @Generated(value="org.mybatis.generator.api.MyBatisGenerator", date="2020-09-14T10:48:45.796+09:00", comments="Source Table: t_master_storage")
    default int insert(TMasterStorage record) {
        return MyBatis3Utils.insert(this::insert, record, TMasterStorage, c ->
            c.map(did).toProperty("did")
            .map(recoveryKey).toProperty("recoveryKey")
            .map(created).toProperty("created")
        );
    }

    @Generated(value="org.mybatis.generator.api.MyBatisGenerator", date="2020-09-14T10:48:45.798+09:00", comments="Source Table: t_master_storage")
    default int insertMultiple(Collection<TMasterStorage> records) {
        return MyBatis3Utils.insertMultiple(this::insertMultiple, records, TMasterStorage, c ->
            c.map(did).toProperty("did")
            .map(recoveryKey).toProperty("recoveryKey")
            .map(created).toProperty("created")
        );
    }

    @Generated(value="org.mybatis.generator.api.MyBatisGenerator", date="2020-09-14T10:48:45.799+09:00", comments="Source Table: t_master_storage")
    default int insertSelective(TMasterStorage record) {
        return MyBatis3Utils.insert(this::insert, record, TMasterStorage, c ->
            c.map(did).toPropertyWhenPresent("did", record::getDid)
            .map(recoveryKey).toPropertyWhenPresent("recoveryKey", record::getRecoveryKey)
            .map(created).toPropertyWhenPresent("created", record::getCreated)
        );
    }

    @Generated(value="org.mybatis.generator.api.MyBatisGenerator", date="2020-09-14T10:48:45.803+09:00", comments="Source Table: t_master_storage")
    default Optional<TMasterStorage> selectOne(SelectDSLCompleter completer) {
        return MyBatis3Utils.selectOne(this::selectOne, selectList, TMasterStorage, completer);
    }

    @Generated(value="org.mybatis.generator.api.MyBatisGenerator", date="2020-09-14T10:48:45.804+09:00", comments="Source Table: t_master_storage")
    default List<TMasterStorage> select(SelectDSLCompleter completer) {
        return MyBatis3Utils.selectList(this::selectMany, selectList, TMasterStorage, completer);
    }

    @Generated(value="org.mybatis.generator.api.MyBatisGenerator", date="2020-09-14T10:48:45.804+09:00", comments="Source Table: t_master_storage")
    default List<TMasterStorage> selectDistinct(SelectDSLCompleter completer) {
        return MyBatis3Utils.selectDistinct(this::selectMany, selectList, TMasterStorage, completer);
    }

    @Generated(value="org.mybatis.generator.api.MyBatisGenerator", date="2020-09-14T10:48:45.805+09:00", comments="Source Table: t_master_storage")
    default Optional<TMasterStorage> selectByPrimaryKey(Integer id_) {
        return selectOne(c ->
            c.where(id, isEqualTo(id_))
        );
    }

    @Generated(value="org.mybatis.generator.api.MyBatisGenerator", date="2020-09-14T10:48:45.805+09:00", comments="Source Table: t_master_storage")
    default int update(UpdateDSLCompleter completer) {
        return MyBatis3Utils.update(this::update, TMasterStorage, completer);
    }

    @Generated(value="org.mybatis.generator.api.MyBatisGenerator", date="2020-09-14T10:48:45.806+09:00", comments="Source Table: t_master_storage")
    static UpdateDSL<UpdateModel> updateAllColumns(TMasterStorage record, UpdateDSL<UpdateModel> dsl) {
        return dsl.set(did).equalTo(record::getDid)
                .set(recoveryKey).equalTo(record::getRecoveryKey)
                .set(created).equalTo(record::getCreated);
    }

    @Generated(value="org.mybatis.generator.api.MyBatisGenerator", date="2020-09-14T10:48:45.807+09:00", comments="Source Table: t_master_storage")
    static UpdateDSL<UpdateModel> updateSelectiveColumns(TMasterStorage record, UpdateDSL<UpdateModel> dsl) {
        return dsl.set(did).equalToWhenPresent(record::getDid)
                .set(recoveryKey).equalToWhenPresent(record::getRecoveryKey)
                .set(created).equalToWhenPresent(record::getCreated);
    }

    @Generated(value="org.mybatis.generator.api.MyBatisGenerator", date="2020-09-14T10:48:45.808+09:00", comments="Source Table: t_master_storage")
    default int updateByPrimaryKey(TMasterStorage record) {
        return update(c ->
            c.set(did).equalTo(record::getDid)
            .set(recoveryKey).equalTo(record::getRecoveryKey)
            .set(created).equalTo(record::getCreated)
            .where(id, isEqualTo(record::getId))
        );
    }

    @Generated(value="org.mybatis.generator.api.MyBatisGenerator", date="2020-09-14T10:48:45.809+09:00", comments="Source Table: t_master_storage")
    default int updateByPrimaryKeySelective(TMasterStorage record) {
        return update(c ->
            c.set(did).equalToWhenPresent(record::getDid)
            .set(recoveryKey).equalToWhenPresent(record::getRecoveryKey)
            .set(created).equalToWhenPresent(record::getCreated)
            .where(id, isEqualTo(record::getId))
        );
    }
}