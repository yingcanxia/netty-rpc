package cn.shadow.netty.rpc.consumer.proxy;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;

import cn.shadow.netty.rpc.consumer.RPCProxyHandler;
import cn.shadow.netty.rpc.protocol.InvokerProtocol;
import cn.shadow.netty.rpc.registry.RegistryHandler;
import io.netty.bootstrap.Bootstrap;
import io.netty.channel.Channel;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelInboundHandler;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelOption;
import io.netty.channel.ChannelPipeline;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioSocketChannel;
import io.netty.handler.codec.LengthFieldBasedFrameDecoder;
import io.netty.handler.codec.LengthFieldPrepender;
import io.netty.handler.codec.serialization.ClassResolvers;
import io.netty.handler.codec.serialization.ObjectDecoder;
import io.netty.handler.codec.serialization.ObjectEncoder;

public class RPCProxy {
	public static <T> T create(Class<?> clazz) {
		MethodProxy proxy=new MethodProxy(clazz);
		T result=(T) Proxy.newProxyInstance(clazz.getClassLoader(), new Class[] {clazz}, proxy);
		return result;
	}
	private static class MethodProxy implements InvocationHandler{

		private Class<?> clazz;
		
		public MethodProxy(Class<?> clazz) {
			this.clazz = clazz;
		}

		@Override
		public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
			// TODO Auto-generated method stub
			if(Object.class.equals(method.getDeclaringClass())) {
				return method.invoke(proxy, args);
			}else {
				return rpcInvoke(proxy,method,args);
			}
		}

		private Object rpcInvoke(Object proxy, Method method, Object[] args) {
			//先要构造一个协议内容
			InvokerProtocol msg=new InvokerProtocol();
			msg.setClassName(this.clazz.getName());
			msg.setMethodName(method.getName());
			msg.setParames(method.getParameterTypes());
			msg.setValues(args);
			//在此处发送相应的网络请求
			final RPCProxyHandler proxyHandler= new RPCProxyHandler();
			EventLoopGroup workGroup=new NioEventLoopGroup();
			try {
				Bootstrap client=new Bootstrap();
				client.group(workGroup)
					.channel(NioSocketChannel.class)
					.option(ChannelOption.TCP_NODELAY, true)
					.handler(new ChannelInitializer<SocketChannel>() {
						@Override
						protected void initChannel(SocketChannel ch) throws Exception {
							// TODO Auto-generated method stub
							ChannelPipeline pipeline=ch.pipeline();
							//就是对处理逻辑的封装
							//对于自定义协议进行编解码
							pipeline.addLast(new LengthFieldBasedFrameDecoder(Integer.MAX_VALUE, 0, 4,0,4));
							//自定义编码器 
							pipeline.addLast(new LengthFieldPrepender(4));
							//实参处理
							pipeline.addLast("encoder",new ObjectEncoder());
							//此处开始需要执行自己所需要的逻辑
							//1.注册给每个对象取名字，对外提供服务的名字
							pipeline.addLast("decoder",new ObjectDecoder(Integer.MAX_VALUE,ClassResolvers.cacheDisabled(null)));
							//2.对于每一个服务进行登记
							pipeline.addLast(proxyHandler);
						}
						
					});
				ChannelFuture future=client.connect("127.0.0.1",8080).sync();
				future.channel().writeAndFlush(msg).sync();
				future.channel().closeFuture().sync();
				
			} catch (Exception e) {
				// TODO: handle exception
				e.printStackTrace();
			}finally {
				workGroup.shutdownGracefully();
			}
			return proxyHandler.getResponce();
		}
		
	}
}
