package unit;

public class MyFunctions {

    public static void noReturnNoParamFun() {
        System.out.println("hello noReturnNoParamFun");
    }

    public static void noReturnOneParamFun(int a) {
        System.out.println("hello noReturnOneParamFun");
        System.out.println("a=" + a);
    }

    public static void noReturnTwoParamFun(int a, int b) {
        System.out.println("hello noReturnOneParamFun");
        System.out.println("a=" + a);
        System.out.println("b=" + b);
    }

    public static String strReturnNoParamFun() {
        return "hello";
    }

    public static String strReturnOneParamFun(String a) {
        return "hello " + a;
    }


}
