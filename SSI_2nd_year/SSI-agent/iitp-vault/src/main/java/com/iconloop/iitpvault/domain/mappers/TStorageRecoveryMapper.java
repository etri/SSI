package com.iconloop.iitpvault.domain.mappers;

import static com.iconloop.iitpvault.domain.mappers.TStorageRecoveryDynamicSqlSupport.*;
import static org.mybatis.dynamic.sql.SqlBuilder.*;

import com.iconloop.iitpvault.domain.dao.TStorageRecovery;
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
public interface TStorageRecoveryMapper {
    @Generated(value="org.mybatis.generator.api.MyBatisGenerator", date="2020-09-14T10:48:45.814+09:00", comments="Source Table: t_storage_recovery")
    BasicColumn[] selectList = BasicColumn.columnList(id, authId, sharedClue, created);

    @Generated(value="org.mybatis.generator.api.MyBatisGenerator", date="2020-09-14T10:48:45.813+09:00", comments="Source Table: t_storage_recovery")
    @SelectProvider(type=SqlProviderAdapter.class, method="select")
    long count(SelectStatementProvider selectStatement);

    @Generated(value="org.mybatis.generator.api.MyBatisGenerator", date="2020-09-14T10:48:45.813+09:00", comments="Source Table: t_storage_recovery")
    @DeleteProvider(type=SqlProviderAdapter.class, method="delete")
    int delete(DeleteStatementProvider deleteStatement);

    @Generated(value="org.mybatis.generator.api.MyBatisGenerator", date="2020-09-14T10:48:45.813+09:00", comments="Source Table: t_storage_recovery")
    @InsertProvider(type=SqlProviderAdapter.class, method="insert")
    @Options(useGeneratedKeys=true,keyProperty="record.id")
    int insert(InsertStatementProvider<TStorageRecovery> insertStatement);

    @Generated(value="org.mybatis.generator.api.MyBatisGenerator", date="2020-09-14T10:48:45.813+09:00", comments="Source Table: t_storage_recovery")
    @Insert({
        "${insertStatement}"
    })
    @Options(useGeneratedKeys=true,keyProperty="records.id")
    int insertMultiple(@Param("insertStatement") String insertStatement, @Param("records") List<TStorageRecovery> records);

    @Generated(value="org.mybatis.generator.api.MyBatisGenerator", date="2020-09-14T10:48:45.813+09:00", comments="Source Table: t_storage_recovery")
    default int insertMultiple(MultiRowInsertStatementProvider<TStorageRecovery> multipleInsertStatement) {
        return insertMultiple(multipleInsertStatement.getInsertStatement(), multipleInsertStatement.getRecords());
    }

    @Generated(value="org.mybatis.generator.api.MyBatisGenerator", date="2020-09-14T10:48:45.813+09:00", comments="Source Table: t_storage_recovery")
    @SelectProvider(type=SqlProviderAdapter.class, method="select")
    @ResultMap("TStorageRecoveryResult")
    Optional<TStorageRecovery> selectOne(SelectStatementProvider selectStatement);

    @Generated(value="org.mybatis.generator.api.MyBatisGenerator", date="2020-09-14T10:48:45.813+09:00", comments="Source Table: t_storage_recovery")
    @SelectProvider(type=SqlProviderAdapter.class, method="select")
    @Results(id="TStorageRecoveryResult", value = {
        @Result(column="id", property="id", jdbcType=JdbcType.INTEGER, id=true),
        @Result(column="auth_id", property="authId", jdbcType=JdbcType.VARCHAR),
        @Result(column="shared_clue", property="sharedClue", jdbcType=JdbcType.VARCHAR),
        @Result(column="created", property="created", jdbcType=JdbcType.TIMESTAMP)
    })
    List<TStorageRecovery> selectMany(SelectStatementProvider selectStatement);

    @Generated(value="org.mybatis.generator.api.MyBatisGenerator", date="2020-09-14T10:48:45.813+09:00", comments="Source Table: t_storage_recovery")
    @UpdateProvider(type=SqlProviderAdapter.class, method="update")
    int update(UpdateStatementProvider updateStatement);

    @Generated(value="org.mybatis.generator.api.MyBatisGenerator", date="2020-09-14T10:48:45.813+09:00", comments="Source Table: t_storage_recovery")
    default long count(CountDSLCompleter completer) {
        return MyBatis3Utils.countFrom(this::count, TStorageRecovery, completer);
    }

    @Generated(value="org.mybatis.generator.api.MyBatisGenerator", date="2020-09-14T10:48:45.813+09:00", comments="Source Table: t_storage_recovery")
    default int delete(DeleteDSLCompleter completer) {
        return MyBatis3Utils.deleteFrom(this::delete, TStorageRecovery, completer);
    }

    @Generated(value="org.mybatis.generator.api.MyBatisGenerator", date="2020-09-14T10:48:45.813+09:00", comments="Source Table: t_storage_recovery")
    default int deleteByPrimaryKey(Integer id_) {
        return delete(c -> 
            c.where(id, isEqualTo(id_))
        );
    }

