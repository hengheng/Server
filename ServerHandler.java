/*
 *  版权名：
 *  描述：服务器程序的启动模块
 *  修改人：温开源
 *  修改时间：2012-4-12
 *  修改内容：代码规范修改
 */

package localhost.server;

import static org.jboss.netty.handler.codec.http.HttpHeaders.isKeepAlive;
import static org.jboss.netty.handler.codec.http.HttpHeaders.setContentLength;
import static org.jboss.netty.handler.codec.http.HttpHeaders.Names.*;
import static org.jboss.netty.handler.codec.http.HttpMethod.*;
import static org.jboss.netty.handler.codec.http.HttpResponseStatus.NOT_FOUND;
import static org.jboss.netty.handler.codec.http.HttpResponseStatus.OK;
import static org.jboss.netty.handler.codec.http.HttpVersion.HTTP_1_1;
import java.util.HashMap;

import org.jboss.netty.buffer.ChannelBuffer;
import org.jboss.netty.buffer.ChannelBuffers;
import org.jboss.netty.channel.ChannelFuture;
import org.jboss.netty.channel.ChannelFutureListener;
import org.jboss.netty.channel.ChannelHandlerContext;
import org.jboss.netty.channel.ExceptionEvent;
import org.jboss.netty.channel.MessageEvent;
import org.jboss.netty.channel.SimpleChannelUpstreamHandler;
import org.jboss.netty.channel.group.ChannelGroup;
import org.jboss.netty.channel.group.DefaultChannelGroup;
import org.jboss.netty.handler.codec.http.DefaultHttpResponse;
import org.jboss.netty.handler.codec.http.HttpRequest;
import org.jboss.netty.handler.codec.http.HttpResponse;

import org.jboss.netty.util.CharsetUtil;

/* 
 * 该类为主要的业务逻辑处理类 
 * 在Pipeline中，该Handler被加入到executionHandler之后
 * @see     ServerPipelineFactory.java
 */
public class ServerHandler extends SimpleChannelUpstreamHandler 
{
    /* 网页URL根目录地址 */
    static final String ROOT_DIR = "/";
   
    /* ChannelGroup管理一组相似的Channel */
    static ChannelGroup clientChannelGroup = new DefaultChannelGroup();
    
    /* SessionManagement管理所有的Session */
    static SessionManagement sessionManager = new SessionManagement();
      
    public void messageReceived(ChannelHandlerContext ctx, MessageEvent e)throws Exception
    {
        Object msg = e.getMessage();
       
        /* 判断当前信息是否为http请求信息 */
        if (msg instanceof HttpRequest) 
        {
            /* 调用请求处理函数处理HttpRequest信息 */
            handleHttpRequest(ctx,(HttpRequest)msg,e);
        }
    }
    
