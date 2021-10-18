package algoserverless.model;

public class ResponseModel {

    private String txId;
    private String msg;

    public ResponseModel() {
    }

    public ResponseModel(String txId, String msg) {
        this.txId = txId;
        this.msg = msg;
    }

    public String getTxId() {
        return txId;
    }

    public void setTxId(String txId) {
        this.txId = txId;
    }

    public String getMsg() {
        return msg;
    }

    public void setMsg(String msg) {
        this.msg = msg;
    }
}
