package cn.shadow.netty.rpc.protocol;

import java.io.Serializable;

import lombok.Data;
@Data
public class InvokerProtocol implements Serializable{

	private String className;//��������
	private String methodName;//��������
	private Class<?> [] parames;//��ֹ��������β��б�
	private Object[] values;
}
