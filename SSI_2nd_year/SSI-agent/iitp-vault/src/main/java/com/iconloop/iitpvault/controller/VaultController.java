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


@Profile("local")
@RestController
@Slf4j
@RequiredArgsConstructor
@RequestMapping("v1/vault")
public class VaultController {

    private final VaultService vaultService;

    @Value("${spring.profiles.active}")
    private String activeProfile;

    @ApiOperation(value = "복구 키 저장(Master)", response = ResponseVo.class)
    @PostMapping("master")
    public ResponseEntity saveMaster(@RequestBody SaveRecoveryReq req) {
        log.info("===========================");
        log.info("backup master : {}", req);
        return ResponseEntity.ok().body(vaultService.saveMaster(req));
    }

    @ApiOperation(value = "복구 키 저장(clue)", response = ResponseVo.class)
    @PostMapping("clue")
    public ResponseEntity saveClue(@RequestBody SaveClueReq req) {
        log.info("===========================");
        log.info("backup clue : {}", req);
        return ResponseEntity.ok().body(vaultService.saveClue(req));
    }

    @ApiOperation(value = "키 복구", response = ResponseVo.class)
    @PostMapping("/restore")
    public ResponseEntity load(@RequestBody RestoreReq req) {
        log.info("===========================");
        log.info("restore : {}", req);
        RestoreType type;
        if(activeProfile.equals("master")) {
            type = RestoreType.MASTER;
        } else if(activeProfile.equals("clue")) {
            type = RestoreType.CLUE;
        } else {
            type = RestoreType.CLUE;
        }

        return ResponseEntity.ok().body(vaultService.restore(req.getAuthToken(), type));
    }
}
