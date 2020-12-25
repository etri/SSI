package com.iitp.iitp_demo.chain.indy;

import com.iitp.iitp_demo.util.PrintLog;

import org.apache.commons.io.FileUtils;
import org.hyperledger.indy.sdk.IndyException;
import org.hyperledger.indy.sdk.ledger.Ledger;
import org.hyperledger.indy.sdk.pool.Pool;
import org.json.JSONException;

import java.util.concurrent.ExecutionException;

public class EnvironmentUtils{
    static String getTestPoolIP() {
        String testPoolIp = System.getenv("TEST_POOL_IP");
        return testPoolIp != null ? testPoolIp : "129.254.194.212";
//                return testPoolIp != null ? testPoolIp : "143.248.137.177";
    }

    public static String getTmpPath() {
        return FileUtils.getTempDirectoryPath() + "/indy/";
    }

    public static String getTmpPath(String filename) {

        return getTmpPath() + filename;
    }
}
