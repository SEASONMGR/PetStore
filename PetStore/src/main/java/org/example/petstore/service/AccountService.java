package org.example.petstore.service;

import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import org.example.petstore.entity.Account;
import org.example.petstore.mapper.AccountMapper;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.Optional;

/**
 * 用户模块基础服务：注册、登录、按用户名查询。
 *
 * <p><b>安全声明（重要，实验用）</b>：本课程实验按需求裁定 R1 采用<b>密码明文存储</b>，
 * 仅为演示 MyBatis Plus CRUD 与 Spring MVC 表单流程。生产环境<b>必须</b>改为
 * 加盐哈希（如 BCrypt / Argon2）并配合 Spring Security，切勿照搬。</p>
 *
 * <p>本类不引入 Spring Security（需求裁定已排除），登录态仅存放于 HttpSession。</p>
 */
@Service
public class AccountService {

    /** 账号启用状态，与 schema.sql 中 account.status 的取值一致 */
    private static final String STATUS_ACTIVE = "ACTIVE";

    private final AccountMapper accountMapper;

    public AccountService(AccountMapper accountMapper) {
        this.accountMapper = accountMapper;
    }

    /**
     * 登录校验。
     *
     * <p>注意：明文口令比较用 {@code equals}，<b>不要</b>用 {@code ==}。</p>
     *
     * @param username 登录名
     * @param password 明文口令
     * @return 校验通过返回账号，否则 {@link Optional#empty()}
     */
    public Optional<Account> login(String username, String password) {
        if (username == null || password == null) {
            return Optional.empty();
        }
        Optional<Account> account = findByUsername(username.trim());
        if (account.isEmpty()) {
            return Optional.empty();
        }
        Account found = account.get();
        if (!STATUS_ACTIVE.equals(found.getStatus())) {
            return Optional.empty();
        }
        if (!password.equals(found.getPassword())) {
            return Optional.empty();
        }
        return Optional.of(found);
    }

    /**
     * 注册新用户。
     *
     * <p>只写业务字段；id 由数据库自增，admin/status/createdAt 用默认值，
     * 避免前端传入 admin=true 越权注册管理员。</p>
     *
     * @param account 至少需包含 username / password / email（email 表上有唯一约束）
     * @return 写入数据库后的账号（含自增 id）
     * @throws IllegalArgumentException 用户名为空或已存在时抛出，由 Controller 转为页面提示
     */
    public Account register(Account account) {
        if (account == null || account.getUsername() == null || account.getUsername().isBlank()) {
            throw new IllegalArgumentException("用户名不能为空");
        }
        String username = account.getUsername().trim();
        if (findByUsername(username).isPresent()) {
            throw new IllegalArgumentException("用户名已存在：" + username);
        }
        // email 有唯一约束，先查一次给出友好提示，避免直接抛数据库异常
        String email = account.getEmail();
        if (email != null && !email.isBlank()) {
            Long sameEmail = accountMapper.selectCount(
                    Wrappers.<Account>lambdaQuery().eq(Account::getEmail, email.trim()));
            if (sameEmail != null && sameEmail > 0) {
                throw new IllegalArgumentException("邮箱已被注册：" + email);
            }
        }

        Account toSave = new Account();
        toSave.setUsername(username);
        // 实验用明文口令；生产须改哈希（见类注释）
        toSave.setPassword(account.getPassword());
        toSave.setEmail(email == null ? null : email.trim());
        toSave.setFirstName(account.getFirstName());
        toSave.setLastName(account.getLastName());
        toSave.setPhone(account.getPhone());
        toSave.setAddress(account.getAddress());
        toSave.setCity(account.getCity());
        toSave.setState(account.getState());
        toSave.setZip(account.getZip());
        toSave.setCountry(account.getCountry());
        // 关键：不从前端取 admin / status，统一由后端定
        toSave.setStatus(STATUS_ACTIVE);
        toSave.setAdmin(Boolean.FALSE);
        toSave.setCreatedAt(LocalDateTime.now());

        accountMapper.insert(toSave);
        return toSave;
    }

    /** 按用户名精确查询，查不到返回空 */
    public Optional<Account> findByUsername(String username) {
        if (username == null || username.isBlank()) {
            return Optional.empty();
        }
        return Optional.ofNullable(accountMapper.selectOne(
                Wrappers.<Account>lambdaQuery().eq(Account::getUsername, username.trim())));
    }
}
