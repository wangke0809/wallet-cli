package org.tron.demo;

import com.google.protobuf.InvalidProtocolBufferException;
import com.typesafe.config.Config;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.util.Random;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.tron.common.utils.ByteArray;
import org.tron.core.config.Configuration;
import org.tron.protos.Contract;
import org.tron.protos.Contract.TransferAssetContract;
import org.tron.protos.Protocol.Account;
import org.tron.protos.Protocol.Transaction;
import org.tron.walletserver.GrpcClient;
import org.tron.walletserver.WalletApi;

public class OnLine {

  private static int addressNumber;
  private static byte[] owner = null;

  private static final Logger logger = LoggerFactory.getLogger("Client");
  private static String assetId;
  private static File addressFile = new File("address.txt");
  private static File transactionFile = new File("transaction.txt");
  private static File transactionSignedFile = new File("transactionSigned.txt");
  private static File balanceFile = new File("balance.txt");
  private static File logs = new File("logs.txt");
  private static GrpcClient rpcCli = null;

  private static Transaction createTransaction(Contract.TransferAssetContract contract) {
    Transaction transaction = rpcCli.createTransferAssetTransaction(contract);
    Transaction.raw rawData = transaction.getRawData().toBuilder()
        .setExpiration(System.currentTimeMillis() + 60 * 60 * 1000L).build(); //1h
    transaction = transaction.toBuilder().setRawData(rawData).build();
    return transaction;
  }

  private static void initConfig() {
    Config config = Configuration.getByPath("config-on.conf");

    if (config.hasPath("AddressNum")) {
      String keyNum = config.getString("AddressNum");
      addressNumber = Integer.parseInt(keyNum);
    } else {
      addressNumber = 300;
    }
    if (config.hasPath("address")) {
      String address = config.getString("address");
      owner = WalletApi.decodeFromBase58Check(address);
    }

    String fullNode = "";
    String solidityNode = "";
    if (config.hasPath("soliditynode.ip.list")) {
      solidityNode = config.getStringList("soliditynode.ip.list").get(0);
    }
    if (config.hasPath("fullnode.ip.list")) {
      fullNode = config.getStringList("fullnode.ip.list").get(0);
    }
    if (config.hasPath("assertId")) {
      assetId = config.getString("assertId");
    }
    rpcCli = new GrpcClient(fullNode, solidityNode);
  }

  private static boolean sendCoin(Transaction transaction) throws InvalidProtocolBufferException {
    return rpcCli.broadcastTransaction(transaction);
  }

  private static long getBalance(byte[] address) {
    Account account = WalletApi.queryAccount(address);
    long balance = 0;
    if (account != null && account.getAssetV2().containsKey(assetId)) {
      balance = account.getAssetV2().get(assetId);
    }
    logger.info(WalletApi.encode58Check(address) + "'s balance is " + balance);
    return balance;
  }

  private static Transaction createTransaction(byte[] owner, byte[] toAddress, long amount) {
    Contract.TransferAssetContract contract = WalletApi
        .createTransferAssetContract(toAddress, assetId.getBytes(), owner, amount);
    Transaction transaction = createTransaction(contract);
    if (transaction == null || transaction.getRawData().getContractCount() == 0) {
      System.out.println(
          "Create transaction transfer " + amount + " " + assetId + " to " + WalletApi
              .encode58Check(toAddress)
              + " failed !!!");
      return null;
    }

    System.out.println(
        "Create transaction transfer " + amount + " " + assetId + " to " + WalletApi
            .encode58Check(toAddress)
            + " successful !!!");
    return transaction;
  }

  private static long getRandomAmmount(long blance, int num) {
    if (num == 1) {
      return blance;
    }
    long ammout = blance / num;

    long random = new Random().nextLong();
    random = random % ammout / 2;  //-0.5ammount< random < 0.5ammount
    return random + ammout;  //[0.5amount, 1.5ammount]
  }

  private static void createTransaction() throws IOException {
    long totalBalance = getBalance(owner);
    FileInputStream inputStream = null;
    InputStreamReader inputStreamReader = null;
    BufferedReader bufferedReader = null;
    FileOutputStream transactionFOS = null;
    OutputStreamWriter transactionOSW = null;

    try {
      inputStream = new FileInputStream(addressFile);
      inputStreamReader = new InputStreamReader(inputStream);
      bufferedReader = new BufferedReader(inputStreamReader);

      transactionFOS = new FileOutputStream(transactionFile);
      transactionOSW = new OutputStreamWriter(transactionFOS);

      String address;
      String number;
      int i = addressNumber;
      while ((number = bufferedReader.readLine()) != null) {
        address = bufferedReader.readLine();
        long amount = getRandomAmmount(totalBalance, i);
        i--;
        Transaction transaction = createTransaction(owner, WalletApi.decodeFromBase58Check(address),
            amount);
        totalBalance -= amount;
        if (transaction != null) {
          transactionOSW.append(number + "\n");
          transactionOSW.append(ByteArray.toHexString(transaction.toByteArray()) + "\n");
        }
      }
    } catch (IOException e) {
      throw e;
    } finally {
      if (bufferedReader != null) {
        bufferedReader.close();
      }
      if (inputStreamReader != null) {
        inputStreamReader.close();
      }
      if (inputStream != null) {
        inputStream.close();
      }
      if (transactionOSW != null) {
        transactionOSW.close();
      }
      if (transactionFOS != null) {
        transactionFOS.close();
      }
    }
  }


