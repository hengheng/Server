/*
 *  ��Ȩ����
 *  ���������������������ģ��
 *  �޸��ˣ��¿�Դ
 *  �޸�ʱ�䣺2012-4-12
 *  �޸����ݣ�����淶�޸�
 */

package localhost.server;

import java.util.GregorianCalendar;

/* Session�࣬������Ӧ��session���ݲ��ṩ��ȡ���� 
 * ÿһ��ʵ��Ϊһ��Session����
 * */
public class Session
{	
    /* ����Session������״̬��Ŀǰ���������� */
    static final int INDEX = 0;                //��¼ҳ��״̬
    static final int WEBSOCKET = 1;            //WebSocket���ӽ���״̬
    static final int LONG_POLLING = 2;         //long polling���ӽ���״̬
    static final int STREAMING = 3;            //streaming���ӽ���״̬
	
    private String sessionId;                  //Session��Ψһ��ʶ��
    private int currentState = INDEX;          //Session�ĵ�ǰ״̬��Ĭ��Ϊ��ʼҳ��״̬
    private GregorianCalendar lastActiveTime;  //Session���ʱ��
    private int channelId;                     //��Session��ϵ��һ��ĳ�����Channel��Ψһ��ʶ��channelId
	
    /* 
     * ������������������sessionId 
     * Ŀǰֻ�������򵥲��ԣ���������ʵʹ�� 
     */
    static String produceSessionId()
    {
        return "" + (long)(Math.random()*1000000000);
    }
	
    /* ���캯������ʼ��Session���е��ĸ�˽���ֶ� */
    public Session(String sessionId,int sessionState,int channelId)
    {
        this.sessionId = sessionId;
        this.currentState = sessionState;
        this.lastActiveTime = new GregorianCalendar();
        this.channelId = channelId;
    }
	
    /* ��ȡ��Session��sessionId */
    public String getSessionId()
    {
        return this.sessionId;	
    }
    
    /* ��ȡ��Session�ĵ�ǰ״̬ */
    public int getCurrentState()
    {
        return this.currentState;
    }
    
    /* ���ô�Session�ĵ�ǰ״̬ */
    public void setCurrentState(int sessionState)
    {
        this.currentState = sessionState;
    }
	
    /* ��ȡ��Session�����ʱ�� */
    public GregorianCalendar getLastActiveTime()
    {
        return this.lastActiveTime;
    }
    
    /* ���ô�Session�����ʱ�� */
    public void setLastActiveTime(GregorianCalendar lastActiveTime)
    {
        this.lastActiveTime = lastActiveTime;
    }
    
    /* ��ȡ�뵱ǰSession��صĳ����ӵ�channelId */
    public int getChannelId()
    {
        return this.channelId;
    }
    
    /* �����뵱ǰSession��صĳ����ӵ�channelId */
    public void setChannelId(int channelId)
    {
        this.channelId = channelId;
    }
}