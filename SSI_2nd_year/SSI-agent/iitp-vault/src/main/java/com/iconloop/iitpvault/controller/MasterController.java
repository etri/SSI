package com.iconloop.iitpvault.controller;

import com.iconloop.iitpvault.exception.AuthException;
import com.iconloop.iitpvault.service.AuthService;
import com.iconloop.iitpvault.service.VaultService;
import com.iconloop.iitpvault.utils.VerifyTokenUtil;
import com.iconloop.iitpvault.vo.ErrorResultVo;
import com.iconloop.iitpvault.vo.ResponseVo;
import com.iconloop.iitpvault.vo.dto.*;
import com.iconloop.iitpvault.vo.enumType.RestoreType;
import io.swagger.annotations.ApiOperation;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Profile;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.PostConstruct;

@Profile("master")
@RestController
@Slf4j
@RequiredArgsConstructor
@RequestMapping("v1/vault")
public class MasterController {

    private final VaultService vaultService;

    @ApiOperation(value = "복구 키 저장(Master)", response = ResponseVo.class)
    @PostMapping("master")
    public ResponseEntity saveMaster(@RequestBody SaveRecoveryReq req) {
        log.info("===========================");
        log.info("backup master : {}", req);
        return ResponseEntity.ok().body(vaultService.saveMaster(req));
    }

    @ApiOperation(value = "키 복구", response = ResponseVo.class)
    @PostMapping("/restore")
    public ResponseEntity load(@RequestBody RestoreReq req) {
        log.info("===========================");
        log.info("restore : {}", req);

        return ResponseEntity.ok().body(vaultService.restore(req.getAuthToken(), RestoreType.MASTER));
    }
}
