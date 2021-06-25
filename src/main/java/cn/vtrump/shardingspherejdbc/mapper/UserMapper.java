package cn.vtrump.shardingspherejdbc.mapper;

import cn.vtrump.shardingspherejdbc.pojo.Customer;
import cn.vtrump.shardingspherejdbc.pojo.User;
import org.apache.ibatis.annotations.*;
import org.apache.ibatis.type.JdbcType;

import java.util.List;

@Mapper
public interface UserMapper {
    @Results({
            @Result(column = "id",jdbcType = JdbcType.INTEGER,property = "id"),
            @Result(column = "name",jdbcType = JdbcType.VARCHAR,property = "name")
    })
    //查询所有User信息
    @Select("select * from t_user")
    List<User> selectAll();
    //增加一个User
    @Insert("insert into t_user(id, name) values(#{id}, #{name}")
    boolean insertUser(User user);
}
