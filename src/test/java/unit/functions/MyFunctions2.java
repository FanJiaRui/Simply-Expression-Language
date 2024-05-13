package unit.functions;

import org.fanjr.simplify.el.ELMethod;
import org.fanjr.simplify.el.ElException;

class MyFunctions2 {

    @ELMethod(order = Integer.MAX_VALUE)
    public static void noReturnOneParamFun(int a) {
        throw new ElException("不应该调用低优先级的方法");
    }


    public static void noReturnTwoParamFun(int a, int b) {
        System.out.println("hello noReturnOneParamFun");
        System.out.println("a=" + a);
        System.out.println("b=" + b);
    }
}
