package com.iconloop.iitpvault.domain.mappers;

import static com.iconloop.iitpvault.domain.mappers.TStorageSecretDynamicSqlSupport.*;
import static org.mybatis.dynamic.sql.SqlBuilder.*;

import com.iconloop.iitpvault.domain.dao.TStorageSecret;
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
public interface TStorageSecretMapper {
    @Generated(value="org.mybatis.generator.api.MyBatisGenerator", date="2020-09-14T10:48:45.817+09:00", comments="Source Table: t_storage_secret")
    BasicColumn[] selectList = BasicColumn.columnList(id, authId, sharedClue, created);

    @Generated(value="org.mybatis.generator.api.MyBatisGenerator", date="2020-09-14T10:48:45.816+09:00", comments="Source Table: t_storage_secret")
    @SelectProvider(type=SqlProviderAdapter.class, method="select")
    long count(SelectStatementProvider selectStatement);

    @Generated(value="org.mybatis.generator.api.MyBatisGenerator", date="2020-09-14T10:48:45.816+09:00", comments="Source Table: t_storage_secret")
    @DeleteProvider(type=SqlProviderAdapter.class, method="delete")
    int delete(DeleteStatementProvider deleteStatement);

    @Generated(value="org.mybatis.generator.api.MyBatisGenerator", date="2020-09-14T10:48:45.816+09:00", comments="Source Table: t_storage_secret")
    @InsertProvider(type=SqlProviderAdapter.class, method="insert")
    @Options(useGeneratedKeys=true,keyProperty="record.id")
    int insert(InsertStatementProvider<TStorageSecret> insertStatement);

    @Generated(value="org.mybatis.generator.api.MyBatisGenerator", date="2020-09-14T10:48:45.816+09:00", comments="Source Table: t_storage_secret")
    @Insert({
        "${insertStatement}"
    })
    @Options(useGeneratedKeys=true,keyProperty="records.id")
    int insertMultiple(@Param("insertStatement") String insertStatement, @Param("records") List<TStorageSecret> records);

    @Generated(value="org.mybatis.generator.api.MyBatisGenerator", date="2020-09-14T10:48:45.817+09:00", comments="Source Table: t_storage_secret")
    default int insertMultiple(MultiRowInsertStatementProvider<TStorageSecret> multipleInsertStatement) {
        return insertMultiple(multipleInsertStatement.getInsertStatement(), multipleInsertStatement.getRecords());
    }

    @Generated(value="org.mybatis.generator.api.MyBatisGenerator", date="2020-09-14T10:48:45.817+09:00", comments="Source Table: t_storage_secret")
    @SelectProvider(type=SqlProviderAdapter.class, method="select")
    @ResultMap("TStorageSecretResult")
    Optional<TStorageSecret> selectOne(SelectStatementProvider selectStatement);

    @Generated(value="org.mybatis.generator.api.MyBatisGenerator", date="2020-09-14T10:48:45.817+09:00", comments="Source Table: t_storage_secret")
    @SelectProvider(type=SqlProviderAdapter.class, method="select")
    @Results(id="TStorageSecretResult", value = {
        @Result(column="id", property="id", jdbcType=JdbcType.INTEGER, id=true),
        @Result(column="auth_id", property="authId", jdbcType=JdbcType.VARCHAR),
        @Result(column="shared_clue", property="sharedClue", jdbcType=JdbcType.VARCHAR),
        @Result(column="created", property="created", jdbcType=JdbcType.TIMESTAMP)
    })
    List<TStorageSecret> selectMany(SelectStatementProvider selectStatement);

    @Generated(value="org.mybatis.generator.api.MyBatisGenerator", date="2020-09-14T10:48:45.817+09:00", comments="Source Table: t_storage_secret")
    @UpdateProvider(type=SqlProviderAdapter.class, method="update")
    int update(UpdateStatementProvider updateStatement);

    @Generated(value="org.mybatis.generator.api.MyBatisGenerator", date="2020-09-14T10:48:45.817+09:00", comments="Source Table: t_storage_secret")
    default long count(CountDSLCompleter completer) {
        return MyBatis3Utils.countFrom(this::count, TStorageSecret, completer);
    }

    @Generated(value="org.mybatis.generator.api.MyBatisGenerator", date="2020-09-14T10:48:45.817+09:00", comments="Source Table: t_storage_secret")
    default int delete(DeleteDSLCompleter completer) {
        return MyBatis3Utils.deleteFrom(this::delete, TStorageSecret, completer);
    }

    @Generated(value="org.mybatis.generator.api.MyBatisGenerator", date="2020-09-14T10:48:45.817+09:00", comments="Source Table: t_storage_secret")
    default int deleteByPrimaryKey(Integer id_) {
        return delete(c -> 
            c.where(id, isEqualTo(id_))
        );
    }

    @Generated(value="org.mybatis.generator.api.MyBatisGenerator", date="2020-09-14T10:48:45.817+09:00", comments="Source Table: t_storage_secret")
    default int insert(TStorageSecret record) {
        return MyBatis3Utils.insert(this::insert, record, TStorageSecret, c ->
            c.map(authId).toProperty("authId")
            .map(sharedClue).toProperty("sharedClue")
            .map(created).toProperty("created")
        );
    }

