/*
 *  版权名：
 *  描述：服务器程序的启动模块
 *  修改人：温开源
 *  修改时间：2012-4-12
 *  修改内容：代码规范修改
 */
package localhost.server;
  
import static org.jboss.netty.channel.Channels.*; 
import org.jboss.netty.channel.ChannelPipeline;
import org.jboss.netty.channel.ChannelPipelineFactory;
import org.jboss.netty.handler.codec.http.HttpChunkAggregator;
import org.jboss.netty.handler.codec.http.HttpRequestDecoder;
import org.jboss.netty.handler.codec.http.HttpResponseEncoder;
import org.jboss.netty.handler.execution.ExecutionHandler;

/*
 * PipelineFactory类，此类用来初始化Server类
 * 主要实现一个公有函数getPipeline()；即创建一个Pipeline，并向其中加入需要的Handler
 * 该类在Server.java中使用
 */
public class ServerPipelineFactory implements ChannelPipelineFactory 
{
    /* 
     * Netty内置的ExecutionHandler作为此类私有对象 
     * 其具体作用可参见Netty官网API说明：http://netty.io/docs/stable/api/ 
     */
    private final ExecutionHandler executionHandler;
   
    /* ServerPipelineFactory的默认构造函数 */
    public ServerPipelineFactory(ExecutionHandler executionHandler)
    {
        this.executionHandler = executionHandler;
    }
    
    /* 父类抽象函数getPipeline()的覆盖实现 */
    public ChannelPipeline getPipeline() throws Exception
    {
        /* 配置初始的Pipeline */
        ChannelPipeline pipeline = pipeline();
        pipeline.addLast( "decoder", new HttpRequestDecoder() );
        pipeline.addLast( "aggregator", new HttpChunkAggregator(65536) );
        pipeline.addLast( "encoder", new HttpResponseEncoder() );
        /* executionHandler 将把ServerHandler传递到Executor中 */
        pipeline.addLast( "execution", this.executionHandler);
        pipeline.addLast( "handler", new ServerHandler() );
        return pipeline;
	}
}