    private void handleHttpRequest(ChannelHandlerContext ctx, HttpRequest req,MessageEvent e)throws Exception
    {
        /* 获取请求头中的cookie字段 */
        String cookieInReqHeader = req.getHeader(COOKIE);
       
        /*
         * 根据URL地址判断当前的请求处理方式 
         *      1.如果URL为ServerHandler.ROOT_DIR，那么处理用户登录过程
         *      2.如果URL为WebSocketHandler.WEBSOCKET_PATH，则处理连接WebSocket建立过程
         *      3.........(等待后续添加)
         */
        if (req.getUri().equals(ServerHandler.ROOT_DIR))        //判断URL为根目录地址
        {    		  	 
            HttpResponse res = new DefaultHttpResponse(HTTP_1_1, OK);	 
            
            /* 
             * 判断当前请求头中的cookie是否为空 
             * 如果为空，需要强制要求浏览器设置cookie 
             */
            if(cookieInReqHeader == null)
            {   			 
                String sessionId = Session.produceSessionId();
                ServerHandler.sessionManager.add(new Session(sessionId,Session.INDEX,ctx.getChannel().getId() ));
                ChannelBuffer content = ServerWritePage.getContent(Session.INDEX);	
                buildResponse(res,true,sessionId,content,content.readableBytes());
                sendHttpResponse(ctx, req, res);
            }
            else 
            {	
                /* 即使浏览器请求头中的cookie不为空
                 * 可能服务器的Session集合中并不包含对应的cookie，此时仍然需要强制浏览器重设cookie
                 */
                if(!ServerHandler.sessionManager.contain(cookieInReqHeader))
                {
                    String sessionId = Session.produceSessionId();
                    ServerHandler.sessionManager.add(new Session(sessionId,Session.INDEX,ctx.getChannel().getId()));
                    ChannelBuffer content = ServerWritePage.getContent(Session.INDEX);	
                    buildResponse(res,true,sessionId,content,content.readableBytes());
                    sendHttpResponse(ctx, req, res);
                }
                else 
                {
                    int sessionState = ServerHandler.sessionManager.find(cookieInReqHeader).getCurrentState();
                    if(sessionState == Session.INDEX)       //判断当前的Session状态是否为登录页面状态
                    {	
                        if(req.getMethod() == GET)          //如果当前网页被重复刷新，需要始终维持此Session并向用户送回登陆页面
                        {
                            ServerHandler.sessionManager.sessionActiveTimeUpdate(cookieInReqHeader);
                            ChannelBuffer content = ServerWritePage.getContent(Session.INDEX);	
                            buildResponse(res,false,null,content,content.readableBytes());
                            sendHttpResponse(ctx, req, res); 
                        }					  					  
                        if(req.getMethod() == POST)             //如果用户点击了登录按钮
                        {
                            HashMap<String,String> parameterList = getPostParameter(req);       //获取POST请求参数
                            if(parameterList.get("navigatorName").equals("Firefox")
                                    ||parameterList.get("navigatorName").equals("Chrome")
                                    ||parameterList.get("navigatorName").equals("Safari"))                 //暂时简单判断浏览器版本，并根据浏览器版本发回连接建立页面
                            {
                                try
                                {	
                                    // session的状态和活动时间都被相继更新
                                    ServerHandler.sessionManager.sessionStateUpdate(cookieInReqHeader, Session.WEBSOCKET);
                                    ServerHandler.sessionManager.sessionActiveTimeUpdate(cookieInReqHeader);
                                    ChannelBuffer content = ServerWritePage.getContent(Session.WEBSOCKET);
                                    buildResponse(res,false,null,content,content.readableBytes());
                                    sendHttpResponse(ctx, req, res);
                                }
                                catch(Exception a)
                                {
                                    a.printStackTrace();
                                }
                            }	
                            else    //根据其他浏览器版本选择 long Polling或者streaming方式同样建立连接
                            {
                                /* To deal with long polling and streaming */
                                /* To send back the correspondent page */	  
                            }
                        }
                    }
                    else if(sessionState == Session.WEBSOCKET)      //当前的Session状态是否为WebSocket连接建立状态
                    {
                        /* 
                         * 如果已经是连接建立状态，但用户刷新了此页面，此时仍然需要维持Session
                         * WebSocket连接已经建立的情况下，刷新页面之后用户将不需要重新登录
                         * 但是先前的WebSocket连接将被强制关闭，并且重新发出新的连接建立请求
                         * 即强制一个用户只能向服务器请求一条WebSocket连接
                         */
                        if(req.getMethod() == GET){
                            if(ServerHandler.clientChannelGroup.find(ServerHandler.sessionManager.find(cookieInReqHeader).getChannelId()) != null)
                            {
                                ServerHandler.clientChannelGroup.find(ServerHandler.sessionManager.find(cookieInReqHeader).getChannelId()).close();
                            }
                            ServerHandler.sessionManager.sessionActiveTimeUpdate(cookieInReqHeader);
                            ChannelBuffer content = ServerWritePage.getContent(Session.WEBSOCKET);
                            buildResponse(res,false,null,content,content.readableBytes());
                            sendHttpResponse(ctx, req, res); 
                        }
                    }
                } 
            } 
    		  
        } else if(req.getUri().equals(WebSocketHandler.WEBSOCKET_PATH))         //根据地址判断WebSocket请求是否被发送
        {
            ServerHandler.sessionManager.sessionChannelIdUpdate(cookieInReqHeader, ctx.getChannel().getId());
            ctx.getPipeline().addLast("websocket", new WebSocketHandler());            //将WebSocketHandler加入到当前的Pipeline中
            ctx.getPipeline().remove(this);                                                                         //将当前的Handler移除掉
            ctx.sendUpstream(e);                                                                                        //将Pipeline中的事件传递到WebSocketHandler中
            ServerHandler.clientChannelGroup.add(ctx.getChannel());                         //并且将当前的Channel加入到Group中
        }
        else {      //如果请求到了其他地址，发回请求地址无法响应的回复
            HttpResponse res = new DefaultHttpResponse(HTTP_1_1, NOT_FOUND);
            sendHttpResponse(ctx, req, res);
            return;
        }
    }
 
    /* 
     * 私有函数，服务器发送响应给用户时将被调用调用
     * @param       [ctx]       channelHandler中的上下文
     * @param       [req]       消息请求
     * @param       [res]       消息响应
     */
    private void sendHttpResponse(ChannelHandlerContext ctx, HttpRequest req, HttpResponse res) 
    {
        if (res.getStatus().getCode() != 200)
        {
            //将响应信息以UTF_8编码格式写回
            ChannelBuffer content = ChannelBuffers.copiedBuffer(res.getStatus().toString(), CharsetUtil.UTF_8);
            int contentLength = res.getContent().readableBytes();
            buildResponse(res,false,null,content,contentLength);
        }
        ChannelFuture f = ctx.getChannel().write(res);
        if (!isKeepAlive(req) || res.getStatus().getCode() != 200)
        {
            f.addListener(ChannelFutureListener.CLOSE);
            }
    }
 
    /* 简单的异常处理 */
    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, ExceptionEvent e) throws Exception
    {
        e.getCause().printStackTrace();
        e.getChannel().close();
    }
      
    /* 此函数封装一个响应信息
     * 当需要发送信息给用户时，此函数将被调用
     * @param       [res]                   响应对象
     * @param       [setCookie]             是否需要强制浏览器设置cookie
     * @param       [sessionId]             当setCookie为真时使用，服务器产生的sessionId
     * @param       [content]               服务器响应内容
     * @param       [contentLength]         服务器响应内容长度     
     */
    private void buildResponse(HttpResponse res,boolean setCookie,String sessionId,ChannelBuffer content,int contentLength)
    {
        if(setCookie == true)
        {
            res.setHeader(SET_COOKIE, sessionId); 
        }
        res.setHeader(CONTENT_TYPE, "text/html; charset=UTF-8");
        setContentLength(res, content.readableBytes());
        res.setContent(content);
    }
     
    /*
     *  获取POST请求中的请求参数 
     * @param           [req]           一个POST请求
     */
    static HashMap<String,String> getPostParameter(HttpRequest req)
    {
        HashMap<String,String> postParameterList = new HashMap<String,String>();
        ChannelBuffer buf = req.getContent();
        for(String parameterEquation:buf.toString(CharsetUtil.UTF_8).split("&"))
        {
            String[] parameterList = parameterEquation.split("=");
            postParameterList.put(parameterList[0],parameterList[1]);
        }
        return postParameterList;	  
    }
}