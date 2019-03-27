package net.qiujuer.web.italker.push.factory;

import net.qiujuer.web.italker.push.bean.api.message.MessageCreateModel;
import net.qiujuer.web.italker.push.bean.db.Group;
import net.qiujuer.web.italker.push.bean.db.Message;
import net.qiujuer.web.italker.push.bean.db.User;
import net.qiujuer.web.italker.push.utlis.Hib;

/**
 * 消息数据存储的类
 */
public class MessageFactory {

    public static Message findById(String id){
        return Hib.query(session -> session.get(Message.class,id));
    }
    public static Message add(User sender, User receiver, MessageCreateModel model){
        Message message = new Message(sender,receiver,model);
        return save(message);
    }

    public static Message add(User sender, Group group, MessageCreateModel model){
        Message message = new Message(sender,group,model);
        return save(message);
    }

    private static Message save(Message message){
        return Hib.query(session -> {
            session.save(message);

            //写入到数据库
            session.flush();
            //紧接着从数据库里查询出来
            session.refresh(message);
            return message;
        });
    }
}
