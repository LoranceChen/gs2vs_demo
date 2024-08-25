package gs2vs.core.utils;

import gs2vs.core.wsserver.WsContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import scala.Tuple2;

import java.util.function.Supplier;

public class DebugUtils {
    static Logger logger = LoggerFactory.getLogger(DebugUtils.class);

    /**
     * // 35161301, 40950300, 1282600 ==> 77406800
     * private def timeCheck[T](actionName: String, ops: => T): (T, Long) = {
     * val beginTime = System.nanoTime()
     * val rst = ops
     * (rst, System.nanoTime() - beginTime)
     * }
     */
    public static <T> Tuple2<T, Long> timeCheck(String actionName, Supplier<T> ops) {
        var beginTime = System.nanoTime();
        var rst = ops.get();
        var endTime = System.nanoTime() - beginTime;
        WsContext wsContext = WsContext.getUser();
        logger.debug("[TIME_COST_DEBUG] [%s] %s: %dns".formatted(wsContext.getMsgId(), actionName, endTime));
        return new Tuple2<>(rst, endTime);
    }

}
