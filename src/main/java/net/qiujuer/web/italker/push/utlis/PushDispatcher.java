package net.qiujuer.web.italker.push.utlis;

import com.gexin.rp.sdk.base.IBatch;
import com.gexin.rp.sdk.base.IIGtPush;
import com.gexin.rp.sdk.base.IPushResult;
import com.gexin.rp.sdk.base.impl.SingleMessage;
import com.gexin.rp.sdk.base.impl.Target;
import com.gexin.rp.sdk.http.IGtPush;
import com.gexin.rp.sdk.template.LinkTemplate;
import com.gexin.rp.sdk.template.TransmissionTemplate;
import com.google.common.base.Strings;
import net.qiujuer.web.italker.push.bean.api.base.PushModel;
import net.qiujuer.web.italker.push.bean.db.User;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * 消息推送工具类
 */
public class PushDispatcher  {

    //采用"Java SDK 快速入门"， "第二步 获取访问凭证 "中获得的应用配置，用户可以自行替换
    private static final String appId = "xcl33ZRby5AhPCaC9ka1ZA";
    private static final String appKey = "YFtBtce7ArAfwLL0yikn82";
    private static final String masterSecret = "xtFoHuOSyM6RvUTaB9Bbl4";
    private static final String host = "http://sdk.open.api.igexin.com/apiex.htm";

    private final IGtPush pusher;
    //要收到消息的人和内容的列表
    private final List<BatchBean> beans = new ArrayList<>();

    public PushDispatcher() {
        //最根本的发送者
        pusher = new IGtPush(host,appKey,masterSecret);
    }

    /**
     * 添加一条消息
     * @param receiver 接收者
     * @param model 接收的推送的Model
     * @return 是否添加成功
     */
    public boolean add(User receiver, PushModel model){
        //基础检查，必须要有pushId
        if(receiver == null || model == null || Strings.isNullOrEmpty(receiver.getPushId()))
            return false;

        String pushString = model.getPushString();
        if(Strings.isNullOrEmpty(pushString))
            return false;

        //构建一个目标+内容
        BatchBean bean = buildMessage(receiver.getPushId(),pushString);
        beans.add(bean);
        return true;
    }

    /**
     * 对要发送的数据进行格式化封装
     * @param clientId 接收者的设备Id
     * @param text 要发送的数据
     * @return
     */
    private BatchBean buildMessage(String clientId,String text){

        TransmissionTemplate template = new TransmissionTemplate();
        template.setAppId(appId);
        template.setAppkey(appKey);
        template.setTransmissionContent(text);
        template.setTransmissionType(0); // 这个Type为int型，填写1则自动启动app

        SingleMessage message = new SingleMessage();
        message.setData(template);//把透传消息设置到到消息模板中
        message.setOffline(true);//是否允许离线发送
        message.setOfflineExpireTime(24 *3600* 1000);//离线消息时长，在一天内你都可以接收到消息

        // 设置推送目标，填入appid和clientId
        Target target = new Target();
        target.setAppId(appId);
        target.setClientId(clientId);

        return new BatchBean(message,target);
    }

    //进行消息最终发送
    public boolean submit(){
        IBatch batch = pusher.getBatch();

        //是否有数据需要发送
        boolean haveData = false;

        for (BatchBean bean : beans){
            try {
                batch.add(bean.message,bean.target);
                haveData = true;
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

        //没有数据就返回
        if(!haveData)
            return false;

        //有数据，进行发送
        IPushResult result = null;
        try {
            result = batch.submit();
        } catch (IOException e) {
            e.printStackTrace();
            //失败情况下尝试重复发送
            try {
                batch.retry();
            } catch (IOException e1) {
                e1.printStackTrace();
            }
        }

        if (result != null){
            try{
                Logger.getLogger("PushDispatcher")
                        .log(Level.INFO, (String) result.getResponse().get("result"));
                return true;
            }catch (Exception e){
                e.printStackTrace();
            }

        }

        Logger.getLogger("PushDispatcher")
                .log(Level.WARNING, "推送服务器响应异常！！");

        return false;


    }

    //给每个人发送消息的一个Bean封装
    private static class BatchBean{
        SingleMessage message;
        Target target;

        public BatchBean(SingleMessage message, Target target) {
            this.message = message;
            this.target = target;
        }
    }


}
