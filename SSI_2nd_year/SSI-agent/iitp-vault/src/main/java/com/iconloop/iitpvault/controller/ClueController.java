package com.iconloop.iitpvault.controller;

import com.iconloop.iitpvault.service.VaultService;
import com.iconloop.iitpvault.vo.ResponseVo;
import com.iconloop.iitpvault.vo.dto.RestoreReq;
import com.iconloop.iitpvault.vo.dto.SaveClueReq;
import com.iconloop.iitpvault.vo.dto.SaveRecoveryReq;
import com.iconloop.iitpvault.vo.enumType.RestoreType;
import io.swagger.annotations.ApiOperation;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Profile;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Profile({"clue","clue2"})
@RestController
@Slf4j
@RequiredArgsConstructor
@RequestMapping("v1/vault")
public class ClueController {

    private final VaultService vaultService;

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

        return ResponseEntity.ok().body(vaultService.restore(req.getAuthToken(), RestoreType.CLUE));
    }
}
