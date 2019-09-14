package cn.shadow.netty.rpc.consumer;

import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;

public class RPCProxyHandler extends ChannelInboundHandlerAdapter{
	private Object responce;

	@Override
	public void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception {
		// TODO Auto-generated method stub
		responce=msg;
	}

	@Override
	public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) throws Exception {
		// TODO Auto-generated method stub
		cause.printStackTrace();
		System.out.println("客户算出现异常");
	}

	public Object getResponce() {
		// TODO Auto-generated method stub
		return responce;
	}

}
