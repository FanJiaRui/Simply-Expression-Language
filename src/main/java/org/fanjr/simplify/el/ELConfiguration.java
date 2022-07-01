package org.fanjr.simplify.el;

import com.hundsun.gaps.flowexecutor.configuration.GapsConfigurationHolder;
import com.hundsun.gaps.flowexecutor.manager.GapsFlowLogManager;
import org.slf4j.Logger;
import org.springframework.beans.factory.annotation.Value;

/**
 * @author fanjr15662@hundsun.com
 * @file ELConfiguration.java
 * @since 2022/1/24 上午10:14
 */
public class ELConfiguration {

    private static final Logger logger = GapsFlowLogManager.getLogger();
    private static final InnerUtil INNER_UTIL;

    static {
        INNER_UTIL = new InnerUtil();
        GapsConfigurationHolder.addJavaBean(INNER_UTIL);
    }


    public static boolean isEqPrecision() {
        return INNER_UTIL.eqPrecision;
    }

    public static boolean isPlusMosaics() {
        return INNER_UTIL.plusMosaics;
    }

    public static void elStatus() {
        //skip
    }

    /**
     * 内部初始化工具类
     */
    private static class InnerUtil {

        /**
         * ==比较数值精度
         */
        private boolean eqPrecision;
        /**
         * 加号支持字符串拼接
         */
        private boolean plusMosaics;

        @Value("${gaps.el.number.eqPrecision:false}")
        public void setEqPrecision(boolean eqPrecision) {
            this.eqPrecision = eqPrecision;
            if (eqPrecision) {
                logger.info("El引擎：打开'=='和'!='运算符数值精度比较");
            } else {
                logger.info("El引擎：关闭'=='和'!='运算符数值精度比较");
            }
        }

        @Value("${gaps.el.number.plusMosaics:false}")
        public void setPlusMosaics(boolean plusMosaics) {
            this.plusMosaics = plusMosaics;
            if (plusMosaics) {
                logger.info("El引擎：打开'+'运算符字符串拼接");
            } else {
                logger.info("El引擎：关闭'+'运算符字符串拼接");
            }
        }
    }
}
