package cn.shadow.netty.rpc.registry;


import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelOption;
import io.netty.channel.ChannelPipeline;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import io.netty.handler.codec.LengthFieldBasedFrameDecoder;
import io.netty.handler.codec.LengthFieldPrepender;
import io.netty.handler.codec.serialization.ClassResolvers;
import io.netty.handler.codec.serialization.ObjectDecoder;
import io.netty.handler.codec.serialization.ObjectEncoder;

public class RPCRegistry {

	private int port;

	public RPCRegistry(int port) {
		this.port = port;
	}
	
	public void start() {
		try {
			//该类与serverSocket和serverSocketChannel相似
			//主线程池初始化，selector
			EventLoopGroup bossGroup=new NioEventLoopGroup();
			//子线程池，对应客户端的处理逻辑
			EventLoopGroup workerGroup=new NioEventLoopGroup();
			
			ServerBootstrap server=new ServerBootstrap();
			//netty是基于nio
			//其中selector是主线程，work线程
			server.group(bossGroup,workerGroup)
				.channel(NioServerSocketChannel.class)
				.childHandler(new ChannelInitializer<SocketChannel>() {
	
					@Override
					protected void initChannel(SocketChannel ch) throws Exception {
						// 在netty中吧所有的业务逻辑处理全部归总到一个队列
						//队列中包含了各种的处理逻辑有一个封装
						//封装成一个对象，无锁话串行任务队列
						//pipeline
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
						pipeline.addLast(new RegistryHandler());
						
					}
				})
				//最大的key的数量
				.option(ChannelOption.SO_BACKLOG, 128)
				//保证每个子线程都可以被回收利用
				.childOption(ChannelOption.SO_KEEPALIVE, true);
			//正式启动服务，相当于用一个死循环开始轮训
			ChannelFuture future= server.bind(this.port).sync();
			System.out.println("我的RPC框架已经启动，监听端口为："+port);
			future.channel().closeFuture().sync();
		} catch (Exception e) {
			// TODO: handle exception
			e.printStackTrace();
		}
	}
	public static void main(String[] args) {
		new RPCRegistry(8080).start();
	}
}
