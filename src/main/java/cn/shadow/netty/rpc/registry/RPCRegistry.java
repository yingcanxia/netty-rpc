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
			//������serverSocket��serverSocketChannel����
			//���̳߳س�ʼ����selector
			EventLoopGroup bossGroup=new NioEventLoopGroup();
			//���̳߳أ���Ӧ�ͻ��˵Ĵ����߼�
			EventLoopGroup workerGroup=new NioEventLoopGroup();
			
			ServerBootstrap server=new ServerBootstrap();
			//netty�ǻ���nio
			//����selector�����̣߳�work�߳�
			server.group(bossGroup,workerGroup)
				.channel(NioServerSocketChannel.class)
				.childHandler(new ChannelInitializer<SocketChannel>() {
	
					@Override
					protected void initChannel(SocketChannel ch) throws Exception {
						// ��netty�а����е�ҵ���߼�����ȫ�����ܵ�һ������
						//�����а����˸��ֵĴ����߼���һ����װ
						//��װ��һ�����������������������
						//pipeline
						ChannelPipeline pipeline=ch.pipeline();
						//���ǶԴ����߼��ķ�װ
						//�����Զ���Э����б����
						pipeline.addLast(new LengthFieldBasedFrameDecoder(Integer.MAX_VALUE, 0, 4,0,4));
						//�Զ�������� 
						pipeline.addLast(new LengthFieldPrepender(4));
						//ʵ�δ���
						pipeline.addLast("encoder",new ObjectEncoder());
						//�˴���ʼ��Ҫִ���Լ�����Ҫ���߼�
						//1.ע���ÿ������ȡ���֣������ṩ���������
						pipeline.addLast("decoder",new ObjectDecoder(Integer.MAX_VALUE,ClassResolvers.cacheDisabled(null)));
						//2.����ÿһ��������еǼ�
						pipeline.addLast(new RegistryHandler());
						
					}
				})
				//����key������
				.option(ChannelOption.SO_BACKLOG, 128)
				//��֤ÿ�����̶߳����Ա���������
				.childOption(ChannelOption.SO_KEEPALIVE, true);
			//��ʽ���������൱����һ����ѭ����ʼ��ѵ
			ChannelFuture future= server.bind(this.port).sync();
			System.out.println("�ҵ�RPC����Ѿ������������˿�Ϊ��"+port);
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
