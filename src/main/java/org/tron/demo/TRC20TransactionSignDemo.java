package org.tron.demo;

import com.google.protobuf.Any;
import com.google.protobuf.ByteString;
import com.google.protobuf.InvalidProtocolBufferException;
import org.tron.common.crypto.ECKey;
import org.tron.common.crypto.Sha256Hash;
import org.tron.common.utils.ByteArray;
import org.tron.core.exception.CancelException;
import org.tron.keystore.Wallet;
import org.tron.protos.Contract;
import org.tron.protos.Protocol;
import org.tron.protos.Protocol.Block;
import org.tron.protos.Protocol.Transaction;
import org.tron.walletserver.WalletApi;

import java.util.Arrays;
import java.util.Optional;

public class TRC20TransactionSignDemo {

  public static Transaction setReference(Transaction transaction, Block newestBlock) {
    long blockHeight = newestBlock.getBlockHeader().getRawData().getNumber();
    byte[] blockHash = getBlockHash(newestBlock).getBytes();
    byte[] refBlockNum = ByteArray.fromLong(blockHeight);
    Transaction.raw rawData = transaction.getRawData().toBuilder()
        .setRefBlockHash(ByteString.copyFrom(ByteArray.subArray(blockHash, 8, 16)))
        .setRefBlockBytes(ByteString.copyFrom(ByteArray.subArray(refBlockNum, 6, 8)))
        .build();
    return transaction.toBuilder().setRawData(rawData).build();
  }

  public static Sha256Hash getBlockHash(Block block) {
    return Sha256Hash.of(block.getBlockHeader().getRawData().toByteArray());
  }

  public static String getTransactionHash(Transaction transaction) {
    String txid = ByteArray.toHexString(Sha256Hash.hash(transaction.getRawData().toByteArray()));
    return txid;
  }


  public static Transaction createTransaction(byte[] tokenAddress, byte[] from, byte[] to, long amount) {
    Transaction.Builder transactionBuilder = Transaction.newBuilder();
    Block newestBlock = WalletApi.getBlock(-1);

    Transaction.Contract.Builder contractBuilder = Transaction.Contract.newBuilder();
    Contract.TriggerSmartContract.Builder triggerSmartContract = Contract.TriggerSmartContract.newBuilder();
    ByteString bsTo = ByteString.copyFrom(to);

    ByteString bsOwner = ByteString.copyFrom(from);
    ByteString token = ByteString.copyFrom(tokenAddress);

    triggerSmartContract.setContractAddress(token);
    triggerSmartContract.setOwnerAddress(bsOwner);
//    String data = "0xa9059cbb00000000000000000000000016afde1644cab31a1f8df7ed0ea5625c36a6d62500000000000000000000000000000000000000000000000000000000000186a0";
    String data = "0xa9059cbb0000000000000000000000005457d1d10e5e39292a170a0d7ff834186265c34600000000000000000000000000000000000000000000000000000000000186a0";
    byte[] dataByte = ByteArray.fromHexString(data);
    ByteString dataByteString = ByteString.copyFrom(dataByte);
    triggerSmartContract.setData(dataByteString);



    try {
      Any any = Any.pack(triggerSmartContract.build());
      contractBuilder.setParameter(any);
    } catch (Exception e) {
      return null;
    }
    contractBuilder.setType(Transaction.Contract.ContractType.TriggerSmartContract);
    transactionBuilder.getRawDataBuilder().addContract(contractBuilder)
        .setTimestamp(System.currentTimeMillis())
        .setExpiration(newestBlock.getBlockHeader().getRawData().getTimestamp() + 10 * 60 * 60 * 1000).setFeeLimit(10000000);

    Transaction transaction = transactionBuilder.build();
    Transaction refTransaction = setReference(transaction, newestBlock);

    return refTransaction;
  }


  private static byte[] signTransaction2Byte(byte[] transaction, byte[] privateKey)
      throws InvalidProtocolBufferException {
    ECKey ecKey = ECKey.fromPrivate(privateKey);
    Transaction transaction1 = Transaction.parseFrom(transaction);
    System.out.println("permis");
    byte[] rawdata = transaction1.getRawData().toByteArray();
    byte[] hash = Sha256Hash.hash(rawdata);
    byte[] sign = ecKey.sign(hash).toByteArray();
    System.out.println("sign length " + sign.length);
    transaction1 = transaction1.toBuilder().addSignature(ByteString.copyFrom(sign)).build();
    return transaction1.toByteArray();
  }

