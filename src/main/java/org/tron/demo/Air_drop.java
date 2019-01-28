package org.tron.demo;

import com.typesafe.config.Config;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.util.Optional;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.tron.common.crypto.ECKey;
import org.tron.common.crypto.Sha256Hash;
import org.tron.common.utils.ByteArray;
import org.tron.common.utils.TransactionUtils;
import org.tron.core.config.Configuration;
import org.tron.protos.Contract;
import org.tron.protos.Contract.TransferAssetContract;
import org.tron.protos.Protocol.Account;
import org.tron.protos.Protocol.Transaction;
import org.tron.protos.Protocol.Transaction.Contract.ContractType;
import org.tron.walletserver.GrpcClient;
import org.tron.walletserver.WalletApi;

class AirException extends Exception {

  public AirException(String msg) {
    super(msg);
  }
}

public class Air_drop {

  private static byte[] owner = null;
  private static byte[] privateKey;


  private static final Logger logger = LoggerFactory.getLogger("Client");
  private static String assetId;
  private static File addressFile = new File("address.txt");
  private static File transactionFile = new File("transaction.txt");
  private static File transactionSignedFile = new File("transactionSigned.txt");
  private static File logs = new File("logs.txt");
  private static File txIdFile = new File("txid.txt");
  private static File lostAddress = new File("lostaddress.txt");
  private static GrpcClient rpcCli = null;

  private static Transaction createTransaction(Contract.TransferAssetContract contract) {
    Transaction transaction = rpcCli.createTransferAssetTransaction(contract);
    Transaction.raw rawData = transaction.getRawData().toBuilder()
        .setExpiration(System.currentTimeMillis() + 6 * 60 * 60 * 1000L).build(); //6h
    transaction = transaction.toBuilder().setRawData(rawData).build();
    return transaction;
  }

  private static void printLostAddress(String address, long amount) throws IOException {
    FileOutputStream transactionFOS = null;
    OutputStreamWriter transactionOSW = null;

    try {
      transactionFOS = new FileOutputStream(lostAddress, true);
      transactionOSW = new OutputStreamWriter(transactionFOS);

      transactionOSW.append(address + " " + amount + "\n");
    } catch (IOException e) {
      throw e;
    } finally {

      if (transactionOSW != null) {
        transactionOSW.close();
      }
      if (transactionFOS != null) {
        transactionFOS.close();
      }
    }
  }

  private static void initConfig() {
    Config config = Configuration.getByPath("config-on.conf");

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
    if (config.hasPath("privateKey")) {
      String priKey = config.getString("privateKey");
      privateKey = ByteArray.fromHexString(priKey);
    }
    rpcCli = new GrpcClient(fullNode, solidityNode);
  }

