package unit.functions;

import net.fanjr.simplify.el.ELException;
import net.fanjr.simplify.el.ELMethod;

class MyFunctions {

    public static void noReturnNoParamFun() {
        System.out.println("hello noReturnNoParamFun");
    }

    public static void noReturnOneParamFun(int a) {
        System.out.println("hello noReturnOneParamFun");
        System.out.println("a=" + a);
    }

    @ELMethod(order = Integer.MAX_VALUE)
    public static void noReturnTwoParamFun(int a, int b) {
        throw new ELException("不应该调用低优先级的方法");
    }


    public static String strReturnNoParamFun() {
        throw new ELException("不应该调用低优先级的方法");
    }

    @ELMethod(order = -2, functionName = "strReturnNoParamFun")
    public static String strReturnNoParamFun2() {
        return "hello";
    }

    public static String strReturnOneParamFun(String a) {
        return "hello " + a;
    }


}
