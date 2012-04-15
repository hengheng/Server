/*
 *  ��Ȩ����
 *  ���������������������ģ��
 *  �޸��ˣ��¿�Դ
 *  �޸�ʱ�䣺2012-4-12
 *  �޸����ݣ�����淶�޸�
 */

package localhost.server;

import java.util.GregorianCalendar;
import java.util.HashMap;

/* Session���Ϲ�����
 * �����������������������session�����ṩ���ĳ���ض��������ݵ��޸ĺ��� 
 */
public class SessionManagement
{
    /* Ĭ�ϵ�session�ĳ�ʱʱ�䣬��msΪ��λ�� */
    static long sessionLifeTime = 10000; 
    
    /* 
     * ʹ��HashMap����Session
     * ÿһ��sessionIdͬһ��session������һ��ӳ�� 
     */
    private HashMap<String,Session> sessionSet;
    	
    public static void setSessionLifeTime(long sessionLifeTime)
    {
        SessionManagement.sessionLifeTime = sessionLifeTime;
    }
    
    /* Ĭ�Ϲ��캯�� */
    public SessionManagement()
    {
        this.sessionSet = new HashMap<String,Session>();
    }
    
    /* ʹ��ָ����С������ʼ��session���� */
    public SessionManagement(int size)
    {
        this.sessionSet = new HashMap<String,Session>(size);
    }
    
    /* ��ȡ��ǰ��Session���� */
    public HashMap<String,Session> getSessionSet()
    {
        return this.sessionSet;
    }
	
    /* �жϵ�ǰ��Session�����Ƿ�Ϊ�� */
    public boolean isEmpty()
    {
        if(this.sessionSet.isEmpty())
        {
            return true;
        }
        else 
        {
            return false;
        }
    }
	
    /* 
     * ��ǰ��Session���������һ��Session����������Ӧ��ӳ�� 
     * @param   [session]       һ��Sessionʵ�� 
     */
    public void add(Session session)
    {
        if(this.sessionSet == null)
        {
            this.sessionSet = new HashMap<String,Session>();
        }
        this.sessionSet.put(session.getSessionId(),session);
    }
    
    /* �ڵ�ǰ��Session������ɾ����sessionId��Ӧ��session */
    public boolean remove(String sessionId)
    {
        if(this.sessionSet == null)
        {
            return false;
        }
        else
        {
            this.sessionSet.remove(sessionId);  
            return true;
        }
    }
    
    /* �жϵ�ǰ��Session�������Ƿ����ָ��sessionId��Ӧ��session */
    public boolean contain(String sessionId)
    {
        if(this.sessionSet.containsKey(sessionId))
        {
            return true;
        }
        else 
        {
            return false;
        }
    }
    
    /* ����sessionId�޸ĸ�session��״̬Ϊstate */
    public boolean sessionStateUpdate(String sessionId,int state)
    {
        if(this.sessionSet == null)
        {
            return false;
        }
        else if(!this.sessionSet.containsKey(sessionId))
        {
            return false;
        }
        else 
        {
            Session session = this.sessionSet.get(sessionId);
            session.setCurrentState(state);
            this.sessionSet.put(sessionId, session);
            return true;
        }	
    }
	
    /* ����sessionId�޸ĸ�session������Ծʱ��Ϊ��ǰʱ�� */
    public void sessionActiveTimeUpdate(String sessionId)
    {
        GregorianCalendar currentTime = new java.util.GregorianCalendar();
        setLastActiveTime(sessionId,currentTime);
    }
    
    /* ����sessionId�޸ĸ�session�����channelΪchannelId��ʶ��Channel */
    public boolean sessionChannelIdUpdate(String sessionId,int channelId)
    {
        if(this.sessionSet == null) return false;
        else if(!this.sessionSet.containsKey(sessionId))
        {
            return false;
        }
        else
        {
            Session session = this.sessionSet.get(sessionId);
            session.setChannelId(channelId);
            this.sessionSet.put(sessionId, session);
            return true;
        }	
    }
	
    /* ����sessionId��Session������Ѱ����Ӧ��Session */
    public Session find(String sessionId)
    {
        return this.sessionSet.get(sessionId);
    }
    
    /* 
     * ˽�и����������ɹ��к���sessionActiveTimeUpdate()���� 
     * ��sessionId������session������Ծʱ���趨ΪlastActiveTime
     */
    private boolean setLastActiveTime(String sessionId,GregorianCalendar lastActiveTime)
    {
        if(this.sessionSet == null)
        {
            return false;
        }
        else if(!this.sessionSet.containsKey(sessionId))
        {
            return false;
        }
        else 
        {
            Session session = this.sessionSet.get(sessionId);
            session.setLastActiveTime(lastActiveTime);
            this.sessionSet.put(sessionId, session);
            return true;
        }   
    }
}