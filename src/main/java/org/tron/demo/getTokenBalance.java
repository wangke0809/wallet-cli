package org.tron.demo;

import com.google.protobuf.Any;
import com.google.protobuf.ByteString;
import com.google.protobuf.InvalidProtocolBufferException;
import org.tron.api.GrpcAPI;
import org.tron.common.crypto.ECKey;
import org.tron.common.crypto.Sha256Hash;
import org.tron.common.utils.ByteArray;
import org.tron.core.exception.CancelException;
import org.tron.protos.Contract;
import org.tron.protos.Protocol;
import org.tron.protos.Protocol.Block;
import org.tron.protos.Protocol.Transaction;
import org.tron.walletserver.WalletApi;

import java.util.Arrays;
import java.util.Optional;

public class getTokenBalance {


  public static void createTransaction(byte[] tokenAddress, byte[] from, byte[] to, long amount) {
    Transaction.Builder transactionBuilder = Transaction.newBuilder();
    Block newestBlock = WalletApi.getBlock(-1);

    Transaction.Contract.Builder contractBuilder = Transaction.Contract.newBuilder();
    Contract.TriggerSmartContract.Builder triggerSmartContract = Contract.TriggerSmartContract.newBuilder();
    ByteString bsTo = ByteString.copyFrom(to);

//    ByteString bsOwner = ByteString.copyFrom(from);
    ByteString token = ByteString.copyFrom(tokenAddress);

    triggerSmartContract.setContractAddress(token);
//    triggerSmartContract.setOwnerAddress(bsOwner);
    String data = "0x70a0823100000000000000000000000016afde1644cab31a1f8df7ed0ea5625c36a6d625";
    byte[] dataByte = ByteArray.fromHexString(data);
    ByteString dataByteString = ByteString.copyFrom(dataByte);
    triggerSmartContract.setData(dataByteString);

    GrpcAPI.TransactionExtention transactionExtention = WalletApi.getGrpcClient().triggerContract(triggerSmartContract.build());

    byte[] res = transactionExtention.getConstantResult(0).toByteArray();

    System.out.println(ByteArray.toHexString(res));
    System.out.println(ByteArray.toLong(res));

    System.out.println(transactionExtention);

  }

  public static void createTransaction2(byte[] tokenAddress, byte[] from, byte[] to, long amount) {
    Transaction.Builder transactionBuilder = Transaction.newBuilder();
    Block newestBlock = WalletApi.getBlock(-1);

    Transaction.Contract.Builder contractBuilder = Transaction.Contract.newBuilder();
    Contract.TriggerSmartContract.Builder triggerSmartContract = Contract.TriggerSmartContract.newBuilder();
    ByteString bsTo = ByteString.copyFrom(to);

    ByteString bsOwner = ByteString.copyFrom(from);
    ByteString token = ByteString.copyFrom(tokenAddress);

    triggerSmartContract.setContractAddress(token);
    triggerSmartContract.setOwnerAddress(bsOwner);
    String data = "0x313ce567";
    byte[] dataByte = ByteArray.fromHexString(data);
    ByteString dataByteString = ByteString.copyFrom(dataByte);
    triggerSmartContract.setData(dataByteString);

    GrpcAPI.TransactionExtention transactionExtention = WalletApi.getGrpcClient().triggerContract(triggerSmartContract.build());

    byte[] res = transactionExtention.getConstantResult(0).toByteArray();

    System.out.println(ByteArray.toHexString(res));
    System.out.println(ByteArray.toLong(res));

    System.out.println(transactionExtention);

  }



  public static void main(String[] args) throws InvalidProtocolBufferException, CancelException {
    WalletApi.setGrpcClient("grpc.shasta.trongrid.io:50051", "grpc.shasta.trongrid.io:50052", true, 2);
    String privateStr = "cc72d47613396b760b468bb88063b0facc8870d289c2ab272c57f58233956f34";
    byte[] privateBytes = ByteArray.fromHexString(privateStr);
    ECKey ecKey = ECKey.fromPrivate(privateBytes);
    byte[] from = ecKey.getAddress();
    byte[] to = WalletApi.decodeFromBase58Check("TC3AYma8o31DeJwbScuBtts7asxZfqMkJr");
    byte[] tokenAddress = WalletApi.decodeFromBase58Check("TDqLcrvLxkGyFGuES6fJN1qpfcN4b9cd2x");
    long amount = 100000; //1 TRX, api only receive trx in drop, and 1 trx = 1000000 drop
    byte[] to2 = Arrays.copyOfRange(to, 1, to.length);
    System.out.println(to.length);
    System.out.println(to2.length);
    System.out.println(ByteArray.toHexString(to));
    System.out.println(ByteArray.toHexString(to2));


    createTransaction(tokenAddress, from, to, amount);



  }
}