  private static void sendCoin() throws IOException {
    FileInputStream inputStream = null;
    InputStreamReader inputStreamReader = null;
    BufferedReader bufferedReader = null;
    FileOutputStream outputStream = null;
    OutputStreamWriter outputStreamWriter = null;
    try {
      inputStream = new FileInputStream(transactionSignedFile);
      inputStreamReader = new InputStreamReader(inputStream);
      bufferedReader = new BufferedReader(inputStreamReader);

      outputStream = new FileOutputStream(logs);
      outputStreamWriter = new OutputStreamWriter(outputStream);

      String transactionSigned;
      String number;
      while ((number = bufferedReader.readLine()) != null) {
        transactionSigned = bufferedReader.readLine();
        Transaction transaction = Transaction.parseFrom(ByteArray.fromHexString(transactionSigned));
        Transaction.Contract contract = transaction.getRawData().getContract(0);
        TransferAssetContract transferContract = contract.getParameter()
            .unpack(TransferAssetContract.class);
        long amount = transferContract.getAmount();
        byte[] toAddress = transferContract.getToAddress().toByteArray();
        String assertId = transferContract.getAssetName().toStringUtf8();
        outputStreamWriter.append(number + "\n");
        if (sendCoin(transaction)) {
          System.out.println(
              "Send " + amount + " " + assertId + " to " + WalletApi.encode58Check(toAddress)
                  + " successful !!!");
          outputStreamWriter.append(amount + " " + WalletApi.encode58Check(toAddress) + "\n");
        } else {
          System.out.println(
              "Send " + amount + " " + assertId + " to " + WalletApi.encode58Check(toAddress)
                  + " failed !!!");
          outputStreamWriter
              .append(amount + " " + WalletApi.encode58Check(toAddress) + " failed !!!" + "\n");
        }
      }
    } catch (IOException e) {
      throw e;
    } finally {
      if (bufferedReader != null) {
        bufferedReader.close();
      }
      if (inputStreamReader != null) {
        inputStreamReader.close();

      }
      if (inputStream != null) {
        inputStream.close();
      }
      if (outputStreamWriter != null) {
        outputStreamWriter.close();
      }
      if (outputStream != null) {
        outputStream.close();
      }
    }
  }

  private static void queryBalance() throws IOException {
    FileInputStream inputStream = null;
    InputStreamReader inputStreamReader = null;
    BufferedReader bufferedReader = null;

    FileOutputStream outputStream = new FileOutputStream(balanceFile);
    OutputStreamWriter outputStreamWriter = new OutputStreamWriter(outputStream);

    long totalBalance1 = 0;
    try {
      inputStream = new FileInputStream(addressFile);
      inputStreamReader = new InputStreamReader(inputStream);
      bufferedReader = new BufferedReader(inputStreamReader);

      String address;
      String number;
      while ((number = bufferedReader.readLine()) != null) {
        address = bufferedReader.readLine();
        long balance = getBalance(WalletApi.decodeFromBase58Check(address));
        totalBalance1 += balance;
        outputStreamWriter.append(number + "\n");
        outputStreamWriter.append(balance + " " + address + "\n");
      }
    } catch (IOException e) {
      throw e;
    } finally {
      if (bufferedReader != null) {
        bufferedReader.close();
      }
      if (inputStreamReader != null) {
        inputStreamReader.close();
      }
      if (inputStream != null) {
        inputStream.close();
      }
      if (outputStreamWriter != null) {
        outputStreamWriter.close();
      }
      if (outputStream != null) {
        outputStream.close();
      }
    }
    System.out.println(
        "Total balance except " + WalletApi.encode58Check(owner) + " ::: " + totalBalance1);
    totalBalance1 += getBalance(owner);
    System.out.println("Total balance ::: " + totalBalance1);
  }

  public static void main(String[] args) throws IOException {
    initConfig();

    for (String arg : args) {
      System.out.println(arg);
    }

    if (args[0].equals("create")) {
      createTransaction();
      return;
    }
    if (args[0].equals("send")) {
      sendCoin();
      return;
    }
    if (args[0].equals("query")) {
      queryBalance();
      return;
    }
  }
}
