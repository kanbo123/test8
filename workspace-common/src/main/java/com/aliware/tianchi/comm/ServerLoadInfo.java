package com.aliware.tianchi.comm;

import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicLong;

/**
 * 
 * @desc: <服务端负载信息>
 * @author: KANBO  
 * @date:2019年6月22日 下午1:37:53
 */
public class ServerLoadInfo {
    
    private String quota = null;
    // 等于服务端线程数量
    private int providerThread = 0;
    // 权重
    private volatile int weight = 0;
    // 请求总数(上一个5秒)
    private AtomicLong reqCount = new AtomicLong(0);
    // 当前任务数量
    private AtomicLong activeCount = new AtomicLong(0);
    // 总耗时(上一个5秒)
    private AtomicLong spendTimeTotal = new AtomicLong(0);
    // 上次请求失败时间
    private long lastFailTime;
    private int avgSpendTime;
    
    //
    private AtomicLong clientTimeSpentTotalTps = new AtomicLong(0); 
    private AtomicInteger clientReqCount = new AtomicInteger(0);
    private volatile int clientTimeAvgSpentTps = 0;
    private volatile long clientLastAvgTime = System.currentTimeMillis();
    private AtomicBoolean clientLastAvgTimeFlag = new AtomicBoolean(false);
    public ServerLoadInfo(){
        
    }
    public ServerLoadInfo(String quota,int providerThread){
        
        this.quota = quota;
        this.providerThread = providerThread;
        this.weight = this.providerThread-50;
//            if("small".equals(quota)){
//                this.weight = 2;
//            }else if("medium".equals(quota)){
//                this.weight = 5;
//            }else if("large".equals(quota)){
//                this.weight = 8;
//            }else{
//                this.weight = 1;
//            }
    }
    
    public String getQuota() {
    
        return quota;
    }

    public void setQuota(String quota) {
    
        this.quota = quota;
    }

    public int getWeight() {
    
        return weight;
    }
    
    public void setWeight(int weight) {
    
        this.weight = weight;
    }
    
    public AtomicLong getReqCount() {
    
        return reqCount;
    }
    
    public void setReqCount(AtomicLong reqCount) {
    
        this.reqCount = reqCount;
    }
    
    public AtomicLong getActiveCount() {
    
        return activeCount;
    }
    
    public void setActiveCount(AtomicLong activeCount) {
    
        this.activeCount = activeCount;
    }
    
    public AtomicLong getSpendTimeTotal() {
    
        return spendTimeTotal;
    }
    
    public void setSpendTimeTotal(AtomicLong spendTimeTotal) {
    
        this.spendTimeTotal = spendTimeTotal;
    }
    
    public long getLastFailTime() {
    
        return lastFailTime;
    }
    
    public void setLastFailTime(long lastFailTime) {
    
        this.lastFailTime = lastFailTime;
    }

    
    public int getAvgSpendTime() {
    
        return avgSpendTime;
    }

    
    public void setAvgSpendTime(int avgSpendTime) {
    
        this.avgSpendTime = avgSpendTime;
    }
    
    public int getProviderThread() {
    
        return providerThread;
    }
    
    public void setProviderThread(int providerThread) {
    
        this.providerThread = providerThread;
    }
    
    
    public AtomicLong getClientTimeSpentTotalTps() {
    
        return clientTimeSpentTotalTps;
    }
    
    public void setClientTimeSpentTotalTps(AtomicLong clientTimeSpentTotalTps) {
    
        this.clientTimeSpentTotalTps = clientTimeSpentTotalTps;
    }
    
    
    public int getClientTimeAvgSpentTps() {
    
        return clientTimeAvgSpentTps;
    }
    
    public void setClientTimeAvgSpentTps(int clientTimeAvgSpentTps) {
    
        this.clientTimeAvgSpentTps = clientTimeAvgSpentTps;
    }
    
    public AtomicInteger getClientReqCount() {
    
        return clientReqCount;
    }
    
    public void setClientReqCount(AtomicInteger clientReqCount) {
    
        this.clientReqCount = clientReqCount;
    }
    
    public long getClientLastAvgTime() {
    
        return clientLastAvgTime;
    }
    
    public void setClientLastAvgTime(long clientLastAvgTime) {
    
        this.clientLastAvgTime = clientLastAvgTime;
    }
    
    public AtomicBoolean getClientLastAvgTimeFlag() {
    
        return clientLastAvgTimeFlag;
    }
    
    public void setClientLastAvgTimeFlag(AtomicBoolean clientLastAvgTimeFlag) {
    
        this.clientLastAvgTimeFlag = clientLastAvgTimeFlag;
    }
    
    
}
