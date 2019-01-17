package org.tron.demo;

import com.typesafe.config.Config;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.Arrays;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.tron.api.GrpcAPI.EasyTransferResponse;
import org.tron.common.crypto.ECKey;
import org.tron.common.crypto.Sha256Hash;
import org.tron.common.utils.ByteArray;
import org.tron.common.utils.Utils;
import org.tron.core.config.Configuration;
import org.tron.core.config.Parameter.CommonConstant;
import org.tron.core.exception.CancelException;
import org.tron.core.exception.CipherException;
import org.tron.keystore.StringUtils;
import org.tron.protos.Protocol.Transaction;
import org.tron.walletcli.WalletApiWrapper;
import org.tron.walletserver.GrpcClient;
import org.tron.walletserver.WalletApi;

public class Air_drop {

  private static final Logger logger = LoggerFactory.getLogger("Client");
  private WalletApiWrapper walletApiWrapper = new WalletApiWrapper();
  private static final String filePath = "Address";
  private String assetId;
  private long amount;
  private int keyNum;
  private byte[] privateKey = null;

  public Air_drop() {
    Config config = Configuration.getByPath("config.conf");

    if (config.hasPath("assertId")) {
      this.assetId = config.getString("assertId");
    }

    if (config.hasPath("amount")) {
      this.amount = config.getLong("amount");
    }

    if (config.hasPath("privateKey")) {
      String priKey = config.getString("privateKey");
      privateKey = ByteArray.fromHexString(priKey);
    }

    if (config.hasPath("KeyNum")) {
      keyNum = config.getInt("KeyNum");
    }
  }

//  private void loadWallet() throws IOException, CipherException {
//    System.out.println("Please input your password.");
//    char[] password = Utils.inputPassword(false);
//
//    boolean result = walletApiWrapper.login(password);
//    StringUtils.clear(password);
//
//    if (result) {
//      logger.info("Login successful !!!");
//    } else {
//      logger.info("Login failed !!!");
//    }
//  }

  private void airDropAsset()
      throws IOException, CipherException, CancelException {
    File path = new File(filePath);
    if (!path.exists()) {
      throw new IOException("No directory");
    }
    File addressFile = new File(path, "address.txt");

    FileInputStream inputStream = null;
    InputStreamReader inputStreamReader = null;
    BufferedReader bufferedReader = null;
    try {
      inputStream = new FileInputStream(addressFile);
      inputStreamReader = new InputStreamReader(inputStream);
      bufferedReader = new BufferedReader(inputStreamReader);

      String address;
      while ((address = bufferedReader.readLine()) != null) {
        this.transferAsset(this.privateKey, address, this.assetId, this.amount);
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

  private boolean transferAsset(byte[] privateKey, String toAddress, String assertId, long amount)
      throws CipherException, IOException, CancelException {
    boolean result = walletApiWrapper.transferAsset(privateKey, toAddress, assertId, amount);
    if (result) {
      logger.info("transferAsset " + amount + " " + assertId + " to " + toAddress + " successful");
    } else {
      logger.info("transferAsset " + amount + " " + assertId + " to " + toAddress + " failed");
    }
    return result;
  }

  public static void main(String[] args) throws IOException, CipherException, CancelException {
    Air_drop air_drop = new Air_drop();
    air_drop.airDropAsset();
  }
}
