package org.example;

import com.hedera.hashgraph.sdk.Client;
import com.hedera.hashgraph.sdk.ContractCreateTransaction;
import com.hedera.hashgraph.sdk.ContractId;
import com.hedera.hashgraph.sdk.FileAppendTransaction;
import com.hedera.hashgraph.sdk.FileCreateTransaction;
import com.hedera.hashgraph.sdk.Hbar;
import com.hedera.hashgraph.sdk.PrecheckStatusException;
import com.hedera.hashgraph.sdk.PublicKey;
import com.hedera.hashgraph.sdk.ReceiptStatusException;
import com.hedera.hashgraph.sdk.TransactionReceipt;
import com.hedera.hashgraph.sdk.TransactionResponse;

import java.io.File;
import java.io.IOException;
import java.net.URISyntaxException;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.time.Duration;
import java.time.Instant;
import java.util.Arrays;
import java.util.Enumeration;
import java.util.concurrent.TimeoutException;

public class ContractCreator {
    private static final int MAX_FILE_SIZE = 4000;

    public ContractId create(String contractName, Client client, AccountWrapper accountWrapper)
            throws PrecheckStatusException, TimeoutException, ReceiptStatusException {
        // upload file (chunk if necessary):
        byte[] contract = getBytesFromFile(contractName);
        TransactionReceipt txReceipt_file = null;
        PublicKey myPublicKey = accountWrapper.getPublicKey();
        int nIters = (int) Math.ceil(contract.length / MAX_FILE_SIZE);
        System.out.println("File is " + String.format("%.2f", ((float) contract.length / 1000))
                + " kB long");
        System.out.println(nIters + " chunks will be uploaded");

        for (int i = 0; i <= nIters; i++) {
            int from = MAX_FILE_SIZE * i;
            int to = (i == nIters) ? from + contract.length % MAX_FILE_SIZE : MAX_FILE_SIZE * (i + 1);

            byte[] segment = Arrays.copyOfRange(contract, from, to);

            if (i == 0) {
                var tx_file = new FileCreateTransaction();
                tx_file.setContents(segment);
                tx_file.setKeys(myPublicKey);
                tx_file.setExpirationTime(Instant.now().plus(Duration.ofSeconds(7890000)));
                tx_file.setMaxTransactionFee(new Hbar(30));

                TransactionResponse txId_file = tx_file.execute(client);
                txReceipt_file = txId_file.getReceipt(client);
                // print out the result:
                System.out.println("Result of file save:");
                System.out.println(txReceipt_file.status);
            } else {
                // append chunk to existing file
                System.out.println("Appending chunk: " + i + " (" + segment.length + " bytes)");
                new FileAppendTransaction().setFileId(txReceipt_file.fileId)
                        .setMaxTransactionFee(new Hbar(30))
                        .setContents(segment).execute(client);
            }
        }
//Deploy the contract
        ContractCreateTransaction contractTx = new ContractCreateTransaction()
                .setBytecodeFileId(txReceipt_file.fileId)
                .setGas(2_000_000)
                .setAdminKey(myPublicKey);
//Submit the transaction to the Hedera test network
        TransactionResponse contractResponse = contractTx.execute(client);
//Get the receipt of the file create transaction
        TransactionReceipt contractReceipt = contractResponse.getReceipt(client);
        ContractId newContractId = contractReceipt.contractId;

        System.out.println("The smart contract ID is " + newContractId);
        return newContractId;
    }

    private byte[] getBytesFromFile(String fileName) {
        byte[] content = null;
        try {
            URL resources = getClass()
                    .getClassLoader()
                    .getResource(fileName.trim().concat(".bin"));
            if (resources == null)
                throw new IllegalArgumentException("file not found!");

            File contractFile = new File(resources.toURI());
            content = Files.readAllBytes(contractFile.toPath());
            System.out.println(new String(content));
        } catch (IOException e) {
            e.printStackTrace();
        } catch (URISyntaxException e) {
            throw new RuntimeException(e);
        }
        return content;
    }
}
