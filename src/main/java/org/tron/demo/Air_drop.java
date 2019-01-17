package org.tron.demo;

import java.io.IOException;
import java.util.Arrays;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.tron.api.GrpcAPI.EasyTransferResponse;
import org.tron.common.crypto.ECKey;
import org.tron.common.crypto.Sha256Hash;
import org.tron.common.utils.ByteArray;
import org.tron.common.utils.Utils;
import org.tron.core.exception.CancelException;
import org.tron.core.exception.CipherException;
import org.tron.keystore.StringUtils;
import org.tron.protos.Protocol.Transaction;
import org.tron.walletcli.WalletApiWrapper;
import org.tron.walletserver.WalletApi;

public class Air_drop {
  private static final Logger logger = LoggerFactory.getLogger("Client");
  private WalletApiWrapper walletApiWrapper = new WalletApiWrapper();

  private void loadWallet() throws IOException, CipherException {
    System.out.println("Please input your password.");
    char[] password = Utils.inputPassword(false);

    boolean result = walletApiWrapper.login(password);
    StringUtils.clear(password);

    if (result) {
      System.out.println("Login successful !!!");
    } else {
      System.out.println("Login failed !!!");
    }
  }

  private static void loadFile() {

  }

  private boolean transferAsset(String toAddress, String assertId, long amount)
      throws CipherException, IOException, CancelException {
    boolean result = walletApiWrapper.transferAsset(toAddress, assertId, amount);
    if (result) {
      logger.info("transferAsset " + amount + " " + assertId + " successful");
    } else {
      logger.info("transferAsset " + amount + " " + assertId + " failed");
    }
    return result;
  }

  public static void main(String[] args) throws IOException, CipherException, CancelException {
    Air_drop air_drop = new Air_drop();
    air_drop.loadWallet();

    air_drop.transferAsset("TTbioAsbefqWxtoGk3MsKkUw7jgtFKPH9E", "1001377", 100);
  }
}
