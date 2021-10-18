package algoserverless.model;

public class RequestModel {

    String address;
    Long amount;

    public RequestModel() {
    }

    public RequestModel(String address, Long amount) {
        this.address = address;
        this.amount = amount;
    }

    public String getAddress() {
        return address;
    }

    public void setAddress(String address) {
        this.address = address;
    }

    public Long getAmount() {
        return amount;
    }

    public void setAmount(Long amount) {
        this.amount = amount;
    }
}
