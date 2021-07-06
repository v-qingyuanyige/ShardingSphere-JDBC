### ShardingSphere-JDBC的使用

---

#### 数据分片

- **环境准备**

程序环境：SpringBoot

数据库环境：两个库六个表（做对比验证用）

![img](C:\Users\LMD\AppData\Local\Temp\企业微信截图_16246113028073.png)

- **导入maven依赖**

导入数据库驱动等必要依赖

导入ShardingSphere-JDBC依赖

```java
<dependency>
    <groupId>org.apache.shardingsphere</groupId>
    <artifactId>sharding-jdbc-spring-boot-starter</artifactId>
    <version>4.1.1</version>
</dependency>
```

- **编写 `application.yml` ，配置shardingsphere所需信息**

***1.数据库垂直拆分 + 库内分表的配置方式***

数据库垂直拆分：将数据库中的表按一定规则分开存放到不同数据库中（t_user放在db0，t_customer放在db1）

库内分表：数据库中的一个表分裂成多个表（db0中的t_user分为t_user0和t_user1）

```properties
spring:
  #shardingsphere配置
  shardingsphere:
    #配置数据源列表
    dataSource:
      #数据源集合
      names: ds0,ds1
      #配置数据源ds0
      ds0: 
        type: com.alibaba.druid.pool.DruidDataSource
        driver-class-name: com.mysql.jdbc.Driver
        url: jdbc:mysql://localhost:3306/db0
        username: root
        password: 123456
      #配置数据源ds1
      ds1: 
        type: com.alibaba.druid.pool.DruidDataSource
        driver-class-name: com.mysql.jdbc.Driver
        url: jdbc:mysql://localhost:3306/db1
        username: root
        password: 123456
    #配置分片规则
    sharding:
      #配置参与分片的table列表; 可以针对单个逻辑表单独配置分表策略、分库策略，也可以配置默认分库/分表策略应用给所有表
      tables:
        #配置逻辑表t_user的分片规则
        t_user:
          #逻辑表t_user有两个物理表：ds0.t_user0, ds0.t_user1
          actual-data-nodes: ds0.t_user$->{0..1}
          #配置分表策略
          table-strategy:
            #行表达式分片策略; 分片策略还可以是standard、complex、hint，详见官方文档
            inline:
              #行表达式分片的依据字段
              sharding-column: id
              #行表达式分片的算法：id为偶数路由到ds0.t_user0，id为奇数路由到ds0.t_user1
              algorithm-expression: t_user$->{id % 2} 
        #配置逻辑表t_customer的分片规则
        t_customer:
          #逻辑表t_customer有两个物理表：ds1.t_customer0, ds1.t_customer1
          actual-data-nodes: ds1.t_customer$->{0..1}
          #配置分表策略
          table-strategy:
            #行表达式分片策略
            inline:
              #行表达式分片的依据字段
              sharding-column: id
              #行表达式分片的算法：id为偶数路由到ds1.t_customer0，id为奇数路由到ds1.t_customer1
              algorithm-expression: t_customer$->{id % 2}
    props:
      sql:
        show: true
#更多配置详见ShardingSphere官方文档
```

***2.数据库水平拆分 + 库内分表的配置方式***

数据库水平拆分：将一个表分裂成多个，存放到不同的数据库中（db0和db1中都有t_user）

?sql通过分片规则路由到具体表的方式?：

> a.在sql中确定逻辑表名t_user，到tables中寻找t_user（找不到就去默认数据库中找，此处未配置默认数据库）
>
> b.在t_user.actual-data-nodes中查看是否逻辑库逻辑表
>
> c.若是逻辑库，就到database-strategy（default-database-strategy）中找到物理库
>
> d.若是逻辑表，就到table-strategy（default-table-strategy）中找到物理表

```properties
#数据源配置同上
#配置分片规则
    sharding:
      #配置参与分片的table列表; 可以针对单个逻辑表单独配置分表策略、分库策略，也可以配置默认分库/分表策略应用给所有表
      tables:
        #配置逻辑表t_user的分片规则
        t_user:
          #逻辑表t_user有四个物理表：ds0.t_user0, ds0.t_user1, ds1.t_user0, ds1.t_user1
          actual-data-nodes: ds$->{0..1}.t_user$->{0..1}
          #配置分表策略
          table-strategy:
            #行表达式分片策略; 分片策略还可以是standard、complex、hint，详见官方文档
            inline:
              #行表达式分片的依据字段
              sharding-column: id
              #行表达式分片的算法：id为偶数路由到ds0.t_user0，id为奇数路由到ds0.t_user1
              algorithm-expression: t_user$->{id % 2}
          #配置分库策略
          database-strategy:
            inline:
              #行表达式分片的依据字段
              sharding-column: age #分库的字段
              #行表达式分片的算法：age为偶数路由到ds0，age为奇数路由到ds1
              algorithm-expression: ds$->{age % 2} #分片的算法
```

***3.为所有表配置默认分片规则***