  private static Transaction signTransaction2Object(byte[] transaction, byte[] privateKey)
      throws InvalidProtocolBufferException {
    ECKey ecKey = ECKey.fromPrivate(privateKey);
    Transaction transaction1 = Transaction.parseFrom(transaction);
    byte[] rawdata = transaction1.getRawData().toByteArray();
    byte[] hash = Sha256Hash.hash(rawdata);
    byte[] sign = ecKey.sign(hash).toByteArray();
//    System.out.println("sign length " + sign.length);
    return transaction1.toBuilder().addSignature(ByteString.copyFrom(sign)).build();
  }

  private static boolean broadcast(byte[] transactionBytes) throws InvalidProtocolBufferException {
    return WalletApi.broadcastTransaction(transactionBytes);
  }

  private static void base58checkToHexString() {
    String base58check = "TGehVcNhud84JDCGrNHKVz9jEAVKUpbuiv";
    String hexString = ByteArray.toHexString(WalletApi.decodeFromBase58Check(base58check));
    System.out.println(hexString);
  }

  private static void hexStringTobase58check() {
    String hexString = "414948c2e8a756d9437037dcd8c7e0c73d560ca38d";
    String base58check = WalletApi.encode58Check(ByteArray.fromHexString(hexString));
    System.out.println(base58check);
  }

  public static void main(String[] args) throws InvalidProtocolBufferException, CancelException {
    WalletApi.setGrpcClient("grpc.shasta.trongrid.io:50051", "grpc.shasta.trongrid.io:50052", true, 2);
    String privateStr = "cc72d47613396b760b468bb88063b0facc8870d289c2ab272c57f58233956f34";
    byte[] privateBytes = ByteArray.fromHexString(privateStr);
    ECKey ecKey = ECKey.fromPrivate(privateBytes);
    byte[] from = ecKey.getAddress();
    // TC3AYma8o31DeJwbScuBtts7asxZfqMkJr
    // TExqJ79q4znGTNTQJXVbpP5wBysBjG4L6g
    byte[] to = WalletApi.decodeFromBase58Check("TExqJ79q4znGTNTQJXVbpP5wBysBjG4L6g");
    byte[] tokenAddress = WalletApi.decodeFromBase58Check("TDqLcrvLxkGyFGuES6fJN1qpfcN4b9cd2x");
    long amount = 100000; // 0.1 usdt
    byte[] to2 = Arrays.copyOfRange(to, 1, to.length);
    System.out.println(to.length);
    System.out.println(to2.length);
    System.out.println(ByteArray.toHexString(to));
    System.out.println(ByteArray.toHexString(to2));

    try {
      while (true) {
        Transaction transaction = createTransaction(tokenAddress, from, to, amount);
        byte[] transactionBytes = transaction.toByteArray();
        byte[] transaction4 = signTransaction2Byte(transactionBytes, privateBytes);
        boolean result = broadcast(transaction4);
        System.out.println("broadcast: " + result);
        byte[] hash1 = Sha256Hash.hash(transaction.getRawData().toByteArray());
        System.out.println(
                "Receive txid = " + ByteArray.toHexString(hash1));
        String txHash = ByteArray.toHexString(hash1);

        System.out.println("===========while============");
        Optional<Transaction> tx2 = WalletApi.getTransactionById(txHash);

        if (tx2.isPresent()) {
          System.out.println("tx:");
          System.out.println(tx2.get());
          System.out.println(tx2.get().getRawData()==null);
        }
        Optional<Protocol.TransactionInfo> txInfo2 = WalletApi.getTransactionInfoById(txHash);
        if (txInfo2.isPresent()) {
          System.out.println("info:");
          System.out.println(txInfo2.get());
          System.out.println(txInfo2.get()==null);
          System.out.println(txInfo2.get().getBlockNumber());

        }
        Thread.sleep(5000);
        break;
      }
    }catch (Exception e){
      System.out.println(e);
    }



  }
}
