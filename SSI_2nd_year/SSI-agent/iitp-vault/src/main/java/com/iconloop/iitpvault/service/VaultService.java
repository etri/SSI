package com.iconloop.iitpvault.service;

import com.iconloop.iitpvault.domain.dao.TMasterStorage;
import com.iconloop.iitpvault.domain.dao.TStorageRecovery;
import com.iconloop.iitpvault.domain.dao.TStorageSecret;
import com.iconloop.iitpvault.domain.mappers.*;
import com.iconloop.iitpvault.exception.AuthException;
import com.iconloop.iitpvault.exception.error.VaultErrorCode;
import com.iconloop.iitpvault.utils.AuthTokenUtil;
import com.iconloop.iitpvault.vo.ErrorResultVo;
import com.iconloop.iitpvault.vo.ResponseVo;
import com.iconloop.iitpvault.vo.dto.*;
import com.iconloop.iitpvault.vo.enumType.RestoreType;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.List;

import static org.mybatis.dynamic.sql.SqlBuilder.isEqualTo;

@Service
@Slf4j
@RequiredArgsConstructor
public class VaultService {
    private final AuthTokenUtil authTokenUtil;
    private final TMasterStorageMapper tMasterStorageMapper;
    private final TStorageRecoveryMapper tStorageRecoveryMapper;
    private final TStorageSecretMapper tStorageSecretMapper;

    public ResponseVo saveMaster(SaveRecoveryReq req) {
        try {
            String authId = authTokenUtil.getAuthId(req.getAuthToken());
            clearOldMaster(authId);
            TMasterStorage storage = new TMasterStorage();
            storage.setDid(authId);
            storage.setRecoveryKey(req.getRecoveryKey());
            tMasterStorageMapper.insertSelective(storage);
            return ResponseVo.builder().success(true).build();
        } catch (AuthException e) {
            log.error("{} : {}", e.getCode(), e.getMessage());
            return ResponseVo.builder()
                    .success(false)
                    .result(
                            ErrorResultVo.builder()
                                    .code(e.getCode())
                                    .message(e.getMessage())
                                    .build())
                    .build();
        }
    }

    public ResponseVo saveClue(SaveClueReq req) {
        try {
            String authId = authTokenUtil.getAuthId(req.getAuthToken());
            clearOldClue(authId);

            TStorageRecovery recovery = new TStorageRecovery();
            recovery.setAuthId(authId);
            recovery.setSharedClue(req.getRecoveryClue());
            tStorageRecoveryMapper.insertSelective(recovery);

            TStorageSecret secret = new TStorageSecret();
            secret.setAuthId(authId);
            secret.setSharedClue(req.getDataClue());
            tStorageSecretMapper.insertSelective(secret);

            return ResponseVo.builder().success(true).build();
        } catch (AuthException e) {
            log.error("{} : {}", e.getCode(), e.getMessage());
            return ResponseVo.builder()
                    .success(false)
                    .result(
                            ErrorResultVo.builder()
                                    .code(e.getCode())
                                    .message(e.getMessage())
                                    .build())
                    .build();
        }
    }

    public ResponseVo restore(String authToken, RestoreType type) {
        try {
            ResponseVo resVo = ResponseVo.builder().build();
            String authId = authTokenUtil.getAuthId(authToken);
            if(type.equals(RestoreType.MASTER)) {
                TMasterStorage storage = getMasterRecovery(authId);
                if(storage != null) {
                    MasterRecoveryRes res = new MasterRecoveryRes();
                    res.setRecoveryKey(storage.getRecoveryKey());
                    resVo.setSuccess(true);
                    resVo.setResult(res);
                } else {
                    ErrorResultVo vo = ErrorResultVo.builder()
                            .code(VaultErrorCode.NOT_EXIST_RECOVERY_DATA.getCode())
                            .message(VaultErrorCode.NOT_EXIST_RECOVERY_DATA.getMessage())
                            .build();
                    resVo.setSuccess(false);
                    resVo.setResult(vo);
                }
            } else if(type.equals(RestoreType.CLUE)) {
                TStorageRecovery recovery = getRecoveryByAuthId(authId);
                TStorageSecret secret = getClueByAuthId(authId);
                RecoveryRes res = new RecoveryRes();
                if(secret == null && recovery == null) {
                    ErrorResultVo vo = ErrorResultVo.builder()
                            .code(VaultErrorCode.NOT_EXIST_RECOVERY_DATA.getCode())
                            .message(VaultErrorCode.NOT_EXIST_RECOVERY_DATA.getMessage())
                            .build();
                    return ResponseVo.builder().success(false).result(vo).build();
                }
                if(recovery != null) {
                    res.setRecoveryClue(recovery.getSharedClue());
                }
                if(secret != null) {
                    res.setDataClue(secret.getSharedClue());
                }
                resVo.setSuccess(true);
                resVo.setResult(res);
            } else {
                return ResponseVo.builder()
                        .success(false)
                        .result(
                                ErrorResultVo.builder()
                                        .code(VaultErrorCode.UNKNOWN_RESTORE_TYPE.getCode())
                                        .message(VaultErrorCode.UNKNOWN_RESTORE_TYPE.getMessage())
                                        .build())
                        .build();
            }

            return resVo;

        } catch (AuthException e) {
            log.error("{} : {}", e.getCode(), e.getMessage());
            return ResponseVo.builder()
                    .success(false)
                    .result(
                            ErrorResultVo.builder()
                                    .code(e.getCode())
                                    .message(e.getMessage())
                                    .build())
                    .build();
        }
    }

    private void clearOldMaster(String authId) {
        tMasterStorageMapper.delete(c->c.where(TMasterStorageDynamicSqlSupport.did, isEqualTo(authId)));
    }

    private void clearOldClue(String authId) {
        tStorageRecoveryMapper.delete(c->c.where(TStorageRecoveryDynamicSqlSupport.authId, isEqualTo(authId)));
        tStorageSecretMapper.delete(c->c.where(TStorageSecretDynamicSqlSupport.authId, isEqualTo(authId)));
    }

    private TMasterStorage getMasterRecovery(String authId) {
        return tMasterStorageMapper.selectOne(c->c.where(TMasterStorageDynamicSqlSupport.did, isEqualTo(authId))).orElse(null);
    }

    private TStorageRecovery getRecoveryByAuthId(String authId) {
        return tStorageRecoveryMapper.selectOne(c->c.where(TStorageRecoveryDynamicSqlSupport.authId, isEqualTo(authId))).orElse(null);
    }

    private TStorageSecret getClueByAuthId(String authId) {
        return tStorageSecretMapper.selectOne(c->c.where(TStorageSecretDynamicSqlSupport.authId, isEqualTo(authId))).orElse(null);
    }
}
