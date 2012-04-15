/*
 *  ��Ȩ����
 *  ���������������������ģ��
 *  �޸��ˣ��¿�Դ
 *  �޸�ʱ�䣺2012-4-12
 *  �޸����ݣ�����淶�޸�
 */

package localhost.server;

import java.util.GregorianCalendar;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;

/*
 * ������һ���߳����У��������session����
 * ÿ��һ��ʱ�䣨ʱ������ΪSession����ʱ���һ�룩�����߳̽����������������ʹ�õ�Session
 * ���߳��ڷ�������ʼ��ʱ����������Server.java�б����� 
 */
public class SessionCleaner implements Runnable
{
    public void run()
    {
        try
        {
            while(true)
            {
                /* 
                 * ��鳬ʱSession�����������Ӧ�ĳ�ʱSession 
                 * ���ַ�ʽ���ܻᵼ�¿�Ԥ���������ϵ���ʧ����������Ľ�
                 */
                checkSessionSet(ServerHandler.sessionManager,SessionManagement.sessionLifeTime);
                Thread.sleep(SessionManagement.sessionLifeTime / 2);
            }
        }
        catch (Exception e)
        {
            e.printStackTrace();
        }
    }
	
    /* 
     * session��ʱ���˽�к���
     * �˺������ڱ�����������е�session�������session����ʱ����ô�������session
     * @param       [sessionManager]        һ��session�������ʵ��
     * @param       [sessionLifeTime]       session���������ʱ��
     * 
     */
    private void checkSessionSet(SessionManagement sessionManager,long sessionLifeTime)
    {
        GregorianCalendar currentTime = new java.util.GregorianCalendar();	
        if(!sessionManager.isEmpty())
        {
            Set<Map.Entry<String,Session>> entrySet = ServerHandler.sessionManager.getSessionSet().entrySet();
            Iterator<Map.Entry<String, Session>> iter = entrySet.iterator();        //ʹ��iterator������Session���б���
            while(iter.hasNext())
            {
                Map.Entry<String, Session> entry = iter.next();
                Session session = entry.getValue();
                GregorianCalendar sessionLastActiveTime = (GregorianCalendar)session.getLastActiveTime();
                if(currentTime.getTimeInMillis() - sessionLastActiveTime.getTimeInMillis() 
                        > sessionLifeTime && session.getCurrentState() != Session.INDEX)                //�����ǰ����session�Ѿ���ʱ
                {
                    if(!ServerHandler.clientChannelGroup.isEmpty() && 
                            ServerHandler.clientChannelGroup.find(session.getChannelId())!= null)     //�������ǰSession�󶨵�channel��δ���ͷţ��ͷŴ�channel 
                    {
                        ServerHandler.clientChannelGroup.find(session.getChannelId()).close();
                    }
                    sessionManager.remove(session.getSessionId());          //�������Session
                }
            }
        }
    }
}