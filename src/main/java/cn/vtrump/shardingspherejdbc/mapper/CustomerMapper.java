package cn.vtrump.shardingspherejdbc.mapper;

import cn.vtrump.shardingspherejdbc.pojo.Customer;
import org.apache.ibatis.annotations.*;
import org.apache.ibatis.type.JdbcType;
import org.springframework.stereotype.Repository;

import java.util.List;

@Mapper
public interface CustomerMapper {
    @Results({
            @Result(column = "id",jdbcType = JdbcType.INTEGER,property = "id"),
            @Result(column = "name",jdbcType = JdbcType.VARCHAR,property = "name")
    })

    //查询所有Customer信息
    @Select("select * from t_customer")
    List<Customer> selectAll();

    //增加一个Customer
    @Insert("insert into t_customer(id, name) values(#{id}, #{name})")
    boolean insertCustomer(Customer customer);
}
