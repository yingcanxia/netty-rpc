package cn.shadow.netty.rpc.registry;

import java.io.File;
import java.lang.reflect.Method;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import cn.shadow.netty.rpc.protocol.InvokerProtocol;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;

public class RegistryHandler extends ChannelInboundHandlerAdapter{
	private List<String>classNames=new ArrayList<String>();
	private Map<String, Object>registryMap=new ConcurrentHashMap<String, Object>();
	//两个方法
	//客户端建立连接的方法
	//客户端报错的方法
	

	//思路如下
	//根据包名将所有符合条件的class放到一个容器中
	//如果是分布式的话就要读取配置文件
	public RegistryHandler(){
		scannerClass("cn.shadow.netty.rpc.provider");
		doRegister();
	}
	
	private void doRegister() {
		// TODO Auto-generated method stub
		if(classNames.isEmpty()) {
			return;
		}
		for(String className:classNames) {
			try {
				Class<?>clazz=Class.forName(className);
				Class<?>i=clazz.getInterfaces()[0];
				String serviceName=i.getName();
				//本来这里存的应该是个网络路径，从配置文件中读取
				//在调用的时候再去解析
				//给每一个对应的class起名作为服务名称，并保存到容器之中
				registryMap.put(serviceName, clazz.newInstance());
			} catch (Exception e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
	}
	
	private void scannerClass(String packageName) {
		// 正常来说的话是应该去读配置文件，目前简单粗暴直接扫描本地
		URL url= this.getClass().getClassLoader().getResource(packageName.replaceAll("\\.","/"));
		File classPath=new File(url.getFile());
		for(File file:classPath.listFiles()) {
			if(file.isDirectory()) {
				scannerClass(packageName+"."+file.getName());
			}else {
				classNames.add(packageName+"."+file.getName().replace(".class", ""));
			}
		}
	}

	@Override
	public void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception {
		//有客户端连上的时候，进行回调
		//当有客户端连接过来之后，netty会自动将信息解析成一个对象invokeProtocol,获取协议内容
		//去注册好的容器中寻找符合条件的服务
		//通过远程调用，从而得到结果并回复给客户端
		Object result=new Object();
		InvokerProtocol request=(InvokerProtocol)msg;
		if(registryMap.containsKey(request.getClassName())) {
			Object service =registryMap.get(request.getClassName());
			Method method=service.getClass().getMethod(request.getMethodName(), request.getParames());
			result=method.invoke(service, request.getValues());
		}
		ctx.write(result);
		ctx.flush();
		ctx.close();
	}

	@Override
	public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) throws Exception {
		//连接异常的时候进行回调
		super.exceptionCaught(ctx, cause);
	}

}
