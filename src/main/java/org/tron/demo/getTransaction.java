package org.tron.demo;

import org.spongycastle.util.encoders.Base64;
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
public class getTransaction {
    public static BigInteger getTokenValueByDecimals(BigDecimal value, int decimals) {
        if (value == null) {
            return new BigInteger("0");
        }
        BigInteger bigInteger = new BigInteger(String.valueOf(value.multiply(BigDecimal.valueOf(10).pow(decimals)).stripTrailingZeros().toPlainString()).split("\\.")[0]);
        return bigInteger;
    }

    public static BigDecimal getValueByTokenDecimals(String value, Long decimals) {
        if (StringUtils.isEmpty(value)) {
            return new BigDecimal("0");
        }

        int length = value.length();
        int de = decimals.intValue();
        //超过位数，在指定地方加入点
        if (length > de) {
            int endIndex = length - de;
            StringBuilder sb = new StringBuilder(value);
            sb.insert(endIndex, ".");
            BigDecimal bigDecimal = new BigDecimal(sb.toString());
            bigDecimal = bigDecimal.stripTrailingZeros();
            return bigDecimal;
        } else {
            //未超过位数，补齐位数
            int dif = de - length;
            for (int i = 0; i < dif; i++) {
                value = "0" + value;
            }
            value = "0." + value;
            return new BigDecimal(value).stripTrailingZeros();
        }
    }

    public static void main(String[] args) throws Exception {
        WalletApi.setGrpcClient("grpc.shasta.trongrid.io:50051", "grpc.shasta.trongrid.io:50052", true, 2);

        // 4459511117f49d05d0326ae68ef8131d617b480b3e8c5d13aebf6d387816d7a0
        // 773ef7fbf4f8bbea528dbf3c3e8ee1c96bf4ff1fec9ef023006f45a22d576152
        // net use c62b260d96579e621c7f74f2936d587f2a5a34d9d83c0df45475254a255c5d2b
        // net fee cc64b2db2c1541dfaa812400eaec2e792647309690df21d0dc84d133f32ba77c
        String txHash = "cc64b2db2c1541dfaa812400eaec2e792647309690df21d0dc84d133f32ba77c";
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
            Contract.TransferContract transferContract = Contract.TransferContract.parseFrom(t.getRawData().getContractList().get(0).getParameter().getValue().toByteArray());
            System.out.println("transferContract:" + transferContract);
            System.out.println("owner: " + WalletApi.encode58Check(transferContract.getOwnerAddress().toByteArray()));
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
            System.out.println(getTokenValueByDecimals(BigDecimal.valueOf(1), 6));
            System.out.println(getValueByTokenDecimals("1", 6L));
            System.out.println(BigDecimal.valueOf(0).divide(BigDecimal.valueOf(1000000)));
        }

    }
}
