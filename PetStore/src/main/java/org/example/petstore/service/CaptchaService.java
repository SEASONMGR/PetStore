package org.example.petstore.service;

import jakarta.servlet.http.HttpSession;
import org.springframework.stereotype.Service;

import java.security.SecureRandom;

/**
 * 算术题验证码（需求裁定 R2：算术题，答案存 session）。
 *
 * <p>设计要点：</p>
 * <ul>
 *   <li>题干与答案分离：只把答案 {@link #SESSION_CAPTCHA_ANSWER} 存 session，题干由
 *       {@link #generate(HttpSession)} 返回给 Controller 放进 model，下次渲染重新生成。</li>
 *   <li>一次性：{@link #verify(HttpSession, String)} 无论对错都立即移除答案（防重放）。</li>
 *   <li>容错：非数字/空输入一律判错，不抛异常（否则前端拼 URL/表单异常会变成 400/500）。</li>
 *   <li>会话隔离：答案存在各用户自己的 HttpSession 里，多用户互不影响；多实例部署需外置 session。</li>
 * </ul>
 */
@Service
public class CaptchaService {

    /** session 中存放答案的键名（契约固定值，见 接口契约.md §2.3） */
    public static final String SESSION_CAPTCHA_ANSWER = "captchaAnswer";

    /** 加数与和的范围：两个 1–9 的整数，和最大 18，不会超纲 */
    private static final int MIN = 1;
    private static final int MAX = 9;

    /** 验证码不是安全凭证，用 SecureRandom 只是顺手避免可预测性 */
    private final SecureRandom random = new SecureRandom();

    /**
     * 生成新题目：随机两个 1–9 的加数，题干如 {@code "3 + 5 = ?"}。
     * 答案写入 session（覆盖旧答案），题干返回给调用方放进 model。
     *
     * @param session 当前会话
     * @return 题干字符串
     */
    public String generate(HttpSession session) {
        int a = nextInt();
        int b = nextInt();
        session.setAttribute(SESSION_CAPTCHA_ANSWER, a + b);
        return a + " + " + b + " = ?";
    }

    /**
     * 校验用户输入，并**立即失效**该验证码（一次性）。
     *
     * <p>注意顺序：先取出答案并移除，再比对。任何分支都不会让答案留在 session 中。</p>
     *
     * @param session 当前会话
     * @param input   用户输入（允许带首尾空格；空或非数字判错）
     * @return 正确返回 true
     */
    public boolean verify(HttpSession session, String input) {
        if (session == null) {
            return false;
        }
        Object expected = session.getAttribute(SESSION_CAPTCHA_ANSWER);
        // 无论后续结果如何，先移除：一次性语义，也是防重放的关键
        session.removeAttribute(SESSION_CAPTCHA_ANSWER);

        if (!(expected instanceof Integer expectedValue)) {
            return false;
        }
        if (input == null) {
            return false;
        }
        try {
            return Integer.parseInt(input.trim()) == expectedValue;
        } catch (NumberFormatException ex) {
            // 非数字输入：按错误处理，不向上抛
            return false;
        }
    }

    private int nextInt() {
        return MIN + random.nextInt(MAX - MIN + 1);
    }
}
