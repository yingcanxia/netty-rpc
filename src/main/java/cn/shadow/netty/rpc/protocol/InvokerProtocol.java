package cn.shadow.netty.rpc.protocol;

import java.io.Serializable;

import lombok.Data;
@Data
public class InvokerProtocol implements Serializable{

	private String className;//服务名称
	private String methodName;//方法名称
	private Class<?> [] parames;//防止重载添加形参列表
	private Object[] values;
}
