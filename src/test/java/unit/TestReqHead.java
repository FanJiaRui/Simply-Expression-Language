package unit;


public class TestReqHead {

    private String serialId;

    private String date;

    public String getSerialId() {
        return serialId;
    }

    public void setSerialId(String serialId) {
        this.serialId = serialId;
    }

    public String getDate() {
        return date;
    }

    public void setDate(String date) {
        this.date = date;
    }

    @Override
    public String toString() {
        return "TestReqHead{" +
                "serialId='" + serialId + '\'' +
                ", date='" + date + '\'' +
                '}';
    }
}