    @Generated(value="org.mybatis.generator.api.MyBatisGenerator", date="2020-09-14T10:48:45.817+09:00", comments="Source Table: t_storage_secret")
    default int insertMultiple(Collection<TStorageSecret> records) {
        return MyBatis3Utils.insertMultiple(this::insertMultiple, records, TStorageSecret, c ->
            c.map(authId).toProperty("authId")
            .map(sharedClue).toProperty("sharedClue")
            .map(created).toProperty("created")
        );
    }

    @Generated(value="org.mybatis.generator.api.MyBatisGenerator", date="2020-09-14T10:48:45.817+09:00", comments="Source Table: t_storage_secret")
    default int insertSelective(TStorageSecret record) {
        return MyBatis3Utils.insert(this::insert, record, TStorageSecret, c ->
            c.map(authId).toPropertyWhenPresent("authId", record::getAuthId)
            .map(sharedClue).toPropertyWhenPresent("sharedClue", record::getSharedClue)
            .map(created).toPropertyWhenPresent("created", record::getCreated)
        );
    }

    @Generated(value="org.mybatis.generator.api.MyBatisGenerator", date="2020-09-14T10:48:45.817+09:00", comments="Source Table: t_storage_secret")
    default Optional<TStorageSecret> selectOne(SelectDSLCompleter completer) {
        return MyBatis3Utils.selectOne(this::selectOne, selectList, TStorageSecret, completer);
    }

    @Generated(value="org.mybatis.generator.api.MyBatisGenerator", date="2020-09-14T10:48:45.817+09:00", comments="Source Table: t_storage_secret")
    default List<TStorageSecret> select(SelectDSLCompleter completer) {
        return MyBatis3Utils.selectList(this::selectMany, selectList, TStorageSecret, completer);
    }

    @Generated(value="org.mybatis.generator.api.MyBatisGenerator", date="2020-09-14T10:48:45.818+09:00", comments="Source Table: t_storage_secret")
    default List<TStorageSecret> selectDistinct(SelectDSLCompleter completer) {
        return MyBatis3Utils.selectDistinct(this::selectMany, selectList, TStorageSecret, completer);
    }

    @Generated(value="org.mybatis.generator.api.MyBatisGenerator", date="2020-09-14T10:48:45.818+09:00", comments="Source Table: t_storage_secret")
    default Optional<TStorageSecret> selectByPrimaryKey(Integer id_) {
        return selectOne(c ->
            c.where(id, isEqualTo(id_))
        );
    }

    @Generated(value="org.mybatis.generator.api.MyBatisGenerator", date="2020-09-14T10:48:45.818+09:00", comments="Source Table: t_storage_secret")
    default int update(UpdateDSLCompleter completer) {
        return MyBatis3Utils.update(this::update, TStorageSecret, completer);
    }

    @Generated(value="org.mybatis.generator.api.MyBatisGenerator", date="2020-09-14T10:48:45.818+09:00", comments="Source Table: t_storage_secret")
    static UpdateDSL<UpdateModel> updateAllColumns(TStorageSecret record, UpdateDSL<UpdateModel> dsl) {
        return dsl.set(authId).equalTo(record::getAuthId)
                .set(sharedClue).equalTo(record::getSharedClue)
                .set(created).equalTo(record::getCreated);
    }

    @Generated(value="org.mybatis.generator.api.MyBatisGenerator", date="2020-09-14T10:48:45.818+09:00", comments="Source Table: t_storage_secret")
    static UpdateDSL<UpdateModel> updateSelectiveColumns(TStorageSecret record, UpdateDSL<UpdateModel> dsl) {
        return dsl.set(authId).equalToWhenPresent(record::getAuthId)
                .set(sharedClue).equalToWhenPresent(record::getSharedClue)
                .set(created).equalToWhenPresent(record::getCreated);
    }

    @Generated(value="org.mybatis.generator.api.MyBatisGenerator", date="2020-09-14T10:48:45.818+09:00", comments="Source Table: t_storage_secret")
    default int updateByPrimaryKey(TStorageSecret record) {
        return update(c ->
            c.set(authId).equalTo(record::getAuthId)
            .set(sharedClue).equalTo(record::getSharedClue)
            .set(created).equalTo(record::getCreated)
            .where(id, isEqualTo(record::getId))
        );
    }

    @Generated(value="org.mybatis.generator.api.MyBatisGenerator", date="2020-09-14T10:48:45.818+09:00", comments="Source Table: t_storage_secret")
    default int updateByPrimaryKeySelective(TStorageSecret record) {
        return update(c ->
            c.set(authId).equalToWhenPresent(record::getAuthId)
            .set(sharedClue).equalToWhenPresent(record::getSharedClue)
            .set(created).equalToWhenPresent(record::getCreated)
            .where(id, isEqualTo(record::getId))
        );
    }
}