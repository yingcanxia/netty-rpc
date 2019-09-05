package cn.shadow.netty.rpc.consumer;

import cn.shadow.netty.rpc.api.IRPCService;
import cn.shadow.netty.rpc.consumer.proxy.RPCProxy;

public class RPCConsumer {
	public static void main(String[] args) {
		IRPCService service=RPCProxy.create(IRPCService.class);
		System.out.println("8+2="+service.add(8, 2));
		System.out.println("8-2="+service.sub(8, 2));
		System.out.println("8*2="+service.mult(8, 2));
		System.out.println("8/2="+service.div(8, 2));
	}
}
