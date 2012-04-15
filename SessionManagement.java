/*
 *  版权名：
 *  描述：服务器程序的启动模块
 *  修改人：温开源
 *  修改时间：2012-4-12
 *  修改内容：代码规范修改
 */

package localhost.server;

import java.util.GregorianCalendar;
import java.util.HashMap;

/* Session集合管理类
 * 该类管理服务器所保存的所有session对象，提供针对某个特定对象内容的修改函数 
 */
public class SessionManagement
{
    /* 默认的session的超时时间，以ms为单位记 */
    static long sessionLifeTime = 10000; 
    
    /* 
     * 使用HashMap管理Session
     * 每一个sessionId同一个session对象建立一个映射 
     */
    private HashMap<String,Session> sessionSet;
    	
    public static void setSessionLifeTime(long sessionLifeTime)
    {
        SessionManagement.sessionLifeTime = sessionLifeTime;
    }
    
    /* 默认构造函数 */
    public SessionManagement()
    {
        this.sessionSet = new HashMap<String,Session>();
    }
    
    /* 使用指定大小容量初始化session集合 */
    public SessionManagement(int size)
    {
        this.sessionSet = new HashMap<String,Session>(size);
    }
    
    /* 获取当前的Session集合 */
    public HashMap<String,Session> getSessionSet()
    {
        return this.sessionSet;
    }
	
    /* 判断当前的Session集合是否为空 */
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
     * 向当前的Session集合中添加一个Session，并建立相应的映射 
     * @param   [session]       一个Session实例 
     */
    public void add(Session session)
    {
        if(this.sessionSet == null)
        {
            this.sessionSet = new HashMap<String,Session>();
        }
        this.sessionSet.put(session.getSessionId(),session);
    }
    
    /* 在当前的Session集合中删除掉sessionId对应的session */
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
    
    /* 判断当前的Session集合中是否包含指定sessionId对应的session */
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
    
    /* 根据sessionId修改该session的状态为state */
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
	
    /* 根据sessionId修改该session的最后活跃时间为当前时间 */
    public void sessionActiveTimeUpdate(String sessionId)
    {
        GregorianCalendar currentTime = new java.util.GregorianCalendar();
        setLastActiveTime(sessionId,currentTime);
    }
    
    /* 根据sessionId修改该session的相关channel为channelId标识的Channel */
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
	
    /* 根据sessionId在Session集合中寻找相应的Session */
    public Session find(String sessionId)
    {
        return this.sessionSet.get(sessionId);
    }
    
    /* 
     * 私有辅助函数，由公有函数sessionActiveTimeUpdate()调用 
     * 将sessionId关联的session的最后活跃时间设定为lastActiveTime
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