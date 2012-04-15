/*
 *  版权名：
 *  描述：服务器程序的启动模块
 *  修改人：温开源
 *  修改时间：2012-4-12
 *  修改内容：代码规范修改
 */

package localhost.server;

import java.util.GregorianCalendar;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;

/*
 * 该类以一个线程运行，负责管理session集合
 * 每隔一段时间（时间设置为Session生存时间的一半），此线程将被启动清理掉不再使用的Session
 * 此线程在服务器初始化时启动，即在Server.java中被调用 
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
                 * 检查超时Session，并清除掉相应的超时Session 
                 * 这种方式可能会导致可预见的性能上的损失，后续将会改进
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
     * session超时检查私有函数
     * 此函数定期被启动检查所有的session，如果有session被超时，那么清理掉此session
     * @param       [sessionManager]        一个session管理类的实例
     * @param       [sessionLifeTime]       session的最大生存时间
     * 
     */
    private void checkSessionSet(SessionManagement sessionManager,long sessionLifeTime)
    {
        GregorianCalendar currentTime = new java.util.GregorianCalendar();	
        if(!sessionManager.isEmpty())
        {
            Set<Map.Entry<String,Session>> entrySet = ServerHandler.sessionManager.getSessionSet().entrySet();
            Iterator<Map.Entry<String, Session>> iter = entrySet.iterator();        //使用iterator对整个Session进行遍历
            while(iter.hasNext())
            {
                Map.Entry<String, Session> entry = iter.next();
                Session session = entry.getValue();
                GregorianCalendar sessionLastActiveTime = (GregorianCalendar)session.getLastActiveTime();
                if(currentTime.getTimeInMillis() - sessionLastActiveTime.getTimeInMillis() 
                        > sessionLifeTime && session.getCurrentState() != Session.INDEX)                //如果当前检查的session已经超时
                {
                    if(!ServerHandler.clientChannelGroup.isEmpty() && 
                            ServerHandler.clientChannelGroup.find(session.getChannelId())!= null)     //如果跟当前Session绑定的channel还未被释放，释放此channel 
                    {
                        ServerHandler.clientChannelGroup.find(session.getChannelId()).close();
                    }
                    sessionManager.remove(session.getSessionId());          //清理掉该Session
                }
            }
        }
    }
}