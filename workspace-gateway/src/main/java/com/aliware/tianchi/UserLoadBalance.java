package com.aliware.tianchi;

import org.apache.dubbo.common.URL;
import org.apache.dubbo.rpc.Invocation;
import org.apache.dubbo.rpc.Invoker;
import org.apache.dubbo.rpc.RpcException;
import org.apache.dubbo.rpc.cluster.LoadBalance;

import com.aliware.tianchi.comm.ServerLoadInfo;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.concurrent.ThreadLocalRandom;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * @author daofeng.xjf
 *
 * 负载均衡扩展接口
 * 必选接口，核心接口
 * 此类可以修改实现，不可以移动类或者修改包名
 * 选手需要基于此类实现自己的负载均衡算法
 */
public class UserLoadBalance implements LoadBalance{
    
    
    @Override
    public <T> Invoker<T> select(List<Invoker<T>> invokers, URL url, Invocation invocation) throws RpcException {
        
        int size = invokers.size();
        // 总权重
        int totalWeight = 0;
        List<Integer> hasPermitArr = new ArrayList<>();
        List<Integer> weightArr = new ArrayList<>();
        int avgSpendTimeMaxIndex = -1;
        int clientTimeAvgSpend = 0;
        for(int index=0;index<size;index++){
            
            ServerLoadInfo serverLoadInfo = UserLoadBalanceService.getServerLoadInfo(invokers.get(index));
            if(serverLoadInfo != null){
                int clientTimeAvgSpendCurr = serverLoadInfo.getClientTimeAvgSpentTps();
                if(clientTimeAvgSpendCurr>clientTimeAvgSpend){
                    avgSpendTimeMaxIndex = index;
                    clientTimeAvgSpend = clientTimeAvgSpendCurr;
                }
            }
        }
        // 首先获取invoker对应的服务端耗时最大的索引
        for(int index=0;index<size;index++){
            Invoker<T> invoker = invokers.get(index);
            ServerLoadInfo serverLoadInfo = UserLoadBalanceService.getServerLoadInfo(invokers.get(index));
            AtomicInteger limiter = UserLoadBalanceService.getAtomicInteger(invoker);
            
            if(serverLoadInfo != null){
                int permits = limiter.get();
                int weight = serverLoadInfo.getWeight();
                if(permits > 0 ){
                    if(avgSpendTimeMaxIndex != index){
                        weight = weight+50;
                    }
                    hasPermitArr.add(index);
                    weightArr.add(weight);
                    totalWeight = totalWeight+weight;
                    
                }
                
            }
            
        }
        // 服务都被打满了,随机选一个
        if(hasPermitArr.size() == 0){
            SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
            String nowStr = sdf.format(new Date());
            System.out.println(nowStr+",服务器满负载");
            return invokers.get(ThreadLocalRandom.current().nextInt(invokers.size()));
        }
        // 根据服务端配置和平均耗时计算权重
        int offsetWeight = ThreadLocalRandom.current().nextInt(totalWeight);
        
        for(int i=0;i<hasPermitArr.size();i++){
            int index = hasPermitArr.get(i);
            int currentWeight = weightArr.get(i);
            offsetWeight  = offsetWeight - currentWeight;
            if (offsetWeight < 0) {
                return invokers.get(index);
            }
        }
        
        return invokers.get(ThreadLocalRandom.current().nextInt(invokers.size()));
    }
    
    
//    @Override
//    public <T> Invoker<T> select(List<Invoker<T>> invokers, URL url, Invocation invocation) throws RpcException {
//        
//        int size = invokers.size();
//        // 总权重
//        int avgSpendTimeMinIndex = -1;
//        int clientTimeAvgSpend = -1;
//        for(int index=0;index<size;index++){
//            
//            ServerLoadInfo serverLoadInfo = UserLoadBalanceService.getServerLoadInfo(invokers.get(index));
//            AtomicInteger limiter = UserLoadBalanceService.getAtomicInteger(invokers.get(index));
//            if(limiter == null || limiter.get()<=0){
//                continue;
//            }
//            if(serverLoadInfo != null){
//                int clientTimeAvgSpendCurr = serverLoadInfo.getClientTimeAvgSpentTps();
//                if(avgSpendTimeMinIndex == -1){
//                    // 初始化
//                    avgSpendTimeMinIndex = index;
//                    clientTimeAvgSpend = clientTimeAvgSpendCurr;
//                    continue;
//                }
//                if(clientTimeAvgSpendCurr<clientTimeAvgSpend){
//                    avgSpendTimeMinIndex = index;
//                    clientTimeAvgSpend = clientTimeAvgSpendCurr;
//                }
//            }
//        }
//        if(avgSpendTimeMinIndex != -1){
//            return invokers.get(avgSpendTimeMinIndex);
//        }
//        return invokers.get(ThreadLocalRandom.current().nextInt(invokers.size()));
//    }
}
