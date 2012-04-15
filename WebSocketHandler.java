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
    /* WebSocket������˵�ַ */
    static final String WEBSOCKET_PATH = "/websocket";
    
    /* ˽�б��������WebSocket���ֹ��� */
    private WebSocketServerHandshaker handshaker;
	
    /*
     * ҵ���߼�����׶Σ����յ���WebSocket��Ϣ������Ӧ�ش���
     *      1.����˽׶��յ�HttpRequest������ô����������Ϊ���ӽ����������WebSocket���֣�
     *      2.����˽׶��յ�TextWebSocketFrame֡����ô����ҵ���߼���
     *      3.����˽׶��յ�CloseWebSocketFrame֡����ô���Թر�WebSocket���ӣ�
     *      4.����˽׶��յ�������PingWebSocketFrame֡����ô������Ӧ�Ķ�����PongWebSocketFrame֡
     *
     * ����2�е�ҵ���߼��������£���Ϊ��������ʹ�ã������ᷢ�����ģ�
     *      1.����յ���Ϊ�ַ������ݣ���ô������ת��Ϊ��д���������û�
     *      2.����յ���Ϊ����֡��ʽ����ô������Ӧ��Session�ʱ�䲢��������֡��Ӧ
     *      3.����յ���Ϊһ���ļ�����������ô�洢���ļ�
     */
    @Override
    public void messageReceived(ChannelHandlerContext ctx, MessageEvent e)throws Exception
    {
        Object msg = e.getMessage();
        
        /* �ж���Ϣ�Ƿ�ΪHttpRequest����
         * ����ǣ���ôʹ��Netty�ṩ�����ֻ������WebSocket���ֹ��� 
         */
        if(msg instanceof HttpRequest)
        {
            HttpRequest req = (HttpRequest)msg;
            WebSocketServerHandshakerFactory wsFactory = new WebSocketServerHandshakerFactory(
                    this.getWebSocketLocation(req), null, false);
            this.handshaker = wsFactory.newHandshaker(req);
           
            /* û�л��������Ϣ��������֧�ִ�WebSocket�汾 */
            if(this.handshaker == null) 
            {
                wsFactory.sendUnsupportedWebSocketVersionResponse(ctx.getChannel());
            } 
            else 
            {
                this.handshaker.handshake(ctx.getChannel(), req);       //��ȷ��������
            }
        }
        /* �����Ϣ��WebSocket֡��ʽ�����к�����������������Ѿ�������*/
        else if(msg instanceof WebSocketFrame)
        {
            WebSocketFrame  frame = (WebSocketFrame)msg;
            handleWebSocketFrame(ctx,frame);
        }
    }
	
    private void handleWebSocketFrame(ChannelHandlerContext ctx, WebSocketFrame frame)
    { 
        if(frame instanceof CloseWebSocketFrame)                    //�ж�֡�Ƿ�Ϊ�ر���������֡                   
        {
            this.handshaker.close(ctx.getChannel(), (CloseWebSocketFrame)frame);
            return;
        }
        else if (frame instanceof PingWebSocketFrame)            //�ж�֡�Ƿ�ΪPingWebSocketFrame����������֡
        {
            ctx.getChannel().write(new PongWebSocketFrame(frame.getBinaryData()));
            return;
        }
        else if (!(frame instanceof TextWebSocketFrame))         //����������жϵ�֡�������ı�֡����ô�����������쳣
        {
            throw new UnsupportedOperationException(String.format("%s frame types not supported", frame.getClass().getName()));
        } 
        
        /* 
         * ����Ĳ���Ϊ���Բ��� 
         * ������ɵĹ���������ע�����Ѿ�����
         */
        TextWebSocketFrame webSocketFrame = (TextWebSocketFrame)frame;
        String request = webSocketFrame.getText();
        
        /* ����֡������<heartBeaten>��ʼ���м�Я�������cookie��Ϣ������</heartBeaten>���� 
         * ����������ݸ�ʽ�жϵ�ǰΪ��Ч���ݻ���������Ϣ������ȡ����Ӧ��cookieֵ
         * ����cookieֵ������Ӧ��Session�ʱ�� 
         */
        if(request.startsWith("<heartBeaten>"))
        {
            String cookie = request.substring("<heartBeaten>".length(),request.indexOf("</heartBeaten>"));
            ServerHandler.sessionManager.sessionActiveTimeUpdate(cookie);
            ctx.getChannel().write(new TextWebSocketFrame("H&B"));          //����������������Ӧ֡
        }
        /* �ļ�����������<file>��ʼ�������ļ���������</file>
         * Ȼ�����<fileContent>�ļ�����</fileContent>
         * ���ݴ˸�ʽ�ڷ������˴����ļ�
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
             * ��������������֡���� 
             * ���������û�����5���û��������ݵĴ�д��ʽ��ÿ�η���֮��˯��0.5��
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
    
    /* ��ȡ����ǰ�����WebSocket��ַ */
    private String getWebSocketLocation(HttpRequest req) 
    {
        return "ws://" + req.getHeader(HttpHeaders.Names.HOST) + WEBSOCKET_PATH;
    }
}