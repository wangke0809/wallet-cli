package org.tron.demo;

import org.tron.common.crypto.Sha256Hash;
import org.tron.common.utils.ByteArray;
import org.tron.protos.Protocol;
import org.tron.walletserver.WalletApi;

import java.util.Date;
import java.util.List;

/**
 * @author wangke
 * @description: TODO
 * @date 2019-04-23 16:52
 */
public class getBlock2 {
    public static void main(String[] args) {
        WalletApi.setGrpcClient("grpc.trongrid.io:50051", "grpc.trongrid.io:50052", true, 2);
        while (true){
            Protocol.Block block = WalletApi.getBlock(	8860264);
            System.out.println("block:" + block);
            long timeStamp = block.getBlockHeader().getRawData().getTimestamp();
            Date date = new Date(timeStamp);
            System.out.println(date);
//        long num = WalletApi.getTransactionCountByBlockNum(3260365);
//        System.out.println("num: " + num);
            System.out.println("count:" + block.getTransactionsCount());
        }

        /**
        List<Protocol.Transaction> txs = block.getTransactionsList();
        for (Protocol.Transaction t : txs) {
            System.out.println("=========");
            System.out.println(t);
            // 获取交易hash
            byte[] hash = Sha256Hash.hash(t.getRawData().toByteArray());
            String hash2 = ByteArray.toHexString(hash);
            System.out.println("txhash: " + hash2);

            // trc10: TransferAssetContract
            if(t.getRawData().getContractCount()>0){
                String txType = t.getRawData().getContractList().get(0).getParameter().getTypeUrl();
                System.out.println("type: " + txType);
            }

//            System.out.println("ContractList: " + t.getRawData().getContractList());
            System.out.println("ContractList: " + t.getRawData().getContractList().get(0).getParameter().getTypeUrl());
            System.out.println("ContractList: " + t.getRawData().getContractList().get(0).getParameter().getValue().toStringUtf8());
        }
         */
    }
}
