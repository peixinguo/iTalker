package net.qiujuer.web.italker.push.service;
import com.google.common.base.Strings;
import net.qiujuer.web.italker.push.bean.api.base.PushModel;
import net.qiujuer.web.italker.push.bean.api.base.ResponseModel;
import net.qiujuer.web.italker.push.bean.api.user.UpdateInfoModel;
import net.qiujuer.web.italker.push.bean.card.UserCard;
import net.qiujuer.web.italker.push.bean.db.User;
import net.qiujuer.web.italker.push.factory.UserFactory;
import net.qiujuer.web.italker.push.utlis.PushDispatcher;

import javax.ws.rs.*;
import javax.ws.rs.core.MediaType;
import java.util.List;
import java.util.stream.Collectors;

// 127.0.0.1/api/user/...
@Path("/user")
public class UserService extends BaseService{
    // 用户信息修改接口
    // 返回自己的个人信息
    @PUT
    //@Path("") //127.0.0.1/api/user 不需要写，就是当前目录
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    public ResponseModel<UserCard> update(UpdateInfoModel model) {
        if (!UpdateInfoModel.check(model)) {
            return ResponseModel.buildParameterError();
        }

        User self = getSelf();
        // 更新用户信息
        self = model.updateToUser(self);
        self = UserFactory.update(self);
        // 构架自己的用户信息
        UserCard card = new UserCard(self, true);
        // 返回
        return ResponseModel.buildOk(card);
    }

    //拉取联系人
    @GET
    @Path("/contact")
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    public ResponseModel<List<UserCard>> contact(){
        User self = getSelf();

//测试推送消息到客户端
//        PushModel model = new PushModel();
//        model.add(new PushModel.Entity(0,"Hello!!!"));
//
//        PushDispatcher dispatcher = new PushDispatcher();
//        dispatcher.add(self,model);
//        dispatcher.submit();

        //拿到我的联系人
        List<User> users= UserFactory.contacts(self);
        List<UserCard> userCards =  users.stream() //转置操作，User->UserCard
                .map(user -> {
                    return new UserCard(user,true);
                })
                .collect(Collectors.toList());

        return ResponseModel.buildOk(userCards);
    }

    //关注某个人
    //简化：关注人的操作其实是双方同时关注
    @PUT //修改 使用PUT
    @Path("/follow/{followId}")
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    public ResponseModel<UserCard> follow(@PathParam("followId") String followId){
        User self = getSelf();

        //不能关注“我”自己
        if(getSelf().getId().equalsIgnoreCase(followId)){
            return ResponseModel.buildParameterError();
        }

        //我 想关注的人
        User followUser = UserFactory.findById(followId);
        if(followUser == null){
            //此人不存在
            return ResponseModel.buildNotFoundUserError(null);
        }

        //备注没有，后面可以扩展
        followUser = UserFactory.follow(self,followUser,null);
        if(followUser == null){
            //关注失败，返回服务器异常
            return ResponseModel.buildServiceError();
        }

        // TODO 通知 被关注者，我关注了他
        //返回关注的人的信息
        return ResponseModel.buildOk(new UserCard(followUser,true));
    }

    // 获取某人的信息
    @GET
    @Path("{id}") // http://127.0.0.1/api/user/{id}
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    public ResponseModel<UserCard> getUser(@PathParam("id") String id){
        if(Strings.isNullOrEmpty(id)){
            //返回参数异常
            return  ResponseModel.buildParameterError();
        }

        User self = getSelf();
        if(self.getId().equalsIgnoreCase(id)){
            //返回自己，不必查询数据库
            return ResponseModel.buildOk(new UserCard(self,true));
        }

        User user = UserFactory.findById(id);
        if(user == null){
            //找不到指定用户
            return ResponseModel.buildNotFoundUserError(null);
        }

        //是否由关注的记录，有则表明两者已经互相关注
        boolean isFollow = UserFactory.getUserFollow(self,user) != null;
        return ResponseModel.buildOk(new UserCard(user,isFollow));
    }

    // 搜索人的接口实现
    // 为了简化分页：只返回20条数据
    @GET // 搜索人，不涉及数据更改，只是查询，则为GET
    // http://127.0.0.1/api/user/search/
    @Path("/search/{name:(.*)?}") // 名字为任意字符，可以为空
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    public ResponseModel<List<UserCard>> search(@DefaultValue("") @PathParam("name") String name){
        User self = getSelf();

        //先查询数据
        List<User> searchUsers = UserFactory.search(name);
        //把查询结构封装为UserCard
        // 判断这些人是否有我已经关注的人，
        // 如果有，则返回的关注状态中应该已经设置好状态
        // 拿出我的联系人
        List<User> contacts = UserFactory.contacts(self);

        //把User -> UserCard
        List<UserCard> userCards = searchUsers.stream()
                .map(user->{
                    //判断这个人是否在我的联系人中,或者 ，是否是我自己
                    boolean isFollow = user.getId().equalsIgnoreCase(self.getId())
                            //进行联系人的任意匹配，匹配Id字段
                            || contacts.stream().anyMatch(
                                    contactUser-> contactUser.getId().equalsIgnoreCase(user.getId()) );
                    return new UserCard(user,isFollow);
                }).collect(Collectors.toList());

        //返回
        return ResponseModel.buildOk(userCards);
    }
}
