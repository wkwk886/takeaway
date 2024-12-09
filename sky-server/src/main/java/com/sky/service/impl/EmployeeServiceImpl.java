package com.sky.service.impl;

import com.github.pagehelper.Page;
import com.github.pagehelper.PageHelper;
import com.sky.constant.MessageConstant;
import com.sky.constant.PasswordConstant;
import com.sky.constant.StatusConstant;
import com.sky.context.BaseContext;
import com.sky.dto.EmployeeDTO;
import com.sky.dto.EmployeeLoginDTO;
import com.sky.dto.EmployeePageQueryDTO;
import com.sky.entity.Employee;
import com.sky.exception.AccountLockedException;
import com.sky.exception.AccountNotFoundException;
import com.sky.exception.PasswordErrorException;
import com.sky.mapper.EmployeeMapper;
import com.sky.result.PageResult;
import com.sky.service.EmployeeService;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.DigestUtils;

import java.time.LocalDateTime;
import java.util.List;

@Service
public class EmployeeServiceImpl implements EmployeeService {

    @Autowired
    private EmployeeMapper employeeMapper;

    /**
     * 员工登录
     *
     * @param employeeLoginDTO
     * @return
     */
    public Employee login(EmployeeLoginDTO employeeLoginDTO) {
        String username = employeeLoginDTO.getUsername();
        String password = employeeLoginDTO.getPassword();

        //1、根据用户名查询数据库中的数据
        Employee employee = employeeMapper.getByUsername(username);

        //2、处理各种异常情况（用户名不存在、密码不对、账号被锁定）
        if (employee == null) {
            //账号不存在
            throw new AccountNotFoundException(MessageConstant.ACCOUNT_NOT_FOUND);
        }

        //密码比对
        // 对前端传过来的密码进行MD5加密处理
        password=DigestUtils.md5DigestAsHex(password.getBytes());
        if (!password.equals(employee.getPassword())) {
            //密码错误
            throw new PasswordErrorException(MessageConstant.PASSWORD_ERROR);
        }

        if (employee.getStatus() == StatusConstant.DISABLE) {
            //账号被锁定
            throw new AccountLockedException(MessageConstant.ACCOUNT_LOCKED);
        }

        //3、返回实体对象
        return employee;
    }

    /**
     * 新增员工
     * @param employeeDTO
     */
    public void save(EmployeeDTO employeeDTO) {
        //System.out.println("当前线程的id"+Thread.currentThread().getId());
        //调用mapper,但是mapper操作的是employee类，需要做类型转换，把现在的EmployeeDTO改成employee
        Employee employee = new Employee();
        //对象属性拷贝
        BeanUtils.copyProperties(employeeDTO, employee);
        //设置剩下的employee数据
        employee.setStatus(StatusConstant.ENABLE);//设置状态，用常量类
        employee.setPassword(DigestUtils.md5DigestAsHex(PasswordConstant.DEFAULT_PASSWORD.getBytes()));//设置默认密码,用常量类，MD5加密
        //时间
//        employee.setCreateTime(LocalDateTime.now());
//        employee.setUpdateTime(LocalDateTime.now());
//        //创建人，修改人id
//        //从当前线程的存储空间 动态取当前登录用户的ID
//        employee.setCreateUser(BaseContext.getCurrentId());
//        employee.setUpdateUser(BaseContext.getCurrentId());

        //调用持久层
        employeeMapper.insert(employee);


    }


    /*
    分页查询
     */
    //employeePageQueryDTO中传入页码，每页记录数
    public PageResult pageQuery(EmployeePageQueryDTO employeePageQueryDTO){
        //select * from employee limit 0,10
        //pagehelper辅助分页查询,查询时在SQL代码里自动加上limit语句
        PageHelper.startPage(employeePageQueryDTO.getPage(),employeePageQueryDTO.getPageSize());//这个代码会设置page对象，传入page,size参数

        //调mapper
        Page<Employee> page=employeeMapper.pageQuery(employeePageQueryDTO);

        long total=page.getTotal();
        List<Employee> records=page.getResult();

        return new PageResult(total,records);
    }

    /*
    启用禁用
     */
    public void startOrStop(Integer status, Long id) {
        //update employee set statue=? where id=?

//        //构造实体
//        Employee employee=new Employee();
//        employee.setStatus(status);
//        employee.setId(id);
        //构造实体的第二种方法
        Employee employee=Employee.builder()
                .status(status)
                .id(id)
                .build();

        employeeMapper.update(employee);//公用updat方法来做所有的更新操作，而不仅仅是设计一个update_for_status

    }

    /*
    根据ID查询员工
     */
    public Employee getById(Long id) {
        Employee employee=employeeMapper.getById(id);
        employee.setPassword("****");
        return employee;
    }

    /*
    编辑员工信息
     */
    public void update(EmployeeDTO employeeDTO) {
        //格式转换
        Employee employee=new Employee();
        BeanUtils.copyProperties(employeeDTO,employee);//属性拷贝
        //单独设置时间，人
//        employee.setUpdateTime(LocalDateTime.now());
//        employee.setUpdateUser(BaseContext.getCurrentId());//threadlocal

        employeeMapper.update(employee);

    }
}
