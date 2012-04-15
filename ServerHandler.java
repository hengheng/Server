/*
 *  ��Ȩ����
 *  ���������������������ģ��
 *  �޸��ˣ��¿�Դ
 *  �޸�ʱ�䣺2012-4-12
 *  �޸����ݣ�����淶�޸�
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
 * ����Ϊ��Ҫ��ҵ���߼������� 
 * ��Pipeline�У���Handler�����뵽executionHandler֮��
 * @see     ServerPipelineFactory.java
 */
public class ServerHandler extends SimpleChannelUpstreamHandler 
{
    /* ��ҳURL��Ŀ¼��ַ */
    static final String ROOT_DIR = "/";
   
    /* ChannelGroup����һ�����Ƶ�Channel */
    static ChannelGroup clientChannelGroup = new DefaultChannelGroup();
    
    /* SessionManagement�������е�Session */
    static SessionManagement sessionManager = new SessionManagement();
      
    public void messageReceived(ChannelHandlerContext ctx, MessageEvent e)throws Exception
    {
        Object msg = e.getMessage();
       
        /* �жϵ�ǰ��Ϣ�Ƿ�Ϊhttp������Ϣ */
        if (msg instanceof HttpRequest) 
        {
            /* ����������������HttpRequest��Ϣ */
            handleHttpRequest(ctx,(HttpRequest)msg,e);
        }
    }
    
    private void handleHttpRequest(ChannelHandlerContext ctx, HttpRequest req,MessageEvent e)throws Exception
    {
        /* ��ȡ����ͷ�е�cookie�ֶ� */
        String cookieInReqHeader = req.getHeader(COOKIE);
       
        /*
         * ����URL��ַ�жϵ�ǰ��������ʽ 
         *      1.���URLΪServerHandler.ROOT_DIR����ô�����û���¼����
         *      2.���URLΪWebSocketHandler.WEBSOCKET_PATH����������WebSocket��������
         *      3.........(�ȴ��������)
         */
        if (req.getUri().equals(ServerHandler.ROOT_DIR))        //�ж�URLΪ��Ŀ¼��ַ
        {    		  	 
            HttpResponse res = new DefaultHttpResponse(HTTP_1_1, OK);	 
            
            /* 
             * �жϵ�ǰ����ͷ�е�cookie�Ƿ�Ϊ�� 
             * ���Ϊ�գ���Ҫǿ��Ҫ�����������cookie 
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
                /* ��ʹ���������ͷ�е�cookie��Ϊ��
                 * ���ܷ�������Session�����в���������Ӧ��cookie����ʱ��Ȼ��Ҫǿ�����������cookie
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
                    if(sessionState == Session.INDEX)       //�жϵ�ǰ��Session״̬�Ƿ�Ϊ��¼ҳ��״̬
                    {	
                        if(req.getMethod() == GET)          //�����ǰ��ҳ���ظ�ˢ�£���Ҫʼ��ά�ִ�Session�����û��ͻص�½ҳ��
                        {
                            ServerHandler.sessionManager.sessionActiveTimeUpdate(cookieInReqHeader);
                            ChannelBuffer content = ServerWritePage.getContent(Session.INDEX);	
                            buildResponse(res,false,null,content,content.readableBytes());
                            sendHttpResponse(ctx, req, res); 
                        }					  					  
                        if(req.getMethod() == POST)             //����û�����˵�¼��ť
                        {
                            HashMap<String,String> parameterList = getPostParameter(req);       //��ȡPOST�������
                            if(parameterList.get("navigatorName").equals("Firefox")
                                    ||parameterList.get("navigatorName").equals("Chrome")
                                    ||parameterList.get("navigatorName").equals("Safari"))                 //��ʱ���ж�������汾��������������汾�������ӽ���ҳ��
                            {
                                try
                                {	
                                    // session��״̬�ͻʱ�䶼����̸���
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
                            else    //��������������汾ѡ�� long Polling����streaming��ʽͬ����������
                            {
                                /* To deal with long polling and streaming */
                                /* To send back the correspondent page */	  
                            }
                        }
                    }
                    else if(sessionState == Session.WEBSOCKET)      //��ǰ��Session״̬�Ƿ�ΪWebSocket���ӽ���״̬
                    {
                        /* 
                         * ����Ѿ������ӽ���״̬�����û�ˢ���˴�ҳ�棬��ʱ��Ȼ��Ҫά��Session
                         * WebSocket�����Ѿ�����������£�ˢ��ҳ��֮���û�������Ҫ���µ�¼
                         * ������ǰ��WebSocket���ӽ���ǿ�ƹرգ��������·����µ����ӽ�������
                         * ��ǿ��һ���û�ֻ�������������һ��WebSocket����
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
    		  
        } else if(req.getUri().equals(WebSocketHandler.WEBSOCKET_PATH))         //���ݵ�ַ�ж�WebSocket�����Ƿ񱻷���
        {
            ServerHandler.sessionManager.sessionChannelIdUpdate(cookieInReqHeader, ctx.getChannel().getId());
            ctx.getPipeline().addLast("websocket", new WebSocketHandler());            //��WebSocketHandler���뵽��ǰ��Pipeline��
            ctx.getPipeline().remove(this);                                                                         //����ǰ��Handler�Ƴ���
            ctx.sendUpstream(e);                                                                                        //��Pipeline�е��¼����ݵ�WebSocketHandler��
            ServerHandler.clientChannelGroup.add(ctx.getChannel());                         //���ҽ���ǰ��Channel���뵽Group��
        }
        else {      //���������������ַ�����������ַ�޷���Ӧ�Ļظ�
            HttpResponse res = new DefaultHttpResponse(HTTP_1_1, NOT_FOUND);
            sendHttpResponse(ctx, req, res);
            return;
        }
    }
 
    /* 
     * ˽�к�����������������Ӧ���û�ʱ�������õ���
     * @param       [ctx]       channelHandler�е�������
     * @param       [req]       ��Ϣ����
     * @param       [res]       ��Ϣ��Ӧ
     */
    private void sendHttpResponse(ChannelHandlerContext ctx, HttpRequest req, HttpResponse res) 
    {
        if (res.getStatus().getCode() != 200)
        {
            //����Ӧ��Ϣ��UTF_8�����ʽд��
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
 
    /* �򵥵��쳣���� */
    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, ExceptionEvent e) throws Exception
    {
        e.getCause().printStackTrace();
        e.getChannel().close();
    }
      
    /* �˺�����װһ����Ӧ��Ϣ
     * ����Ҫ������Ϣ���û�ʱ���˺�����������
     * @param       [res]                   ��Ӧ����
     * @param       [setCookie]             �Ƿ���Ҫǿ�����������cookie
     * @param       [sessionId]             ��setCookieΪ��ʱʹ�ã�������������sessionId
     * @param       [content]               ��������Ӧ����
     * @param       [contentLength]         ��������Ӧ���ݳ���     
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
     *  ��ȡPOST�����е�������� 
     * @param           [req]           һ��POST����
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