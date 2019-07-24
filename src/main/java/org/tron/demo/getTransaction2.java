package org.tron.demo;

import org.springframework.util.StringUtils;
import org.tron.common.crypto.ECKey;
import org.tron.common.crypto.Sha256Hash;
import org.tron.common.utils.ByteArray;
import org.tron.keystore.Wallet;
import org.tron.protos.Contract;
import org.tron.protos.Protocol;
import org.tron.walletserver.WalletApi;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.util.Arrays;
import java.util.Optional;

/**
 * @author wangke
 * @description: TODO
 * @date 2019-04-23 16:53
 */
public class getTransaction2 {


    public static void main(String[] args) throws Exception {
//        WalletApi.setGrpcClient("grpc.shasta.trongrid.io:50051", "grpc.shasta.trongrid.io:50052", true, 2);
        WalletApi.setGrpcClient("grpc.trongrid.io:50051", "grpc.trongrid.io:50052", true, 2);


        // transfer success 92f3e06f035fc069df39e329232ba6ac722c127d96cdfbb39922f0649d6b6807
        // transfer failure 10e2efbe28967a9263e347a1991c4cfb1985e4b9edf71ee290526ae701c16dba
        // mainnet success f777a54264534692f6de607335852784bd686308ff3048ef8355fe48a6d59f09
        // trc20 fail 47f9da3f98a3181f385469ec649b1df1d00592887613dadd76546dfe5fb3dc88
        // trc20 6cf3638d888bff35e388a1899537a54157e2785cd45771e72466982820c455a7
        // create account d019d2b9cbf416307ee8dc822a9da9b5f06489cd5a737a907211d6928a38a8b7
        // trc20 to 未激活地址 6ff195dd300c553a570b89833195d552680930034bb8d0136b0f4ecd66753a67
        // out of enegry 激活地址，没有trx，trc20 bb7ecbb9c3ce4da7066b6bec9eca636d5c0e065c6d8045727b226cbdeb47ae06
        // 充值 trx 后 trc20 4fcfb8185d6aae3770caa21d045782c1bb0c615ffdd5b7cf3e06c7db0fb2d7b5
        String txHash = "4f3f78de1cf89a2a14d7c1694a706e5bb61f5dd778e842ba3fd2d0ada0ba1677";
        Optional<Protocol.Transaction> tx = WalletApi.getTransactionById(txHash);
        if (tx.isPresent()) {
            Protocol.Transaction t = tx.get();
            System.out.println("tx:");
            System.out.println(tx.get());
            Contract.TriggerSmartContract triggerSmartContract = Contract.TriggerSmartContract.parseFrom(tx.get().getRawData().getContract(0).getParameter().getValue().toByteArray());
            System.out.println("tri:" + triggerSmartContract);
            String value = ByteArray.toHexString(triggerSmartContract.getData().toByteArray());
            if(value.substring(0, 8).equals("a9059cbb")){
                System.out.println("transfer");
            }
            String toAddress1 = value.substring(32, 72);
            System.out.println(toAddress1);
            String token_value =value.substring(value.length()-64,value.length());
            System.out.println("toAddress: " + getAddressFromHash(ByteArray.fromHexString(toAddress1)));
            System.out.println("tokenValue: " + ByteArray.toLong(ByteArray.fromHexString(token_value)));
            System.out.println("datavalue: " + ByteArray.toHexString(triggerSmartContract.getData().toByteArray()));
            System.out.println("fromaddress: " + WalletApi.encode58Check(triggerSmartContract.getOwnerAddress().toByteArray()));
            System.out.println("tokenAddress: " + WalletApi.encode58Check(triggerSmartContract.getContractAddress().toByteArray()));

            System.out.println(ByteArray.toHexString(tx.get().getRawData().getRefBlockBytes().toByteArray()));
            System.out.println(ByteArray.toHexString(tx.get().getRawData().getRefBlockHash().toByteArray()));
            System.out.println("ContractList: " + t.getRawData().getContractList());
            System.out.println("ContractList: " + t.getRawData().getContractList().get(0).getParameter().getTypeUrl());
            System.out.println("type: " + t.getRawData().getContractList().get(0).getType().toString());
            System.out.println("ContractList: " + t.getRawData().getContractList().get(0).getParameter().getValue().toStringUtf8());
            long v = Contract.TransferContract.parseFrom(t.getRawData().getContractList().get(0).getParameter().getValue().toByteArray()).getAmount();
            Contract.TransferContract contract = Contract.TransferContract.parseFrom(t.getRawData().getContractList().get(0).getParameter().getValue().toByteArray());
            byte[] toAddress = contract.getToAddress().toByteArray();
            System.out.println(toAddress.length);
            System.out.println(ByteArray.toHexString(toAddress));
            System.out.println("c: " + WalletApi.encode58Check(toAddress));
            byte[] hash = Sha256Hash.hash(t.getRawData().toByteArray());
            System.out.println(t.getSignature(0).toByteArray().length);
            System.out.println("hex: " + ByteArray.toHexString(t.getSignature(0).toByteArray()));
            byte[] sig = t.getSignature(0).toByteArray();
            byte[] signatureEncoded = sig;
            ECKey.ECDSASignature sig2 = ECKey.ECDSASignature.fromComponents(Arrays.copyOf(sig, 32), Arrays.copyOfRange(sig, 32, 64), (byte) ((sig[64] & 0xFF) + 27));

//            ECKey.ECDSASignature sig3 = ECKey.ECDSASignature.fromComponents(
//                    Arrays.copyOfRange(signatureEncoded, 1, 33),
//                    Arrays.copyOfRange(signatureEncoded, 33, 65),
//                    (byte) ((signatureEncoded[0] & 0xFF)+27));
            System.out.println("hash:" + ByteArray.toHexString(hash));
            byte[] fromAddress = ECKey.signatureToAddress(hash, sig2);
            System.out.println(fromAddress.length);
            System.out.println(ByteArray.toHexString(fromAddress));
            System.out.println("c2: " + WalletApi.encode58Check(fromAddress));
            System.out.println(v); // 1000000
        }
//        String txHash = "a";
//        String txHash = "773ef7fbf4f8bbea528dbf3c3e8ee1c96bf4ff1fec9ef023006f45a22d576152";
        System.out.println("===========while============");
        Optional<Protocol.Transaction> tx2 = WalletApi.getTransactionById(txHash);

        if (tx2.isPresent()) {
            System.out.println("tx:");
            System.out.println(tx2.get());
            System.out.println(tx2.get().getRawData() == null);
            System.out.println(tx2.get().getRetCount());
            System.out.println(tx2.get().getRawData().getContractCount());
            System.out.println(tx2.get().getRawData().getExpiration());
        }
        Optional<Protocol.TransactionInfo> txInfo2 = WalletApi.getTransactionInfoById(txHash);
        if (txInfo2.isPresent()) {
            System.out.println("info:");
            System.out.println(txInfo2.get());
            System.out.println(txInfo2.get() == null);
            System.out.println("blocknum: " + txInfo2.get().getBlockNumber());
            System.out.println(ByteArray.toHexString(txInfo2.get().getId().toByteArray()));
            System.out.println(txInfo2.get().getFee());
//            System.out.println(getTokenValueByDecimals(BigDecimal.valueOf(1), 6));
//            System.out.println(getValueByTokenDecimals("1", 6L));
            System.out.println(BigDecimal.valueOf(0).divide(BigDecimal.valueOf(1000000)));

            int count = txInfo2.get().getLogCount();
            System.out.println("count: " + count);
            Protocol.TransactionInfo txinfo2 = txInfo2.get();
            System.out.println(txinfo2.getLog(0).getTopicsCount());

            Protocol.TransactionInfo.Log logs = txinfo2.getLog(0);
            // tokenAddress
            System.out.println(ByteArray.toHexString(logs.getAddress().toByteArray()));
//            byte[] tokenAddress = new byte[21];
//            tokenAddress[0] = WalletApi.getAddressPreFixByte();
//            System.arraycopy(logs.getAddress().toByteArray(), 0, tokenAddress, 1, 20);
//            String tokenAddressStr = WalletApi.encode58Check(tokenAddress);
            System.out.println("tokenaddress: " + getAddressFromHash(logs.getAddress().toByteArray()));
            // transfer
            System.out.println(ByteArray.toHexString(logs.getTopics(0).toByteArray()));
            // from
            System.out.println(ByteArray.toHexString(logs.getTopics(1).toByteArray()));
            System.out.println("from: " + getAddressFromHash(logs.getTopics(1).toByteArray()));
            // to
            System.out.println(ByteArray.toHexString(logs.getTopics(2).toByteArray()));
            System.out.println("to: " + getAddressFromHash(logs.getTopics(2).toByteArray()));
            // value
            System.out.println(ByteArray.toHexString(logs.getData().toByteArray()));
            long value = ByteArray.toLong(logs.getData().toByteArray());
            System.out.println("value: " + value);

            System.out.println("res: " + ByteArray.toLong(txinfo2.getContractResult(0).toByteArray()));
        }

    }

    public static String getAddressFromHash(byte[] hash) {
        if (hash.length != 20 && hash.length != 32) return null;
        byte[] address = new byte[21];
        address[0] = WalletApi.getAddressPreFixByte();
        if (hash.length == 20) {
            System.arraycopy(hash, 0, address, 1, 20);
        } else {
            System.arraycopy(hash, 12, address, 1, 20);
        }
        return WalletApi.encode58Check(address);
    }
}
