package com.aliware.tianchi;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.concurrent.atomic.AtomicInteger;

import org.apache.dubbo.common.Constants;
import org.apache.dubbo.common.extension.Activate;
import org.apache.dubbo.rpc.AsyncRpcResult;
import org.apache.dubbo.rpc.Filter;
import org.apache.dubbo.rpc.Invocation;
import org.apache.dubbo.rpc.Invoker;
import org.apache.dubbo.rpc.Result;
import org.apache.dubbo.rpc.RpcException;

import com.aliware.tianchi.comm.ServerLoadInfo;

/**
 * @author daofeng.xjf
 *
 * 客户端过滤器
 * 可选接口
 * 用户可以在客户端拦截请求和响应,捕获 rpc 调用时产生、服务端返回的已知异常。
 */
@Activate(group = Constants.CONSUMER)
public class TestClientFilter implements Filter {
    
    long avgTime = 1000;
    @Override
    public Result invoke(Invoker<?> invoker, Invocation invocation) throws RpcException {
        
            AtomicInteger limiter = UserLoadBalanceService.getAtomicInteger(invoker);
            ServerLoadInfo serverLoadInfo = UserLoadBalanceService.getServerLoadInfo(invoker);
            if(limiter == null){
                return invoker.invoke(invocation);
            }
            long startTime = System.currentTimeMillis();
            //并发数-1
            limiter.decrementAndGet();
            Result result = invoker.invoke(invocation);
            if(result instanceof AsyncRpcResult){
                AsyncRpcResult asyncResult = (AsyncRpcResult) result;
                asyncResult.getResultFuture().whenComplete((actual, t) -> {
                    // 服务端可用线程数+1
                    limiter.incrementAndGet();
                    long endTime = System.currentTimeMillis();
                    long spend = endTime - startTime;
                    // 计算耗时
                    serverLoadInfo.getClientTimeSpentTotalTps().addAndGet(spend);
                    //客户端请求数+1
                    int currCount = serverLoadInfo.getClientReqCount().incrementAndGet();
                    long clientLastAvgTime = serverLoadInfo.getClientLastAvgTime();
                    if(endTime - clientLastAvgTime >= avgTime){
                        if(serverLoadInfo.getClientLastAvgTimeFlag().compareAndSet(false, true)){
                            
                           clientLastAvgTime = serverLoadInfo.getClientLastAvgTime();
                           if(endTime - clientLastAvgTime >= avgTime){
                            // 计算耗时
                               int avg = serverLoadInfo.getClientTimeSpentTotalTps().intValue() / currCount;
                               serverLoadInfo.setClientTimeAvgSpentTps(avg);
                               // 重置
                               serverLoadInfo.getClientReqCount().set(0);
                               serverLoadInfo.getClientTimeSpentTotalTps().set(0);
                               serverLoadInfo.setClientLastAvgTime(endTime);
                               serverLoadInfo.getClientLastAvgTimeFlag().set(false); 
                               SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
                               String nowStr = sdf.format(new Date());
                               System.out.println(String.format("每秒统计数据,时间:%s,环境:%s,可用线程数:%s,请求数:%s,平均耗时:%s",
                                   nowStr,
                                   serverLoadInfo.getQuota(), 
                                   limiter.get(),
                                   currCount ,
                                   avg));
                           }
                        }
                    }
                });
            }
            return result;
    }

    @Override
    public Result onResponse(Result result, Invoker<?> invoker, Invocation invocation) {
        
        return result;
    }
}
