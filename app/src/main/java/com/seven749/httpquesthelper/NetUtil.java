package com.seven749.httpquesthelper;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.Map;
import java.util.concurrent.Executors;
import java.util.concurrent.LinkedBlockingDeque;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

public class NetUtil {

    //使用静态内部类的单例模式来创建网络请求专用的线程池,线程安全,同时达到了懒加载效果

    private static class Holder{
        private final static NetUtil INSTANCE = new NetUtil();

    }

    public static NetUtil getInstance(){
        return Holder.INSTANCE;
    }

    /**
     * ThreadPoolExecutor的构造方法中的参数含义
     */
    //核心线程数量,同时能够执行的线程数量
    private int corePoolSize;
    //最大线程的数量,能够容纳的最大排队数
    private int maxPoolSize;
    //正在排队的任务的最长的排队时间
    private long keepAliveTime = 30;
    //正在排队的任务的最长排队时间的单位
    private TimeUnit timeUnit = TimeUnit.MINUTES;
    //线程池对象
    private ThreadPoolExecutor executor;

    //将构造方法设为私有,故无法从其他方式创建对象,构成单例
    private NetUtil() {
        //获取当前设备可用的核心处理器核心数*2+1,赋值给核心线程的数量,能使性能达到极致
        corePoolSize = Runtime.getRuntime().availableProcessors()*2+1;
        //30应该够......
        maxPoolSize = 30;
        //初始化线程池
        executor = new ThreadPoolExecutor(
                corePoolSize,
                maxPoolSize,
                keepAliveTime,
                timeUnit,
                //缓冲队列,存放排队的任务,类似栈结构
                new LinkedBlockingDeque<Runnable>(),
                //创建线程的工厂(工厂模式?
                Executors.defaultThreadFactory(),
                //处理那些超出最大线程数量的策略
                new ThreadPoolExecutor.AbortPolicy()
        );
    }

    //下面的这个方法的参数我已经去掉,需要自己添加应该需要的参数
    public void execute(final Request request, final CallBack callBack) {
        executor.execute(new Runnable() {
            @Override
            public void run() {
                //这里就是子线程,可以进行IO操作
                if (request.getMethod().equals("POST")) {
                    POST(request, callBack);
                } else if (request.getMethod().equals("GET")) {
                    GET(request, callBack);
                }
            }
        });
    }
    void GET(Request request, CallBack callBack) {
        HttpURLConnection httpURLConnection = null;
        try {
            // 1. 得到访问地址的URL
            URL url = new URL(request.getUrl());
            // 2. 得到网络访问对象java.net.HttpURLConnection
            httpURLConnection = (HttpURLConnection) url.openConnection();
            /* 3. 设置请求参数（过期时间，输入、输出流、访问方式），以流的形式进行连接 */
            // 设置是否向HttpURLConnection输出
            httpURLConnection.setDoOutput(false);
            // 设置是否从httpUrlConnection读入
            httpURLConnection.setDoInput(true);
            // 设置请求方式　默认为GET
            httpURLConnection.setRequestMethod("GET");
            // 设置是否使用缓存
            httpURLConnection.setUseCaches(true);
            // 设置此 HttpURLConnection 实例是否应该自动执行 HTTP 重定向
            httpURLConnection.setInstanceFollowRedirects(true);
            // 设置超时时间
            httpURLConnection.setConnectTimeout(3000);
            // 连接
            httpURLConnection.connect();
            // 5. 如果返回值正常，数据在网络中是以流的形式得到服务端返回的数据
            BufferedReader reader = new BufferedReader(new InputStreamReader(httpURLConnection.getInputStream()));
            StringBuffer response = new StringBuffer();
            String line = null;
            while ((line = reader.readLine()) != null) { // 循环从流中读取
                response.append(line);
            }
            if (callBack != null) {
                callBack.onResponse(response.toString());
            }
            reader.close(); // 关闭流

        } catch (IOException e) {
            callBack.onFailed(e);
        }finally {
            // 6. 断开连接，释放资源
            if (null != httpURLConnection){
                try {
                    httpURLConnection.disconnect();
                }catch (Exception e){
                    e.printStackTrace();
                }
            }
        }
    }

    void POST(Request request, CallBack callBack) {
        HttpURLConnection httpURLConnection = null;
        try {
            // 1. 获取访问地址URL
            URL url = new URL(request.getUrl());
            // 2. 创建HttpURLConnection对象
            httpURLConnection = (HttpURLConnection) url.openConnection();
            /* 3. 设置请求参数等 */
            // 请求方式  默认 GET
            httpURLConnection.setRequestMethod("POST");
            // 超时时间
            httpURLConnection.setConnectTimeout(3000);
            // 设置是否输出
            httpURLConnection.setDoOutput(true);
            // 设置是否读入
            httpURLConnection.setDoInput(true);
            // 设置是否使用缓存
            httpURLConnection.setUseCaches(false);
            // 设置此 HttpURLConnection 实例是否应该自动执行 HTTP 重定向
            httpURLConnection.setInstanceFollowRedirects(true);
            // 设置请求头
            httpURLConnection.addRequestProperty("sysId","sysId");
            // 设置使用标准编码格式编码参数的名-值对
            httpURLConnection.setRequestProperty("Content-Type", "application/x-www-form-urlencoded");
            // 连接
            httpURLConnection.connect();
            /* 4. 处理输入输出 */
            // 写入参数到请求中
            StringBuffer params = new StringBuffer();
            Map<String, Object> hashMap = request.getHashMap();
            for (String key : hashMap.keySet()) {
                params.append(key).append("=").append(hashMap.get(key)).append("&");
            }
            OutputStream out = httpURLConnection.getOutputStream();
            out.write(params.toString().getBytes());
            // 简化
            //httpURLConnection.getOutputStream().write(params.getBytes());
            out.flush();
            out.close();
            // 从连接中读取响应信息
            BufferedReader reader = new BufferedReader(
                    new InputStreamReader(httpURLConnection.getInputStream()));
            StringBuffer response = new StringBuffer();
            String line;
            while ((line = reader.readLine()) != null) {
                response.append(line);
            }
            if (callBack != null) {
                callBack.onResponse(response.toString());
            }
            reader.close();
        } catch (IOException e) {
            callBack.onFailed(e);
        }finally {
            // 5. 断开连接
            if (null != httpURLConnection){
                try {
                    httpURLConnection.disconnect();
                }catch (Exception e){
                    e.printStackTrace();
                }
            }
        }
    }
}
