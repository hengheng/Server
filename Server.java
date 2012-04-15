/*
 *  版权名：
 *  描述：服务器程序的启动模块
 *  修改人：温开源
 *  修改时间：2012-4-12
 *  修改内容：代码规范修改
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
 * 实现可运行的服务器类，并启动服务器
 * 此类主要由main函数启动运行
 */
public class Server implements Runnable
{	
    /* 定义默认的服务器绑定端口 */
    static final int DEFAULT_PORT = 8088;

    /* static final String CURRENT_DIR_PATH = System.getProperty("user.dir"); */
    
    /* 服务器关闭标志，标志为true时，表明服务器被关闭 */
    private boolean isShutDown = false;
	
    /* 服务器绑定的端口号 */
    private int port = DEFAULT_PORT;
	
    /* 服务器的默认构造函数 */
    public Server()
    {
        /* 此函数不执行任何操作 */
    }
   
    /* 
     * 服务器类的构造函数
     * @param  [port]  用户希望服务器绑定的端口号 
     */
    public Server(int port)
    {		
        this.port = port;
    }
	
    /*
     * 关闭服务器，设置服务器的关闭标志为true
     */
    public void shutDownServer()
    {
        this.isShutDown = true;
    }
	
    /* 服务器启动函数
     * 函数使用两个线程池初始化bootstrap对象；
     * 设置PipelineFactory
     * 将服务器绑定到相应的端口。  
     */
    public void run()
    {
        /* 创建服务器启动对象bootstrap，以两个线程池初始化 */
        ServerBootstrap bootstrap = new ServerBootstrap(new NioServerSocketChannelFactory(
                Executors.newCachedThreadPool(), Executors.newCachedThreadPool()));
	
        /* 创建一个Netty内置的Handler，这个Handler将用来初始化ServerPipelineFactory 
         * executionHandler的作用是放在Pipeline中的ServerHandler之前。
         * Netty中的worker线程能够调用ExecutionHandler中的线程池中的线程来处理ServerHandler
         */
        ExecutionHandler executionHandler = new ExecutionHandler(new OrderedMemoryAwareThreadPoolExecutor(16, 1048576, 1048576));
        
        /* 设置服务器启动的PipelineFactory */
        bootstrap.setPipelineFactory(new ServerPipelineFactory(executionHandler));

        /* 设置子channel属性 */
        bootstrap.setOption("child.tcpNoDelay", true);
        /* 将服务器绑定到指定的端口 */
        bootstrap.bind(new InetSocketAddress(this.port));

        /* 判断当前服务器是否已被关闭  */
        while(!this.isShutDown);
		
        /* 如果服务器已被关闭，那么释放所有的资源 */
        bootstrap.releaseExternalResources();
        executionHandler.releaseExternalResources();
    }
	
    /*
     * main函数启动服务器线程 
     */
    static public void main(String[] args)
    {
        /* 将登陆页面和连接创建页面预读入到内存中 */
        ServerWritePage.serverPageInit();
        
        /* 暂时创建一个容量为2的线程池；*/
        ExecutorService executor = Executors.newFixedThreadPool(2);
        
        /* 启动服务器线程  */
        executor.execute(new Server());		
        
        /* 启动Session清理线程  */
        executor.execute(new SessionCleaner());
    }
}