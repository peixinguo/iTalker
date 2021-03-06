package net.qiujuer.web.italker.push.bean.db;

import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.GenericGenerator;
import org.hibernate.annotations.UpdateTimestamp;

import javax.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "TB_GROUP")
public class Group {
    @Id
    @PrimaryKeyJoinColumn
    //主键生成存储的类型为UUID,自动生成UUID
    @GeneratedValue(generator = "uuid")
    //把uuid的生成器定义为uuid2，uuid2是常规的UUID toString
    @GenericGenerator(name = "uuid",strategy = "uuid2")
    //不允许更改，不允许为NULL
    @Column(updatable = false,nullable = false)
    private String id;

    //群名
    @Column(nullable = false)
    private String name;

    //群描述
    @Column(nullable = false)
    private String description;

    //群图片
    @Column(nullable = false)
    private String picture;

    //定义为创建时间戳，在创建时就已经写入
    @CreationTimestamp
    @Column(nullable = false)
    private LocalDateTime createAt = LocalDateTime.now();

    //定义为更新时间戳，在创建时就已经写入
    @UpdateTimestamp
    @Column(nullable = false)
    private LocalDateTime updateAt = LocalDateTime.now();

    //群的创建者
    //optional: 选为false，代表必须有个创建者
    //fetch: 加载方式为FetchType.EAGER，意味着加载group的信息时必须加载owner的信息
    //cascade: 级联级别为ALL,所有的更改（更新，删除）都将进行关系更新
    @ManyToOne(optional = false,fetch = FetchType.EAGER,cascade = CascadeType.ALL)
    @JoinColumn(name = "ownerId")
    private User owner;
    @Column(nullable = false,updatable = false,insertable = false)
    private String ownerId;


    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getPicture() {
        return picture;
    }

    public void setPicture(String picture) {
        this.picture = picture;
    }

    public LocalDateTime getCreateAt() {
        return createAt;
    }

    public void setCreateAt(LocalDateTime createAt) {
        this.createAt = createAt;
    }

    public LocalDateTime getUpdateAt() {
        return updateAt;
    }

    public void setUpdateAt(LocalDateTime updateAt) {
        this.updateAt = updateAt;
    }

    public User getOwner() {
        return owner;
    }

    public void setOwner(User owner) {
        this.owner = owner;
    }

    public String getOwnerId() {
        return ownerId;
    }

    public void setOwnerId(String ownerId) {
        this.ownerId = ownerId;
    }
}
