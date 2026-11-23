package org.example.petstore.service;

import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import org.example.petstore.entity.Account;
import org.example.petstore.mapper.AccountMapper;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * 后台用户管理服务（T-20261118-24）。
 *
 * <p><b>防锁死（硬性要求）</b>：管理员不得停用自己；本系统不提供"取消管理员"入口，
 * 因此不存在把自己降权导致无管理员的路径。校验在 Service 层与 Controller 层双重执行。</p>
 *
 * <p>口令为实验用明文（需求裁定 R1），生产须哈希。</p>
 */
@Service
public class AdminUserService {

    /** 密码重置后的固定初始密码（契约 §5.4） */
    public static final String INITIAL_PASSWORD = "PetStore@123";

    private static final String STATUS_ACTIVE = "ACTIVE";
    private static final String STATUS_DISABLED = "DISABLED";

    private final AccountMapper accountMapper;

    public AdminUserService(AccountMapper accountMapper) {
        this.accountMapper = accountMapper;
    }

    /**
     * 全部用户列表。返回前**清空 password**，避免模板或日志泄漏口令。
     */
    public List<Account> listUsers() {
        List<Account> accounts = accountMapper.selectList(
                Wrappers.<Account>lambdaQuery().orderByAsc(Account::getId));
        accounts.forEach(a -> a.setPassword(null));
        return accounts;
    }

    /**
     * 重置为固定初始密码。
     *
     * @return 新口令（明文，仅本次回显一次）
     * @throws IllegalArgumentException 用户不存在
     */
    public String resetPassword(Integer id) {
        Account account = accountMapper.selectById(id);
        if (account == null) {
            throw new IllegalArgumentException("用户不存在：" + id);
        }
        Account patch = new Account();
        patch.setId(id);
        patch.setPassword(INITIAL_PASSWORD);
        accountMapper.updateById(patch);
        return INITIAL_PASSWORD;
    }

    /**
     * 启用/停用切换。
     *
     * @param currentUserId 当前操作者 id，用于防锁死
     * @return 切换后的状态文案（"已启用" / "已停用"）
     * @throws IllegalArgumentException 用户不存在、或试图停用自己
     */
    public String toggleStatus(Integer id, Integer currentUserId) {
        if (id == null) {
            throw new IllegalArgumentException("用户 id 不能为空");
        }
        if (id.equals(currentUserId)) {
            throw new IllegalArgumentException("不能停用自己的账号");
        }
        Account account = accountMapper.selectById(id);
        if (account == null) {
            throw new IllegalArgumentException("用户不存在：" + id);
        }
        boolean active = STATUS_ACTIVE.equals(account.getStatus());
        Account patch = new Account();
        patch.setId(id);
        patch.setStatus(active ? STATUS_DISABLED : STATUS_ACTIVE);
        accountMapper.updateById(patch);
        return active ? "已停用" : "已启用";
    }
}
