package cn.vtrump.shardingspherejdbc;

import cn.vtrump.shardingspherejdbc.mapper.CustomerMapper;
import cn.vtrump.shardingspherejdbc.mapper.UserMapper;
import cn.vtrump.shardingspherejdbc.pojo.Customer;
import cn.vtrump.shardingspherejdbc.pojo.User;
import org.junit.jupiter.api.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;

@RunWith(SpringRunner.class)
@SpringBootTest
class ShardingsphereJdbcApplicationTests {

    @Autowired
    private UserMapper userMapper;
    @Autowired
    private CustomerMapper customerMapper;

    @Test
    void insert(){
        User user = new User();
        user.setId(0);
        user.setName("user0");
        userMapper.insertUser(user);

        Customer customer = new Customer();
        customer.setId(1);
        customer.setName("customer1");
        customerMapper.insertCustomer(customer);
    }

    @Test
    void select(){
        System.out.println(userMapper.selectAll());
        System.out.println(customerMapper.selectAll());
    }
}
