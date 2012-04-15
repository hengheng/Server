/*
 *  ��Ȩ����
 *  ���������������������ģ��
 *  �޸��ˣ��¿�Դ
 *  �޸�ʱ�䣺2012-4-12
 *  �޸����ݣ�����淶�޸�
 */

package localhost.server;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;

import org.jboss.netty.buffer.ChannelBuffer;
import org.jboss.netty.buffer.ChannelBuffers;
import org.jboss.netty.util.CharsetUtil;

/* �����ȡ�����ϵ���ҳ�ļ����ڴ� */
public final class ServerWritePage 
{
    /* ����index.htmlҳ�����ڵ�Ŀ¼ */
    static final String INDEX_PATH = "WebPage/Index.html";
    
    /* ����Connection.htmlҳ�����ڵ�Ŀ¼ */
    static final String WEBSOCKET_PATH = "WebPage/Connection.html";
	
    /* 
     * Ϊ�˼ӿ���ҳд���ٶȣ���������������ʱ�򣬼���һЩ����ҳ��ŵ��ڴ���
     * �����ĸ�Buffer�ֱ��Ӧ
     *      1.��ʼ��¼ҳ�棬��index.html
     *      2.WebSocket���ӽ���ҳ�棬��WebPage/Connection.html
     *      3.long polling���ӽ���ҳ�棨����ʵ�֣�
     *      4.streaming���ӽ���ҳ�棨����ʵ�֣�
     */
    static StringBuffer indexBuffer = new StringBuffer();
    static StringBuffer webSocketBuffer = new StringBuffer();
    static StringBuffer streamingBuffer = new StringBuffer();
    static StringBuffer longPollingBuffer = new StringBuffer();
	
    /* �˺�����ȡ�ļ�����Ӧ��buffer�� */
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
     * �ú���������������ͣ����ؾ�����Ӧ��ҳ���ݵ�ChannelBuffer
     * ��Ӧ��������ASCLL���ŵ���Ӧ��buffer��
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