package org.tron.demo;

import org.tron.walletserver.WalletApi;
import org.tron.protos.Protocol.Block;


public class GetBlock {
    public static void main(String[] args) {
        Block block = WalletApi.getBlock(100);
        System.out.println(block);

    }
}
