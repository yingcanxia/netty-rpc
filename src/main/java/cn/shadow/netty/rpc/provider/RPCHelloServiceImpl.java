package cn.shadow.netty.rpc.provider;

import cn.shadow.netty.rpc.api.IRPCHelloService;

public class RPCHelloServiceImpl implements IRPCHelloService {

	@Override
	public String hello(String name) {
		// TODO Auto-generated method stub
		return "hello "+name;
	}

}
