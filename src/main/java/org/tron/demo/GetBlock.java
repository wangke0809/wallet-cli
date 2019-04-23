package org.tron.demo;

import org.tron.api.GrpcAPI;
import org.tron.common.crypto.Sha256Hash;
import org.tron.common.utils.ByteArray;
import org.tron.keystore.Wallet;
import org.tron.protos.Contract;
import org.tron.protos.Protocol;
import org.tron.walletserver.WalletApi;
import org.tron.protos.Protocol.Block;

import java.util.List;
import java.util.Optional;

public class GetBlock {
    public static void main(String[] args) throws Exception {
        Optional<GrpcAPI.BlockList> p = WalletApi.getBlockByLatestNum(1);
        if(p.isPresent()){
            p.get().getBlockCount();
            System.out.println(p.get().getBlock(0).getBlockHeader().getRawData().getNumber());
        }
//        return;
        System.out.println("----------------------------------");
        Block block = WalletApi.getBlock(3260365);
        System.out.println("block:" + block);
        block.getBlockHeader().getRawData().getTimestamp();
        long num = WalletApi.getTransactionCountByBlockNum(3260365);
        System.out.println("num: " + num);
        System.out.println("count:" + block.getTransactionsCount());
        List<Protocol.Transaction> txs = block.getTransactionsList();
        for (Protocol.Transaction t : txs) {
            System.out.println("=========");
            System.out.println(t);
            // 获取交易hash
            byte[] hash = Sha256Hash.hash(t.getRawData().toByteArray());
            System.out.println("txhash: " + ByteArray.toHexString(hash));

            System.out.println("fields");
            System.out.println(t.getUnknownFields());

            System.out.println("ContractList: " + t.getRawData().getContractList());
            System.out.println("ContractList: " + t.getRawData().getContractList().get(0).getParameter().getTypeUrl());
            System.out.println("ContractList: " + t.getRawData().getContractList().get(0).getParameter().getValue().toStringUtf8());
        }
//        System.out.println(WalletApi.getBlock2(3267826));
        System.out.println("====txinfo====");
        // e6f1c4218172737b8ad6f3af1aae5812d125776d6d7e121afc7e5c9e8079bea5
        Optional<Protocol.TransactionInfo>  txInfo = WalletApi.getTransactionInfoById("773ef7fbf4f8bbea528dbf3c3e8ee1c96bf4ff1fec9ef023006f45a22d576152");
        if(txInfo.isPresent()){
            System.out.println("hash: " + ByteArray.toHexString(txInfo.get().getId().toByteArray()));
            System.out.println("hash: " + txInfo.get().getId().toString());
            System.out.println(txInfo.get());
            Protocol.TransactionInfo t = txInfo.get();
            System.out.println(t.getInternalTransactionsCount());
            List<Protocol.InternalTransaction> l = t.getInternalTransactionsList();
            System.out.println("===InternalTransaction=====");
            for (Protocol.InternalTransaction it : l){
                System.out.println(it.getCallerAddress().toByteArray());
                System.out.println(it.getTransferToAddress().toByteArray());

            }
            System.out.println();
        }

        System.out.println("====tx====");
        Optional<Protocol.Transaction> tx = WalletApi.getTransactionById("773ef7fbf4f8bbea528dbf3c3e8ee1c96bf4ff1fec9ef023006f45a22d576152");
        if (tx.isPresent()){
            Protocol.Transaction t = tx.get();
            System.out.println(tx.get());
            System.out.println("ContractList: " + t.getRawData().getContractList());
            System.out.println("ContractList: " + t.getRawData().getContractList().get(0).getParameter().getTypeUrl());
            System.out.println("ContractList: " + t.getRawData().getContractList().get(0).getParameter().getValue().toStringUtf8());
            long v = Contract.TransferContract.parseFrom(t.getRawData().getContractList().get(0).getParameter().getValue().toByteArray()).getAmount();
            System.out.println(v); // 1000000
        }
    }
}
