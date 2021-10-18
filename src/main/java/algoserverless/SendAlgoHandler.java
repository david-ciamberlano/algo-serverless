package algoserverless;

import algoserverless.model.RequestModel;
import algoserverless.model.ResponseModel;
import algoserverless.service.AlgoService;
import com.amazonaws.services.lambda.runtime.Context;
import com.amazonaws.services.lambda.runtime.LambdaLogger;
import com.amazonaws.services.lambda.runtime.RequestHandler;

import java.security.GeneralSecurityException;

public class SendAlgoHandler implements RequestHandler<RequestModel, ResponseModel> {

    public ResponseModel handleRequest(RequestModel request, Context context) {

        LambdaLogger logger = context.getLogger();

        AlgoService algoService = new AlgoService(
                    System.getenv("CORE_API_ADDR"),
                    Integer.parseInt(System.getenv("CORE_API_PORT")),
                    System.getenv("CORE_API_TOKEN"),
                    System.getenv("INDEXER_API_ADDR"),
                    Integer.parseInt(System.getenv("INDEXER_API_PORT")));

        try {
            algoService.initAccount(System.getenv("ACC_PASSPHRASE"), System.getenv("ACC_ADDR"));
            logger.log("Algoservice init ok");

            String txId = algoService.sendAlgo(request.getAddress(), request.getAmount());
            logger.log("Transaction executed");

            return new ResponseModel(txId,"Transaction executed");
        }
        catch (Exception e) {
            logger.log("Transaction Failed");
            return new ResponseModel("", "Transaction Failed");
        }

    }
}
