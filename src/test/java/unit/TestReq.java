package unit;

public class TestReq {

    private TestReqHead head;

    private TestReqBody body;

    public TestReqHead getHead() {
        return head;
    }

    public TestReqBody getBody() {
        return body;
    }

    @Override
    public String toString() {
        return "TestReq{" +
                "head=" + head +
                ", body=" + body +
                '}';
    }
}