  private static boolean broadcastTransaction(Transaction transaction) {
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

  private static boolean searchTransaction(String txid) {
    Optional<Transaction> result = WalletApi.getTransactionById(txid);
    if (!result.isPresent()) {
      return false;
    }

    Transaction transaction = result.get();
    if (transaction == null) {
      return false;
    }
    
    return true;
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

  private static void createTransaction() throws IOException {
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
      while ((number = bufferedReader.readLine()) != null) {
        long amount = Long.parseLong(number);
        address = bufferedReader.readLine();
        Transaction transaction = createTransaction(owner, WalletApi.decodeFromBase58Check(address),
            amount);
        if (transaction == null) {
          printLostAddress(address, amount);
          continue;
        }
        transactionOSW.append(number + "\n");
        transactionOSW.append(ByteArray.toHexString(transaction.toByteArray()) + "\n");

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

    FileOutputStream outputStreamTxid = null;
    OutputStreamWriter outputStreamWriterTxid = null;
    try {
      inputStream = new FileInputStream(transactionSignedFile);
      inputStreamReader = new InputStreamReader(inputStream);
      bufferedReader = new BufferedReader(inputStreamReader);

      outputStream = new FileOutputStream(logs);
      outputStreamWriter = new OutputStreamWriter(outputStream);

      outputStreamTxid = new FileOutputStream(txIdFile);
      outputStreamWriterTxid = new OutputStreamWriter(outputStreamTxid);

      String transactionSigned;
      String number;
      while ((number = bufferedReader.readLine()) != null) {
        transactionSigned = bufferedReader.readLine();
        Transaction transaction = Transaction.parseFrom(ByteArray.fromHexString(transactionSigned));
        Transaction.Contract contract = transaction.getRawData().getContract(0);
        if (contract.getType() == ContractType.TransferAssetContract) {
          TransferAssetContract transferContract = contract.getParameter()
              .unpack(TransferAssetContract.class);
          long amount = transferContract.getAmount();
          byte[] toAddress = transferContract.getToAddress().toByteArray();
          String assertId = transferContract.getAssetName().toStringUtf8();
          outputStreamWriter.append(number + "\n");
          outputStreamWriterTxid.append(number + "\n");
          if (broadcastTransaction(transaction)) {
            System.out.println(
                "Send " + amount + " " + assertId + " to " + WalletApi.encode58Check(toAddress)
                    + " successful !!!");
            outputStreamWriter.append(amount + " " + WalletApi.encode58Check(toAddress) + "\n");
            String txid = ByteArray
                .toHexString(Sha256Hash.hash(transaction.getRawData().toByteArray()));
            outputStreamWriterTxid
                .append(txid + " " + WalletApi.encode58Check(toAddress) + " " + amount);
          } else {
            System.out.println(
                "Send " + amount + " " + assertId + " to " + WalletApi.encode58Check(toAddress)
                    + " failed !!!");
            outputStreamWriter
                .append(amount + " " + WalletApi.encode58Check(toAddress) + " failed !!!" + "\n");
            printLostAddress(WalletApi.encode58Check(toAddress), amount);
          }
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
      if (outputStreamWriterTxid != null) {
        outputStreamWriterTxid.close();
      }
      if (outputStreamTxid != null) {
        outputStreamTxid.close();
      }
    }
  }

  private static void signTransaction(byte[] privateKey) throws IOException {
    File transactionSignedFile = new File("transactionSigned.txt");
    File transactionFile = new File("transaction.txt");
    FileInputStream inputStream = null;
    InputStreamReader inputStreamReader = null;
    BufferedReader bufferedReader = null;
    FileOutputStream outputStream = null;
    OutputStreamWriter outputStreamWriter = null;
    ECKey ecKey = ECKey.fromPrivate(privateKey);
    try {
      inputStream = new FileInputStream(transactionFile);
      inputStreamReader = new InputStreamReader(inputStream);
      bufferedReader = new BufferedReader(inputStreamReader);

      outputStream = new FileOutputStream(transactionSignedFile);
      outputStreamWriter = new OutputStreamWriter(outputStream);

      String transaction;
      String number;
      while ((number = bufferedReader.readLine()) != null) {
        transaction = bufferedReader.readLine();
        Transaction transaction1 = Transaction.parseFrom(ByteArray.fromHexString(transaction));
        transaction1 = TransactionUtils.setTimestamp(transaction1);
        transaction1 = TransactionUtils.sign(transaction1, ecKey);
        outputStreamWriter.append(number + "\n");
        outputStreamWriter.append(ByteArray.toHexString(transaction1.toByteArray()) + "\n");
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

  private static void queryTransaction() throws IOException {
    FileInputStream inputStream = null;
    InputStreamReader inputStreamReader = null;
    BufferedReader bufferedReader = null;

    try {
      inputStream = new FileInputStream(txIdFile);
      inputStreamReader = new InputStreamReader(inputStream);
      bufferedReader = new BufferedReader(inputStreamReader);

      String data;
      while ((data = bufferedReader.readLine()) != null) {
        String[] datas = data.split(" ");
        if (datas.length != 3) {
          continue;
        }
        String txid = datas[0];
        String address = datas[1];
        String amountStr = datas[2];
        Long amount = 0L;
        try {
          amount = Long.parseLong(amountStr);
        } catch (NumberFormatException e) {
          continue;
        }
        if (!searchTransaction(txid)) {
          printLostAddress(address, amount);
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
    }
  }

  public static void main(String[] args) throws IOException {
    initConfig();

    for (String arg : args) {
      System.out.println(arg);
    }
    if (args[0].equals("airdrop")) {
      createTransaction();
      signTransaction(privateKey);
      sendCoin();
      queryTransaction();
    }

    if (args[0].equals("create")) {
      createTransaction();
      return;
    }
    if (args[0].equals("sign")) {
      signTransaction(privateKey);
      return;
    }
    if (args[0].equals("send")) {
      sendCoin();
      return;
    }
    if (args[0].equals("query")) {
      queryTransaction();
      return;
    }

  }
}
