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
	//��������
	//�ͻ��˽������ӵķ���
	//�ͻ��˱���ķ���
	

	//˼·����
	//���ݰ��������з���������class�ŵ�һ��������
	//����Ƿֲ�ʽ�Ļ���Ҫ��ȡ�����ļ�
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
				//����������Ӧ���Ǹ�����·�����������ļ��ж�ȡ
				//�ڵ��õ�ʱ����ȥ����
				//��ÿһ����Ӧ��class������Ϊ�������ƣ������浽����֮��
				registryMap.put(serviceName, clazz.newInstance());
			} catch (Exception e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
	}
	
	private void scannerClass(String packageName) {
		// ������˵�Ļ���Ӧ��ȥ�������ļ���Ŀǰ�򵥴ֱ�ֱ��ɨ�豾��
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
		//�пͻ������ϵ�ʱ�򣬽��лص�
		//���пͻ������ӹ���֮��netty���Զ�����Ϣ������һ������invokeProtocol,��ȡЭ������
		//ȥע��õ�������Ѱ�ҷ��������ķ���
		//ͨ��Զ�̵��ã��Ӷ��õ�������ظ����ͻ���
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
		//�����쳣��ʱ����лص�
		super.exceptionCaught(ctx, cause);
	}

}
