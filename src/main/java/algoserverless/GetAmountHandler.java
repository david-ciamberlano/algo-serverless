package algoserverless;

import algoserverless.service.AlgoService;
import com.amazonaws.services.lambda.runtime.Context;
import com.amazonaws.services.lambda.runtime.LambdaLogger;
import com.amazonaws.services.lambda.runtime.RequestHandler;

import java.security.NoSuchAlgorithmException;

public class GetAmountHandler implements RequestHandler<String, Long> {

    public Long handleRequest(final String address, final Context context) {

        LambdaLogger logger = context.getLogger();

        AlgoService algoService;
        algoService = new AlgoService(
                System.getenv("CORE_API_ADDR"),
                Integer.parseInt(System.getenv("CORE_API_PORT")),
                System.getenv("CORE_API_TOKEN"),
                System.getenv("INDEXER_API_ADDR"),
                Integer.parseInt(System.getenv("INDEXER_API_PORT")));

        logger.log("Algoservice init ok\n");

        Long amount = algoService.getAccountAmount(address).orElse(-1L);


        logger.log("Amount: " + amount + "\n");

        return amount;
    }

}
