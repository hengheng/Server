/*
 *  ��Ȩ����
 *  ���������������������ģ��
 *  �޸��ˣ��¿�Դ
 *  �޸�ʱ�䣺2012-4-12
 *  �޸����ݣ�����淶�޸�
 */

package localhost.server;

import java.net.InetSocketAddress;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;  
import org.jboss.netty.bootstrap.ServerBootstrap;
import org.jboss.netty.channel.socket.nio.NioServerSocketChannelFactory;
import org.jboss.netty.handler.execution.ExecutionHandler;
import org.jboss.netty.handler.execution.OrderedMemoryAwareThreadPoolExecutor;

/*
 * ʵ�ֿ����еķ������࣬������������
 * ������Ҫ��main������������
 */
public class Server implements Runnable
{	
    /* ����Ĭ�ϵķ������󶨶˿� */
    static final int DEFAULT_PORT = 8088;

    /* static final String CURRENT_DIR_PATH = System.getProperty("user.dir"); */
    
    /* �������رձ�־����־Ϊtrueʱ���������������ر� */
    private boolean isShutDown = false;
	
    /* �������󶨵Ķ˿ں� */
    private int port = DEFAULT_PORT;
	
    /* ��������Ĭ�Ϲ��캯�� */
    public Server()
    {
        /* �˺�����ִ���κβ��� */
    }
   
    /* 
     * ��������Ĺ��캯��
     * @param  [port]  �û�ϣ���������󶨵Ķ˿ں� 
     */
    public Server(int port)
    {		
        this.port = port;
    }
	
    /*
     * �رշ����������÷������Ĺرձ�־Ϊtrue
     */
    public void shutDownServer()
    {
        this.isShutDown = true;
    }
	
    /* ��������������
     * ����ʹ�������̳߳س�ʼ��bootstrap����
     * ����PipelineFactory
     * ���������󶨵���Ӧ�Ķ˿ڡ�  
     */
    public void run()
    {
        /* ������������������bootstrap���������̳߳س�ʼ�� */
        ServerBootstrap bootstrap = new ServerBootstrap(new NioServerSocketChannelFactory(
                Executors.newCachedThreadPool(), Executors.newCachedThreadPool()));
	
        /* ����һ��Netty���õ�Handler�����Handler��������ʼ��ServerPipelineFactory 
         * executionHandler�������Ƿ���Pipeline�е�ServerHandler֮ǰ��
         * Netty�е�worker�߳��ܹ�����ExecutionHandler�е��̳߳��е��߳�������ServerHandler
         */
        ExecutionHandler executionHandler = new ExecutionHandler(new OrderedMemoryAwareThreadPoolExecutor(16, 1048576, 1048576));
        
        /* ���÷�����������PipelineFactory */
        bootstrap.setPipelineFactory(new ServerPipelineFactory(executionHandler));

        /* ������channel���� */
        bootstrap.setOption("child.tcpNoDelay", true);
        /* ���������󶨵�ָ���Ķ˿� */
        bootstrap.bind(new InetSocketAddress(this.port));

        /* �жϵ�ǰ�������Ƿ��ѱ��ر�  */
        while(!this.isShutDown);
		
        /* ����������ѱ��رգ���ô�ͷ����е���Դ */
        bootstrap.releaseExternalResources();
        executionHandler.releaseExternalResources();
    }
	
    /*
     * main���������������߳� 
     */
    static public void main(String[] args)
    {
        /* ����½ҳ������Ӵ���ҳ��Ԥ���뵽�ڴ��� */
        ServerWritePage.serverPageInit();
        
        /* ��ʱ����һ������Ϊ2���̳߳أ�*/
        ExecutorService executor = Executors.newFixedThreadPool(2);
        
        /* �����������߳�  */
        executor.execute(new Server());		
        
        /* ����Session�����߳�  */
        executor.execute(new SessionCleaner());
    }
}