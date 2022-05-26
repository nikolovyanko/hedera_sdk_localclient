package org.example;

import com.hedera.hashgraph.sdk.AccountId;
import com.hedera.hashgraph.sdk.AccountInfo;
import com.hedera.hashgraph.sdk.AccountInfoQuery;
import com.hedera.hashgraph.sdk.Client;
import com.hedera.hashgraph.sdk.Hbar;
import com.hedera.hashgraph.sdk.PrecheckStatusException;
import com.hedera.hashgraph.sdk.PrivateKey;
import com.hedera.hashgraph.sdk.PublicKey;
import io.github.cdimascio.dotenv.Dotenv;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.TimeoutException;

public class AccountWrapper {
    private AccountId accountId;
    private PrivateKey privateKey;
    private PublicKey publicKey;
    private static AccountWrapper instance = null;
    public static final AccountId NODE_ID = new AccountId(3);

    private AccountWrapper(HEDERA_NETWORK network) {
        init(network);
    }

    private void init(HEDERA_NETWORK network) {
        switch (network) {
            case PREVIEW_NET -> {
                accountId = AccountId.fromString(Dotenv.load().get("PREVIEW_NET_ACCOUNT_ID"));
                privateKey = PrivateKey.fromString(Dotenv.load().get("PREVIEW_NET_PRIVATE_KEY"));
                publicKey = PublicKey.fromString(Dotenv.load().get("PREVIEW_NET_PUBLIC_KEY"));
            }
            case TESTNET -> {
                accountId = AccountId.fromString(Dotenv.load().get("TESTNET_ACCOUNT_ID"));
                privateKey = PrivateKey.fromString(Dotenv.load().get("TESTNET_PRIVATE_KEY"));
                publicKey = PublicKey.fromString(Dotenv.load().get("TESTNET_PUBLIC_KEY"));
            }
            case LOCAL -> {
                accountId = AccountId.fromString(Dotenv.load().get("LOCAL_NET_ACCOUNT_ID"));
                privateKey = PrivateKey.fromString(Dotenv.load().get("LOCAL_NET_PRIVATE_KEY"));
                publicKey = PublicKey.fromString(Dotenv.load().get("LOCAL_NET_PUBLIC_KEY"));
            }

        }

    }

    public AccountId getAccountId() {
        return accountId;
    }

    public PrivateKey getPrivateKey() {
        return privateKey;
    }

    public PublicKey getPublicKey() {
        return publicKey;
    }

    public Hbar getAccountBalance(Client client) throws PrecheckStatusException, TimeoutException {
        return getAccountInfo(client).balance;
    }

    public AccountInfo getAccountInfo(Client client) throws PrecheckStatusException, TimeoutException {
        return new AccountInfoQuery()
                .setAccountId(new AccountId(1))
                .execute(client);
    }


    public static AccountWrapper forTestNet() {
        if (instance == null)
            instance = new AccountWrapper(HEDERA_NETWORK.TESTNET);

        return instance;
    }

    public static AccountWrapper forPreviewNet() {
        if (instance == null)
            instance = new AccountWrapper(HEDERA_NETWORK.PREVIEW_NET);

        return instance;
    }

    public static AccountWrapper forLocalNode() {
        if (instance == null)
            instance = new AccountWrapper(HEDERA_NETWORK.LOCAL);

        return instance;
    }

    public Map<String, AccountId> node(){
        Map<String, AccountId> node = new HashMap();
        node.put("127.0.0.1:50211", NODE_ID);
        return  node;
    }
    //        TransactionResponse sendHbar = new TransferTransaction()
//                .addHbarTransfer(myAccountId, Hbar.from(-1))
//                .addHbarTransfer(AccountId.fromString("0.0.3558"), Hbar.from(1))
//                .execute(client);
//
//        System.out.println("The transfer transaction was: " +sendHbar.getReceipt(client));
//        AccountBalance accountBalanceNew = new AccountBalanceQuery()
//                .setAccountId(myAccountId)
//                .execute(client);
//
}
