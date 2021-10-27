package algoserverless.service;

import com.algorand.algosdk.account.Account;
import com.algorand.algosdk.crypto.Address;
import com.algorand.algosdk.transaction.SignedTransaction;
import com.algorand.algosdk.util.Encoder;
import com.algorand.algosdk.v2.client.common.AlgodClient;
import com.algorand.algosdk.v2.client.common.IndexerClient;
import com.algorand.algosdk.v2.client.common.Response;
import com.algorand.algosdk.v2.client.model.NodeStatusResponse;
import com.algorand.algosdk.v2.client.model.PendingTransactionResponse;
import com.algorand.algosdk.v2.client.model.PostTransactionsResponse;
import com.algorand.algosdk.v2.client.model.TransactionParametersResponse;

import java.math.BigDecimal;
import java.security.GeneralSecurityException;
import java.security.NoSuchAlgorithmException;
import java.util.Optional;

public class AlgoService {

    private AlgodClient algodClient;
    private IndexerClient indexerClient;

    private Account algoAccount;
    private Address algoAddress;

    public AlgoService(String algodApiAddr, Integer algodPort, String algodApiToken,
                       String indexerApiAddr, Integer indexerApiPort) {
        algodClient = new AlgodClient(algodApiAddr, algodPort, algodApiToken);
        indexerClient = new IndexerClient(indexerApiAddr, indexerApiPort);
    }

    public AlgoService(String algodApiAddr, Integer algodPort, String algodApiToken, String indexerApiAddr,
                       Integer indexerApiPort, String accPassphrase) {
        algodClient = new AlgodClient(algodApiAddr, algodPort, algodApiToken);
        indexerClient = new IndexerClient(indexerApiAddr, indexerApiPort);

        try {
            algoAccount = new Account(accPassphrase);
            algoAddress = algoAccount.getAddress();
        }
        catch (GeneralSecurityException e) {
            throw new RuntimeException(e.getMessage());
        }
    }

    /**
     * get the Algos owned by address
     * @param address
     * @return
     */
    public Optional<Long> getAccountAmount(String address) {

        Response<com.algorand.algosdk.v2.client.model.Account> accountResponse;
        try {
            Address destAddress = new Address(address);
            accountResponse = algodClient.AccountInformation(destAddress).execute();
        }
        catch (Exception e) {
            return Optional.empty();
        }

        if (accountResponse.isSuccessful()) {
            return Optional.of(accountResponse.body().amount);
        }
        else {
            return Optional.empty();
        }
    }


    /**
     * Send amount (il Algo) to receiverAddress.
     * @param receiverAddress
     * @param amount
     * @return
     * @throws Exception
     */
    public String sendAlgo(String receiverAddress, Long amount) throws Exception {

        Address address = new Address(receiverAddress);
        String note = "AlgoServerless Test";
        TransactionParametersResponse params = algodClient.TransactionParams().execute().body();
        com.algorand.algosdk.transaction.Transaction tx =
                com.algorand.algosdk.transaction.Transaction.PaymentTransactionBuilder()
                        .sender(algoAddress)
                        .note(note.getBytes())
                        .amount(amount)
                        .receiver(address)
                        .suggestedParams(params)
                        .build();

        SignedTransaction signedTx = algoAccount.signTransaction(tx);
        byte[] encodedSignedTx = Encoder.encodeToMsgPack(signedTx);

        Response<PostTransactionsResponse> txResponse = algodClient.RawTransaction().rawtxn(encodedSignedTx).execute();

        if (txResponse.isSuccessful()) {
            String txId = txResponse.body().txId;
            //wait for confirmation
            waitForConfirmation(txId, 6);
            return txId;
        } else {
            throw new Exception("Transaction Error");
        }
    }


    public void waitForConfirmation(String txId, int timeout) throws Exception {

        Long txConfirmedRound = -1L;
        Response<NodeStatusResponse> statusResponse = algodClient.GetStatus().execute();

        long lastRound;
        if (statusResponse.isSuccessful()) {
            lastRound = statusResponse.body().lastRound + 1L;
        }
        else {
            throw new IllegalStateException("Cannot get node status");
        }

        long maxRound = lastRound + timeout;

        for (long currentRound = lastRound; currentRound < maxRound; currentRound++) {
            Response<PendingTransactionResponse> response = algodClient.PendingTransactionInformation(txId).execute();

            if (response.isSuccessful()) {
                txConfirmedRound = response.body().confirmedRound;
                if (txConfirmedRound == null) {
                    if (!algodClient.WaitForBlock(currentRound).execute().isSuccessful()) {
                        throw new Exception();
                    }
                }
                else {
                    return;
                }
            } else {
                throw new IllegalStateException("The transaction has been rejected");
            }
        }

        throw new IllegalStateException("Transaction not confirmed after %1" + timeout + " rounds!");
    }


}