```properties
#数据源配置同上
#配置分片规则
    sharding:
      #配置参与分片的table列表; 可以针对单个逻辑表单独配置分表策略、分库策略，也可以配置默认分库/分表策略应用给所有表
      tables:
        #配置逻辑表t_user的分片规则
        t_user:
          #逻辑表t_user有四个物理表：ds0.t_user0, ds0.t_user1, ds1.t_user0, ds1.t_user1
          actual-data-nodes: ds$->{0..1}.t_user$->{0..1}
      #配置默认分表策略
      default-table-strategy:
        #行表达式分片策略; 分片策略还可以是standard、complex、hint，详见官方文档
        inline:
          #行表达式分片的依据字段
          sharding-column: id
          #行表达式分片的算法：id为偶数路由到ds0.t_user0，id为奇数路由到ds0.t_user1
          algorithm-expression: t_user$->{id % 2}
      #配置默认分库策略
      default-database-strategy:
        inline:
          #行表达式分片的依据字段
          sharding-column: age #分库的字段
          #行表达式分片的算法：age为偶数路由到ds0，age为奇数路由到ds1
          algorithm-expression: ds$->{age % 2} #分片的算法
```

***4.其他配置方式***

灵活应用可以实现各种分片

- **编写测试类**

POJO：

```java
@Data
public class User {
    int id;
    String name;
}
@Data
public class Customer {
    int id;
    String name;
}
```

Mapper:

```java
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
```

```java
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
    @Insert("insert into t_customer(id, name) values(#{id}, #{name}")
    boolean insertCustomer(Customer customer);
}
```

测试类（SpringBoot自动生成）

```java
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
```

#### 分布式事务XA

- 环境准备

数据库环境：两个数据库分别建t_record表

![img](C:\Users\LMD\AppData\Local\Temp\企业微信截图_16251994958073.png)

- 导入maven依赖

```java
		<dependency>
            <groupId>org.apache.shardingsphere</groupId>
            <artifactId>sharding-transaction-xa-core</artifactId>
            <version>4.1.1</version>
        </dependency>
```

- 编写application.yml配置

```yaml
	  tables:
        t_record:
#          key-generator:
#            column: user_id
#            type: SNOWFLAKE
          actual-data-nodes: ds$->{0..1}.t_record
          database-strategy:
            inline:
              sharding-column: id
              algorithm-expression: ds$->{id % 2}
```

- 相关工具类配置

向容器中注入 `PlatformTransactionManager` 和 `JdbcTemplate` 

```java
@Configuration
@EnableTransactionManagement
public class TransactionConfiguration {

    @Bean
    public PlatformTransactionManager txManager(final DataSource dataSource){
        return new DataSourceTransactionManager(dataSource);
    }

    @Bean
    public JdbcTemplate jdbcTemplate(final DataSource dataSource){
        return new JdbcTemplate(dataSource);
    }
}
```

- 编写服务类

具体的SQL操作和XA分布式事务使用的地方

```java
@Service
public class XARecordService {

    private final JdbcTemplate jdbcTemplate;

    @Autowired
    public XARecordService(final JdbcTemplate jdbcTemplate){
        this.jdbcTemplate = jdbcTemplate;
    }

    /*
    SQL未出错的情况
    */
    @Transactional
    @ShardingTransactionType(TransactionType.XA)
    public TransactionType insert(final int count){
        return jdbcTemplate.execute("INSERT INTO t_record(id, name) VALUES (?, ?)", (PreparedStatementCallback<TransactionType>) preparedStatement ->{
            doInsert(count, preparedStatement);
            return TransactionTypeHolder.get();
        });
    }

    /*
    人为抛出异常，测试是否回滚
    */
    @Transactional
    @ShardingTransactionType(TransactionType.XA)
    public void insertFailed(final int count){
        jdbcTemplate.execute("INSERT INTO t_record(id, name) VALUES (?, ?)", (PreparedStatementCallback<TransactionType>) preparedStatement -> {
            doInsert(count, preparedStatement);
            throw new SQLException("mock transaction failed");
        });
    }

    private void doInsert(final int count, final PreparedStatement preparedStatement) throws SQLException {
        for(int i = 0; i < count; i++){
            preparedStatement.setInt(1, i);
            preparedStatement.setString(2,"record");
            preparedStatement.executeUpdate();
        }
    }
}
```

- 测试类

```java
@SpringBootTest
class ShardingTransactionDemoApplicationTests {

    @Autowired
    XARecordService xaRecordService;

    @Test
    void insert(){
//        xaRecordService.insert(10);
        xaRecordService.insertFailed(10);
    }
}
```

#### 分布式事务BASE

比XA多一步注册中心

#### 读写分离

maven依赖和数据分片相同

4.1.1版本支持单主库多从库，不支持主从库之间的数据同步。
当一个线程中没有进行主库的修改，读操作会被路由到从库进行查询；若一个线程中主库进行了修改，为了保持数据一致性，读操作也会被路由到主库查询。

```yaml
spring:
  shardingsphere:
    datasource:
      names: master,slave0
      master:
        type: com.alibaba.druid.pool.DruidDataSource
        driver-class-name: com.mysql.cj.jdbc.Driver
        url: jdbc:mysql://10.49.196.10:3306/itest
        username: admin
        password: Root_123!
      slave0:
        type: com.alibaba.druid.pool.DruidDataSource
        driver-class-name: com.mysql.cj.jdbc.Driver
        url: jdbc:mysql://10.49.196.20:3306/itest
        username: admin
        password: Root_123!
    masterslave:
      name: ms
      master-data-source-name: master
      slave-data-source-names: slave0
    props:
      sql:
        show: true
```