    @Generated(value="org.mybatis.generator.api.MyBatisGenerator", date="2020-09-14T10:48:45.814+09:00", comments="Source Table: t_storage_recovery")
    default int insert(TStorageRecovery record) {
        return MyBatis3Utils.insert(this::insert, record, TStorageRecovery, c ->
            c.map(authId).toProperty("authId")
            .map(sharedClue).toProperty("sharedClue")
            .map(created).toProperty("created")
        );
    }

    @Generated(value="org.mybatis.generator.api.MyBatisGenerator", date="2020-09-14T10:48:45.814+09:00", comments="Source Table: t_storage_recovery")
    default int insertMultiple(Collection<TStorageRecovery> records) {
        return MyBatis3Utils.insertMultiple(this::insertMultiple, records, TStorageRecovery, c ->
            c.map(authId).toProperty("authId")
            .map(sharedClue).toProperty("sharedClue")
            .map(created).toProperty("created")
        );
    }

    @Generated(value="org.mybatis.generator.api.MyBatisGenerator", date="2020-09-14T10:48:45.814+09:00", comments="Source Table: t_storage_recovery")
    default int insertSelective(TStorageRecovery record) {
        return MyBatis3Utils.insert(this::insert, record, TStorageRecovery, c ->
            c.map(authId).toPropertyWhenPresent("authId", record::getAuthId)
            .map(sharedClue).toPropertyWhenPresent("sharedClue", record::getSharedClue)
            .map(created).toPropertyWhenPresent("created", record::getCreated)
        );
    }

    @Generated(value="org.mybatis.generator.api.MyBatisGenerator", date="2020-09-14T10:48:45.814+09:00", comments="Source Table: t_storage_recovery")
    default Optional<TStorageRecovery> selectOne(SelectDSLCompleter completer) {
        return MyBatis3Utils.selectOne(this::selectOne, selectList, TStorageRecovery, completer);
    }

    @Generated(value="org.mybatis.generator.api.MyBatisGenerator", date="2020-09-14T10:48:45.814+09:00", comments="Source Table: t_storage_recovery")
    default List<TStorageRecovery> select(SelectDSLCompleter completer) {
        return MyBatis3Utils.selectList(this::selectMany, selectList, TStorageRecovery, completer);
    }

    @Generated(value="org.mybatis.generator.api.MyBatisGenerator", date="2020-09-14T10:48:45.814+09:00", comments="Source Table: t_storage_recovery")
    default List<TStorageRecovery> selectDistinct(SelectDSLCompleter completer) {
        return MyBatis3Utils.selectDistinct(this::selectMany, selectList, TStorageRecovery, completer);
    }

    @Generated(value="org.mybatis.generator.api.MyBatisGenerator", date="2020-09-14T10:48:45.814+09:00", comments="Source Table: t_storage_recovery")
    default Optional<TStorageRecovery> selectByPrimaryKey(Integer id_) {
        return selectOne(c ->
            c.where(id, isEqualTo(id_))
        );
    }

    @Generated(value="org.mybatis.generator.api.MyBatisGenerator", date="2020-09-14T10:48:45.814+09:00", comments="Source Table: t_storage_recovery")
    default int update(UpdateDSLCompleter completer) {
        return MyBatis3Utils.update(this::update, TStorageRecovery, completer);
    }

    @Generated(value="org.mybatis.generator.api.MyBatisGenerator", date="2020-09-14T10:48:45.814+09:00", comments="Source Table: t_storage_recovery")
    static UpdateDSL<UpdateModel> updateAllColumns(TStorageRecovery record, UpdateDSL<UpdateModel> dsl) {
        return dsl.set(authId).equalTo(record::getAuthId)
                .set(sharedClue).equalTo(record::getSharedClue)
                .set(created).equalTo(record::getCreated);
    }

    @Generated(value="org.mybatis.generator.api.MyBatisGenerator", date="2020-09-14T10:48:45.814+09:00", comments="Source Table: t_storage_recovery")
    static UpdateDSL<UpdateModel> updateSelectiveColumns(TStorageRecovery record, UpdateDSL<UpdateModel> dsl) {
        return dsl.set(authId).equalToWhenPresent(record::getAuthId)
                .set(sharedClue).equalToWhenPresent(record::getSharedClue)
                .set(created).equalToWhenPresent(record::getCreated);
    }

    @Generated(value="org.mybatis.generator.api.MyBatisGenerator", date="2020-09-14T10:48:45.814+09:00", comments="Source Table: t_storage_recovery")
    default int updateByPrimaryKey(TStorageRecovery record) {
        return update(c ->
            c.set(authId).equalTo(record::getAuthId)
            .set(sharedClue).equalTo(record::getSharedClue)
            .set(created).equalTo(record::getCreated)
            .where(id, isEqualTo(record::getId))
        );
    }

    @Generated(value="org.mybatis.generator.api.MyBatisGenerator", date="2020-09-14T10:48:45.814+09:00", comments="Source Table: t_storage_recovery")
    default int updateByPrimaryKeySelective(TStorageRecovery record) {
        return update(c ->
            c.set(authId).equalToWhenPresent(record::getAuthId)
            .set(sharedClue).equalToWhenPresent(record::getSharedClue)
            .set(created).equalToWhenPresent(record::getCreated)
            .where(id, isEqualTo(record::getId))
        );
    }
}