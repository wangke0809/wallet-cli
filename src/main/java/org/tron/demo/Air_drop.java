package org.tron.demo;

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
import org.tron.common.crypto.ECKey;
import org.tron.common.utils.ByteArray;
import org.tron.common.utils.Utils;
import org.tron.core.config.Configuration;
import org.tron.core.exception.CancelException;
import org.tron.core.exception.CipherException;
import org.tron.protos.Protocol.Account;
import org.tron.walletcli.WalletApiWrapper;
import org.tron.walletserver.GrpcClient;
import org.tron.walletserver.WalletApi;

public class Air_drop {

  private static final Logger logger = LoggerFactory.getLogger("Client");
  private static GrpcClient rpcCli = null;
  private WalletApiWrapper walletApiWrapper = new WalletApiWrapper();
  private static final String addresssFilePath = "Address";
  private static final String addresssFileName = "address.txt";
  private String assetId;
  private long amount;
  private boolean randAmount = false;
  private static int addressNumber;
  private static byte[] owner = null;
  private byte[] privateKey = null;

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
    rpcCli = new GrpcClient(fullNode, solidityNode);
  }

  public Air_drop() {
    Config config = Configuration.getByPath("config.conf");

    if (config.hasPath("assertId")) {
      this.assetId = config.getString("assertId");
    }

    if (config.hasPath("amount")) {
      this.amount = config.getLong("amount");
    }

    if (config.hasPath("rand_amount")) {
      this.randAmount = config.getBoolean("rand_amount");
    }

    if (config.hasPath("privateKey")) {
      String priKey = config.getString("privateKey");
      privateKey = ByteArray.fromHexString(priKey);
    }

    if (config.hasPath("KeyNum")) {
      addressNumber = config.getInt("KeyNum");
    }
  }

  private long getBalance(byte[] address) {
    Account account = WalletApi.queryAccount(address);
    long balance = 0;
    if (account != null && account.getAssetV2().containsKey(this.assetId)) {
      balance = account.getAssetV2().get(this.assetId);
    }
    logger.info(WalletApi.encode58Check(address) + "'s balance is " + balance);
    return balance;
  }

  private long queryBalance() throws IOException {
    byte[] owner = ECKey.fromPrivate(this.privateKey).getAddress();
    long totalBalance = 0;
    long balance = getBalance(owner);
    totalBalance += balance;

    File path = new File(addresssFilePath);
    if (!path.exists()) {
      throw new IOException("No directory");
    }
    File addressFile = new File(path, addresssFileName);

    FileInputStream inputStream = null;
    InputStreamReader inputStreamReader = null;
    BufferedReader bufferedReader = null;
    try {
      inputStream = new FileInputStream(addressFile);
      inputStreamReader = new InputStreamReader(inputStream);
      bufferedReader = new BufferedReader(inputStreamReader);

      String address;
      while ((address = bufferedReader.readLine()) != null) {
        balance = getBalance(WalletApi.decodeFromBase58Check(address));
        totalBalance += balance;
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
    logger.info("Total balance is " + totalBalance);
    return totalBalance;
  }

  private void airDropAsset()
      throws IOException, CipherException, CancelException {
    File path = new File(addresssFilePath);
    if (!path.exists()) {
      throw new IOException("No directory");
    }
    File addressFile = new File(path, addresssFileName);

    FileInputStream inputStream = null;
    InputStreamReader inputStreamReader = null;
    BufferedReader bufferedReader = null;
    try {
      inputStream = new FileInputStream(addressFile);
      inputStreamReader = new InputStreamReader(inputStream);
      bufferedReader = new BufferedReader(inputStreamReader);

      String address;
      while ((address = bufferedReader.readLine()) != null) {
        this.transferAsset(this.privateKey, address, this.assetId, this.amount, this.randAmount);
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

  private boolean transferAsset(byte[] privateKey, String toAddress, String assertId, long amount,
      boolean randAmount)
      throws CipherException, IOException, CancelException {
    if (randAmount) {
      amount = new Random().nextInt((int) amount) + amount / 2;
    }

    boolean result = walletApiWrapper.transferAsset(privateKey, toAddress, assertId, amount);
    if (result) {
      logger.info("transferAsset " + amount + " " + assertId + " to " + toAddress + " successful");
    } else {
      logger.info("transferAsset " + amount + " " + assertId + " to " + toAddress + " failed");
    }
    return result;
  }

  private static File creteDirct() throws IOException {
    File path = new File(addresssFilePath);
    if (!path.exists()) {
      if (!path.mkdir()) {
        throw new IOException("Make directory failed!");
      }
    } else {
      if (!path.isDirectory()) {
        if (path.delete()) {
          if (!path.mkdir()) {
            throw new IOException("Make directory failed!");
          }
        } else {
          throw new IOException("File exists and can not be deleted!");
        }
      }
    }

    return path;
  }

  private void genPrivateAddress() throws IOException {
    int num = this.addressNumber;
    File path = creteDirct();
    long time = System.currentTimeMillis();
    File privateFile = new File(path, time + "private.txt");
    File addressFile = new File(path, time + "address.txt");
    FileOutputStream privateWriter = new FileOutputStream(privateFile);
    OutputStreamWriter privateOSW = new OutputStreamWriter(privateWriter);
    FileOutputStream addressriter = new FileOutputStream(addressFile);
    OutputStreamWriter addressOSW = new OutputStreamWriter(addressriter);

    try {
      for (int i = 0; i < num; i++) {
        ECKey eCkey = new ECKey(Utils.getRandom());  //Gen new Keypair
        byte[] address = eCkey.getAddress();
        String base58checkAddress = WalletApi.encode58Check(address);
        privateOSW.append(ByteArray.toHexString(eCkey.getPrivKeyBytes()) + "\n");
        addressOSW.append(base58checkAddress + "\n");
      }
    } catch (Exception e) {
      throw new IOException(e.getMessage());
    } finally {
      privateOSW.close();
      addressOSW.close();
      privateWriter.close();
      addressriter.close();
    }
  }

  public static void main(String[] args) throws IOException, CipherException, CancelException {
    Air_drop air_drop = new Air_drop();
    air_drop.genPrivateAddress();

    long total_0 = air_drop.queryBalance();
    air_drop.airDropAsset();
    long total_1 = air_drop.queryBalance();

    if (total_0 == total_1) {
      logger.info("Total balance is same.");
    } else {
      logger.info(
          "Total balance is " + total_0 + " before transfer but " + total_1 + " after transfer.");
    }
  }
}
