/*
 *  版权名：
 *  描述：服务器程序的启动模块
 *  修改人：温开源
 *  修改时间：2012-4-12
 *  修改内容：代码规范修改
 */

package localhost.server;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;

import org.jboss.netty.buffer.ChannelBuffer;
import org.jboss.netty.buffer.ChannelBuffers;
import org.jboss.netty.util.CharsetUtil;

/* 该类读取磁盘上的网页文件到内存 */
public final class ServerWritePage 
{
    /* 定义index.html页面所在的目录 */
    static final String INDEX_PATH = "WebPage/Index.html";
    
    /* 定义Connection.html页面所在的目录 */
    static final String WEBSOCKET_PATH = "WebPage/Connection.html";
	
    /* 
     * 为了加快网页写回速度，服务器在启动的时候，即把一些常用页面放到内存中
     * 下述四个Buffer分别对应
     *      1.初始登录页面，即index.html
     *      2.WebSocket连接建立页面，即WebPage/Connection.html
     *      3.long polling连接建立页面（后续实现）
     *      4.streaming连接建立页面（后续实现）
     */
    static StringBuffer indexBuffer = new StringBuffer();
    static StringBuffer webSocketBuffer = new StringBuffer();
    static StringBuffer streamingBuffer = new StringBuffer();
    static StringBuffer longPollingBuffer = new StringBuffer();
	
    /* 此函数读取文件到相应的buffer中 */
    public static void serverPageInit()
    {
        File file; 
        BufferedReader br;
        try
        {
            file = new File(INDEX_PATH);
            br = new BufferedReader(new FileReader(file));
            String line = br.readLine();
            while(line != null)
            {
                indexBuffer.append(line + "\r\n");
                line = br.readLine();
            }
            br.close();
			
            file = new File(WEBSOCKET_PATH);
            br = new BufferedReader(new FileReader(file));
            line = br.readLine();
            while(line != null)
            {
                webSocketBuffer.append(line + "\r\n");
                line = br.readLine();
            }
            br.close();
        }
        catch (Exception e)
        {
            e.printStackTrace();
        }		
    }
    	      	  
    /* 
     * 该函数根据请求的类型，返回具有相应网页内容的ChannelBuffer
     * 相应的内容以ASCLL码存放到相应的buffer中
     */
    public static ChannelBuffer getContent(int type)
    {
        if(type == Session.INDEX)
        {
            return ChannelBuffers.copiedBuffer(indexBuffer.toString(),CharsetUtil.US_ASCII);
        }
        else if(type == Session.WEBSOCKET)
        {
            return ChannelBuffers.copiedBuffer(webSocketBuffer.toString(),CharsetUtil.US_ASCII);
        }
        else return null;
    }
}