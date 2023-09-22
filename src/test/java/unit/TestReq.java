package unit;

public class TestReq {

    public void setHead(TestReqHead head) {
        this.head = head;
    }

    public void setBody(TestReqBody body) {
        this.body = body;
    }

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
