package org.tron.demo;

import com.google.protobuf.Any;
import com.google.protobuf.ByteString;
import com.google.protobuf.InvalidProtocolBufferException;
import java.util.Arrays;
import java.util.Optional;

import org.tron.api.GrpcAPI.Return;
import org.tron.api.GrpcAPI.TransactionExtention;
import org.tron.common.crypto.ECKey;
import org.tron.common.crypto.Sha256Hash;
import org.tron.common.utils.ByteArray;
import org.tron.core.exception.CancelException;
import org.tron.protos.Contract;
import org.tron.protos.Protocol;
import org.tron.protos.Protocol.Block;
import org.tron.protos.Protocol.Transaction;
import org.tron.walletserver.WalletApi;

public class TransactionSignDemo {

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


  public static Transaction createTransaction(byte[] from, byte[] to, long amount) {
    Transaction.Builder transactionBuilder = Transaction.newBuilder();
    Block newestBlock = WalletApi.getBlock(-1);

    Transaction.Contract.Builder contractBuilder = Transaction.Contract.newBuilder();
    Contract.TransferContract.Builder transferContractBuilder = Contract.TransferContract
        .newBuilder();
    transferContractBuilder.setAmount(amount);
    ByteString bsTo = ByteString.copyFrom(to);
    ByteString bsOwner = ByteString.copyFrom(from);
    transferContractBuilder.setToAddress(bsTo);
    transferContractBuilder.setOwnerAddress(bsOwner);
    try {
      Any any = Any.pack(transferContractBuilder.build());
      contractBuilder.setParameter(any);
    } catch (Exception e) {
      return null;
    }
    contractBuilder.setType(Transaction.Contract.ContractType.TransferContract);
    transactionBuilder.getRawDataBuilder().addContract(contractBuilder)
        .setTimestamp(System.currentTimeMillis())
        .setExpiration(newestBlock.getBlockHeader().getRawData().getTimestamp() + 10 * 60 * 60 * 1000);
    Transaction transaction = transactionBuilder.build();
    Transaction refTransaction = setReference(transaction, newestBlock);
    return refTransaction;
  }


  private static byte[] signTransaction2Byte(byte[] transaction, byte[] privateKey)
      throws InvalidProtocolBufferException {
    ECKey ecKey = ECKey.fromPrivate(privateKey);
    Transaction transaction1 = Transaction.parseFrom(transaction);
    byte[] rawdata = transaction1.getRawData().toByteArray();
    byte[] hash = Sha256Hash.hash(rawdata);
    byte[] sign = ecKey.sign(hash).toByteArray();
    byte[] r;
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
    WalletApi.setGrpcClient("grpc.shasta.trongrid.io:50051", "grpc.shasta.trongrid.io:50052", false, 2);
    String privateStr = "f7f013d4aa6abcb306f23f283b6e272afa81e1c9c7057fd09a4088ec5deab2ca";
    byte[] privateBytes = ByteArray.fromHexString(privateStr);
    ECKey ecKey = ECKey.fromPrivate(privateBytes);
    byte[] from = ecKey.getAddress();
    byte[] to = WalletApi.decodeFromBase58Check("TWmkxJewnMchgRGxF2CAzBiVen1jV1eBPr");
    long amount = 1_000_000L; //1 TRX, api only receive trx in drop, and 1 trx = 1000000 drop
//    Transaction transaction = createTransaction(from, to, amount);
//    byte[] transactionBytes = transaction.toByteArray();

    /*
    //sign a transaction
    Transaction transaction1 = TransactionUtils.sign(transaction, ecKey);
    //get byte transaction
    byte[] transaction2 = transaction1.toByteArray();
    System.out.println("transaction2 ::::: " + ByteArray.toHexString(transaction2));

    //sign a transaction in byte format and return a Transaction object
    Transaction transaction3 = signTransaction2Object(transactionBytes, privateBytes);
    System.out.println("transaction3 ::::: " + ByteArray.toHexString(transaction3.toByteArray()));
    */

    //sign a transaction in byte format and return a Transaction in byte format
//    byte[] transaction4 = signTransaction2Byte(transactionBytes, privateBytes);
//    System.out.println("transaction4 ::::: " + ByteArray.toHexString(transaction4));
//    Transaction transactionSigned;
//    System.out.println("rpc  version:" + WalletApi.getRpcVersion());
//
//    if (WalletApi.getRpcVersion() == 2) {
//      TransactionExtention transactionExtention = WalletApi.signTransactionByApi2(transaction, ecKey.getPrivKeyBytes());
//      if (transactionExtention == null) {
//        System.out.println("transactionExtention is null");
//        return;
//      }
//      Return ret = transactionExtention.getResult();
//      if (!ret.getResult()) {
//        System.out.println("Code = " + ret.getCode());
//        System.out.println("Message = " + ret.getMessage().toStringUtf8());
//        return;
//      }
//      System.out.println(
//          "Receive txid = " + ByteArray.toHexString(transactionExtention.getTxid().toByteArray()));
//      transactionSigned = transactionExtention.getTransaction();
//    } else {
//      transactionSigned = WalletApi.signTransactionByApi(transaction, ecKey.getPrivKeyBytes());
//    }
//    byte[] transaction5 = transactionSigned.toByteArray();
//    System.out.println("transaction5 ::::: " + ByteArray.toHexString(transaction5));
//    if (!Arrays.equals(transaction4, transaction5)){
//      System.out.println("transaction4 is not equals to transaction5 !!!!!");
//    }

//    byte[] hash1 = Sha256Hash.hash(transaction.getRawData().toByteArray());
//    System.out.println(
//            "Receive txid = " + ByteArray.toHexString(hash1));
//    String txHash = ByteArray.toHexString(hash1);
//    Optional<Transaction> tx = WalletApi.getTransactionById(txHash);
//    if (tx.isPresent()){
//      System.out.println("tx:");
//      System.out.println(tx.get());
//    }
//    Optional<Protocol.TransactionInfo>  txInfo = WalletApi.getTransactionInfoById(txHash);
//    if(txInfo.isPresent()){
//      System.out.println("info:");
//      System.out.println(txInfo.get());
//    }
//    System.out.println("-----------b--------------");
//    boolean result = broadcast(transaction4);
//    System.out.println("broadcast: " + result);
    try {
      while (true) {
        Transaction transaction = createTransaction(from, to, amount);
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
      }
    }catch (Exception e){
      System.out.println(e);
    }



  }
}
