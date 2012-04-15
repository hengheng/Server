package localhost.server;

import java.io.File;
import java.io.PrintWriter;

import org.jboss.netty.channel.ChannelHandlerContext;
import org.jboss.netty.channel.ExceptionEvent;
import org.jboss.netty.channel.MessageEvent;
import org.jboss.netty.channel.SimpleChannelUpstreamHandler;
import org.jboss.netty.handler.codec.http.HttpHeaders;
import org.jboss.netty.handler.codec.http.HttpRequest;
import org.jboss.netty.handler.codec.http.websocketx.CloseWebSocketFrame;
import org.jboss.netty.handler.codec.http.websocketx.PingWebSocketFrame;
import org.jboss.netty.handler.codec.http.websocketx.PongWebSocketFrame;
import org.jboss.netty.handler.codec.http.websocketx.TextWebSocketFrame;
import org.jboss.netty.handler.codec.http.websocketx.WebSocketFrame;
import org.jboss.netty.handler.codec.http.websocketx.WebSocketServerHandshaker;
import org.jboss.netty.handler.codec.http.websocketx.WebSocketServerHandshakerFactory;

public class WebSocketHandler extends SimpleChannelUpstreamHandler 
{
    /* WebSocket请求发向此地址 */
    static final String WEBSOCKET_PATH = "/websocket";
    
    /* 私有变量，完成WebSocket握手工作 */
    private WebSocketServerHandshaker handshaker;
	
    /*
     * 业务逻辑处理阶段，对收到的WebSocket消息进行相应地处理
     *      1.如果此阶段收到HttpRequest请求，那么将此请求作为连接建立请求，完成WebSocket握手；
     *      2.如果此阶段收到TextWebSocketFrame帧，那么处理业务逻辑；
     *      3.如果此阶段收到CloseWebSocketFrame帧，那么尝试关闭WebSocket连接；
     *      4.如果此阶段收到二进制PingWebSocketFrame帧，那么发回相应的二进制PongWebSocketFrame帧
     *
     * 而对2中的业务逻辑处理如下（暂为测试连接使用，后续会发生更改）
     *      1.如果收到的为字符串数据，那么将数据转换为大写，并传给用户
     *      2.如果收到的为心跳帧格式，那么更新相应的Session活动时间并发回心跳帧响应
     *      3.如果收到的为一个文件发送请求，那么存储该文件
     */
    @Override
    public void messageReceived(ChannelHandlerContext ctx, MessageEvent e)throws Exception
    {
        Object msg = e.getMessage();
        
        /* 判断信息是否为HttpRequest请求；
         * 如果是，那么使用Netty提供的握手机制完成WebSocket握手工作 
         */
        if(msg instanceof HttpRequest)
        {
            HttpRequest req = (HttpRequest)msg;
            WebSocketServerHandshakerFactory wsFactory = new WebSocketServerHandshakerFactory(
                    this.getWebSocketLocation(req), null, false);
            this.handshaker = wsFactory.newHandshaker(req);
           
            /* 没有获得握手信息，表明不支持此WebSocket版本 */
            if(this.handshaker == null) 
            {
                wsFactory.sendUnsupportedWebSocketVersionResponse(ctx.getChannel());
            } 
            else 
            {
                this.handshaker.handshake(ctx.getChannel(), req);       //正确建立握手
            }
        }
        /* 如果消息是WebSocket帧格式，进行后续处理（这表明握手已经建立）*/
        else if(msg instanceof WebSocketFrame)
        {
            WebSocketFrame  frame = (WebSocketFrame)msg;
            handleWebSocketFrame(ctx,frame);
        }
    }
	
    private void handleWebSocketFrame(ChannelHandlerContext ctx, WebSocketFrame frame)
    { 
        if(frame instanceof CloseWebSocketFrame)                    //判断帧是否为关闭连接请求帧                   
        {
            this.handshaker.close(ctx.getChannel(), (CloseWebSocketFrame)frame);
            return;
        }
        else if (frame instanceof PingWebSocketFrame)            //判断帧是否为PingWebSocketFrame二进制数据帧
        {
            ctx.getChannel().write(new PongWebSocketFrame(frame.getBinaryData()));
            return;
        }
        else if (!(frame instanceof TextWebSocketFrame))         //如果在这里判断到帧并不是文本帧，那么表明操作有异常
        {
            throw new UnsupportedOperationException(String.format("%s frame types not supported", frame.getClass().getName()));
        } 
        
        /* 
         * 下面的操作为测试操作 
         * 具体完成的工作在上面注释中已经详述
         */
        TextWebSocketFrame webSocketFrame = (TextWebSocketFrame)frame;
        String request = webSocketFrame.getText();
        
        /* 心跳帧总是以<heartBeaten>开始，中间携带浏览器cookie信息，并以</heartBeaten>结束 
         * 根据这个数据格式判断当前为有效数据还是心跳信息，并获取到相应的cookie值
         * 根据cookie值更新相应的Session活动时间 
         */
        if(request.startsWith("<heartBeaten>"))
        {
            String cookie = request.substring("<heartBeaten>".length(),request.indexOf("</heartBeaten>"));
            ServerHandler.sessionManager.sessionActiveTimeUpdate(cookie);
            ctx.getChannel().write(new TextWebSocketFrame("H&B"));          //服务器发回心跳响应帧
        }
        /* 文件数据总是以<file>起始，加上文件名，加上</file>
         * 然后加上<fileContent>文件内容</fileContent>
         * 根据此格式在服务器端创建文件
         */
        else if(request.startsWith("<file>"))
        {
            String fileName = request.substring(6,request.indexOf("</file>"));
            File file = new File("file/" + fileName);
            try
            {
                PrintWriter output = new PrintWriter(file);
                int startPos = request.indexOf("<fileContent>") + "<fileContent>".length();
                int endPos = request.indexOf("</fileContent>");
                output.print(request.substring(startPos, endPos));
                output.close();
            }
            catch (Exception e)
            {
                e.printStackTrace(); 
            }
        }
        else 
        {
            /* 
             * 进入正常的数据帧测试 
             * 服务器向用户返回5次用户发送数据的大写格式，每次发送之后睡眠0.5秒
             */
            try
            {
                int times = 5;
                while(times > 0)
                {
                    ctx.getChannel().write(new TextWebSocketFrame(request.toUpperCase() + "  "));
                    Thread.sleep(500);
                    times--;
                }
            }
            catch(Exception a)
            {
                a.printStackTrace();
            }
        }
    }
    
    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, ExceptionEvent e) throws Exception 
    {
        e.getCause().printStackTrace();
        e.getChannel().close();
    }
    
    /* 获取到当前请求的WebSocket地址 */
    private String getWebSocketLocation(HttpRequest req) 
    {
        return "ws://" + req.getHeader(HttpHeaders.Names.HOST) + WEBSOCKET_PATH;
    }
}