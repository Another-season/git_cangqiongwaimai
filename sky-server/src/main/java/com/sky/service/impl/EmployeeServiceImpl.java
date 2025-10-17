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
import java.util.ArrayList;
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
        // TODO 后期需要进行md5加密，然后再进行比对
        password = DigestUtils.md5DigestAsHex(password.getBytes());
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

    //新增员工的方法
    public void save(EmployeeDTO employeeDTO) {
        //补全数据库中需要的字段
        Employee employee = new Employee();

        //单一属性拷贝太麻烦，使用复制属性工具类，要求属性名一致
        BeanUtils.copyProperties(employeeDTO, employee);
        //设置状态为启用，使用预先定义的常量
        employee.setStatus(StatusConstant.ENABLE);
        //设置默认密码为123456用MD5加密,用提前提供的密码常量
        employee.setPassword(DigestUtils.md5DigestAsHex(PasswordConstant.DEFAULT_PASSWORD.getBytes()));
        //设置时间
        employee.setCreateTime(LocalDateTime.now());
        employee.setUpdateTime(LocalDateTime.now());
        //先设置默认值，后期通过jwt来获取当前用户id存入Threadlocal, ThreadLocal在当前线程开辟的共享空间，从中获取当前用户的信息,更便捷的共享信息
        employee.setCreateUser(BaseContext.getCurrentId());
        employee.setUpdateUser(BaseContext.getCurrentId());

        employeeMapper.insert(employee);
    }

    //分页查询员工的方法
    public PageResult pageQuery(EmployeePageQueryDTO employeePageQueryDTO) {
        //使用mybatis提供的分页插件PageHelper,插件自带Page类封装查询结果,别忘记导入依赖
        //插件默认是为startPage后的第一个查询操作动态拼接limit条件
        //底层为使用拦截器为下一个sql语句动态计算出limit条件
        PageHelper.startPage(employeePageQueryDTO.getPage(), employeePageQueryDTO.getPageSize());
        //调用mapper层查询
        Page<Employee> page = employeeMapper.pageQuery(employeePageQueryDTO);
        //获取插件Page对象封装的分页信息
        long total = page.getTotal();
        List<Employee> list = page.getResult();
        //封装分页结果到对应对象
        PageResult pageResult = new PageResult(total, list);

        return pageResult;
    }

    //更改员工状态的代码实现
    public void startOrStop(Integer status, Long id) {
        /*employee.setId(id);
        //修改该更新时间
        employee.setUpdateTime(LocalDateTime.now());
        employee.setUpdateUser(BaseContext.getCurrentId());
        //把状态一块存储到employee对象中
        employee.setStatus(status);*/
        //使用builder模式创建对象，链式调用，更方便
        Employee employee = Employee.builder().status(status).id(id).updateTime(LocalDateTime.now()).updateUser(BaseContext.getCurrentId()).build();
        employeeMapper.update(employee);
    }

    //根据id查询员工的方法，用于修改员工时的显示
    public Employee getById(Long id) {
        Employee employee = employeeMapper.getById(id);
        employee.setPassword("****");
        return employee;
    }

    //编辑修改员工信息
    public void update(EmployeeDTO employeeDTO) {
        Employee employee = new Employee();
        BeanUtils.copyProperties(employeeDTO, employee);
        employee.setUpdateTime(LocalDateTime.now());
        employee.setUpdateUser(BaseContext.getCurrentId());
        employeeMapper.update(employee);
    }
}
