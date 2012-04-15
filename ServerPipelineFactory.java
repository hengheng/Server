/*
 *  ��Ȩ����
 *  ���������������������ģ��
 *  �޸��ˣ��¿�Դ
 *  �޸�ʱ�䣺2012-4-12
 *  �޸����ݣ�����淶�޸�
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
 * PipelineFactory�࣬����������ʼ��Server��
 * ��Ҫʵ��һ�����к���getPipeline()��������һ��Pipeline���������м�����Ҫ��Handler
 * ������Server.java��ʹ��
 */
public class ServerPipelineFactory implements ChannelPipelineFactory 
{
    /* 
     * Netty���õ�ExecutionHandler��Ϊ����˽�ж��� 
     * ��������ÿɲμ�Netty����API˵����http://netty.io/docs/stable/api/ 
     */
    private final ExecutionHandler executionHandler;
   
    /* ServerPipelineFactory��Ĭ�Ϲ��캯�� */
    public ServerPipelineFactory(ExecutionHandler executionHandler)
    {
        this.executionHandler = executionHandler;
    }
    
    /* ���������getPipeline()�ĸ���ʵ�� */
    public ChannelPipeline getPipeline() throws Exception
    {
        /* ���ó�ʼ��Pipeline */
        ChannelPipeline pipeline = pipeline();
        pipeline.addLast( "decoder", new HttpRequestDecoder() );
        pipeline.addLast( "aggregator", new HttpChunkAggregator(65536) );
        pipeline.addLast( "encoder", new HttpResponseEncoder() );
        /* executionHandler ����ServerHandler���ݵ�Executor�� */
        pipeline.addLast( "execution", this.executionHandler);
        pipeline.addLast( "handler", new ServerHandler() );
        return pipeline;
	}
}