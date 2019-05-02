package org.tron.demo;

import org.tron.api.GrpcAPI;
import org.tron.keystore.Wallet;
import org.tron.protos.Protocol;
import org.tron.walletserver.WalletApi;

import java.math.BigDecimal;

/**
 * @author wangke
 * @description: TODO
 * @date 2019-04-23 15:43
 */
public class getBalance {
    public static void main(String[] args) {
        WalletApi.setGrpcClient("grpc.shasta.trongrid.io:50051", "grpc.shasta.trongrid.io:50052", true, 2);
        byte[] to = WalletApi.decodeFromBase58Check("TJt33B4TmGVYWxwNSRWtRiq3gZdiw4Dyx9");
        try{
            GrpcAPI.AccountResourceMessage m = WalletApi.getAccountResource(to);
            GrpcAPI.AccountNetMessage m2 = WalletApi.getAccountNet(to);
            Protocol.Account a = WalletApi.queryAccount(to);
            BigDecimal b = BigDecimal.valueOf(a.getBalance());
            System.out.println(m);
            System.out.println("m2：" + m2);
            System.out.println(b);
        }catch (Exception e){
            System.out.println(e);
        }


    }
}
