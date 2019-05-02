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
        WalletApi.setGrpcClient("grpc.shasta.trongrid.io:50051", "grpc.shasta.trongrid.io:50052", true, 2);


        // transfer success 92f3e06f035fc069df39e329232ba6ac722c127d96cdfbb39922f0649d6b6807
        // transfer failure 10e2efbe28967a9263e347a1991c4cfb1985e4b9edf71ee290526ae701c16dba
        // mainnet success f777a54264534692f6de607335852784bd686308ff3048ef8355fe48a6d59f09

        String txHash = "db24242127115a737c0d72224069df913edc5021d25b7414575d792991bd0cbb";
        Optional<Protocol.Transaction> tx = WalletApi.getTransactionById(txHash);
        if (tx.isPresent()) {
            Protocol.Transaction t = tx.get();
            System.out.println(tx.get());
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
