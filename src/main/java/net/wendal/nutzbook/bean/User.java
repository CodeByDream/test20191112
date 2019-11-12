package net.wendal.nutzbook.bean;

import org.nutz.dao.entity.annotation.*;

import java.util.Date;

/**
 * @author Administrator
 * @date 2019/11/11 15:15:46
 * @description
 */
@Table("t_user")
public class User extends BasePojo {
    @Id//标示主键
    private int id;

    @Name//标示唯一索引
    @Column//表示该pojo字段和数据库一致
    private String name;
    @Column("passwd")//表示数据库对应字段为passwd
    private String password;
    @Column
    private String salt;



    @One(target=UserProfile.class, field="id", key="userId")//添加一对一关系映射
    protected UserProfile profile;


    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public String getSalt() {
        return salt;
    }

    public void setSalt(String salt) {
        this.salt = salt;
    }

    public UserProfile getProfile() {
        return profile;
    }

    public void setProfile(UserProfile profile) {
        this.profile = profile;
    }


}
