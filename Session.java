/*
 *  版权名：
 *  描述：服务器程序的启动模块
 *  修改人：温开源
 *  修改时间：2012-4-12
 *  修改内容：代码规范修改
 */

package localhost.server;

import java.util.GregorianCalendar;

/* Session类，保存相应的session内容并提供存取函数 
 * 每一个实例为一个Session对象
 * */
public class Session
{	
    /* 定义Session所处的状态，目前定义了四种 */
    static final int INDEX = 0;                //登录页面状态
    static final int WEBSOCKET = 1;            //WebSocket连接建立状态
    static final int LONG_POLLING = 2;         //long polling连接建立状态
    static final int STREAMING = 3;            //streaming连接建立状态
	
    private String sessionId;                  //Session的唯一标识符
    private int currentState = INDEX;          //Session的当前状态，默认为初始页面状态
    private GregorianCalendar lastActiveTime;  //Session最后活动时间
    private int channelId;                     //与Session联系在一起的长连接Channel的唯一标识符channelId
	
    /* 
     * 下面这个函数随机产生sessionId 
     * 目前只是用来简单测试，并不被真实使用 
     */
    static String produceSessionId()
    {
        return "" + (long)(Math.random()*1000000000);
    }
	
    /* 构造函数，初始化Session类中的四个私有字段 */
    public Session(String sessionId,int sessionState,int channelId)
    {
        this.sessionId = sessionId;
        this.currentState = sessionState;
        this.lastActiveTime = new GregorianCalendar();
        this.channelId = channelId;
    }
	
    /* 获取此Session的sessionId */
    public String getSessionId()
    {
        return this.sessionId;	
    }
    
    /* 获取此Session的当前状态 */
    public int getCurrentState()
    {
        return this.currentState;
    }
    
    /* 设置此Session的当前状态 */
    public void setCurrentState(int sessionState)
    {
        this.currentState = sessionState;
    }
	
    /* 获取此Session的最后活动时间 */
    public GregorianCalendar getLastActiveTime()
    {
        return this.lastActiveTime;
    }
    
    /* 设置此Session的最后活动时间 */
    public void setLastActiveTime(GregorianCalendar lastActiveTime)
    {
        this.lastActiveTime = lastActiveTime;
    }
    
    /* 获取与当前Session相关的长连接的channelId */
    public int getChannelId()
    {
        return this.channelId;
    }
    
    /* 设置与当前Session相关的长连接的channelId */
    public void setChannelId(int channelId)
    {
        this.channelId = channelId;
    }
}