/**
 * 宠物商店 PetStore · 轻交互脚本
 *
 * 设计约束（任务卡 T-20261113-03 第 4 条）：
 *   - 纯原生 JavaScript，零框架、零依赖、零 CDN，离线可运行；
 *   - 只做「轻交互」：导航高亮、商品图悬停放大、图片加载失败兜底；
 *   - 不伪造任何未实现的功能（购物车按钮保持 disabled，本脚本不为其绑定任何行为）。
 */
(function () {
    "use strict";

    /* ------------------------------------------------------------------
     * 1. 顶部导航高亮
     *    规则（T-20261113-17 修正后）：
     *      /catalog             → 首页
     *      /catalog/{id}        → 分类（此前被误判为首页，已修）
     *      /category、/product  → 分类
     *      /account/login       → 登录（高亮「登录」入口自身）
     *      /account/register    → 注册（高亮「注册」入口自身）
     *      /account/**、其它     → 不高亮任何主导航项（此前默认落到"首页"，已修）
     *    匹配方式：优先 data-nav；登录/注册入口没有 data-nav，改按 href 前缀匹配。
     * ------------------------------------------------------------------ */
    function highlightNav() {
        var links = document.querySelectorAll(".nav-link");
        if (!links.length) {
            return;
        }

        var path = (window.location.pathname || "/").toLowerCase();
        var current = "";

        if (path.indexOf("/account/login") === 0) {
            current = "login";
        } else if (path.indexOf("/account/register") === 0) {
            current = "register";
        } else if (path.indexOf("/product") === 0 || path.indexOf("/category") === 0) {
            // 商品详情与分类列表都属于分类体系
            current = "category";
        } else if (path === "/catalog" || path === "/catalog/" || path === "/") {
            current = "catalog";
        } else if (path.indexOf("/catalog/") === 0) {
            // /catalog/{id} 是某分类下的商品列表，不是首页
            current = "category";
        }

        Array.prototype.forEach.call(links, function (link) {
            var htmlLink = link.tagName === "A" ? link : null;
            var nav = link.getAttribute("data-nav");
            var href = htmlLink ? (htmlLink.getAttribute("href") || "").toLowerCase() : "";
            var isCurrent = false;

            if (nav) {
                isCurrent = nav === current;
            } else if (current === "login") {
                isCurrent = href.indexOf("/account/login") === 0;
            } else if (current === "register") {
                isCurrent = href.indexOf("/account/register") === 0;
            }

            link.classList.toggle("is-active", isCurrent);
            if (isCurrent) {
                link.setAttribute("aria-current", "page");
            } else {
                link.removeAttribute("aria-current");
            }
        });
    }

    /* ------------------------------------------------------------------
     * 2. 商品图悬停放大
     *    CSS 已用 :hover 实现主要效果；此处补充键盘聚焦与不支持 :hover
     *    的场景，通过切换 .is-zoomed 类复用同一套 transform。
     * ------------------------------------------------------------------ */
    function bindZoom() {
        var cards = document.querySelectorAll(".product-card");
        if (!cards.length) {
            return;
        }

        Array.prototype.forEach.call(cards, function (card) {
            var enter = function () {
                card.classList.add("is-zoomed");
            };
            var leave = function () {
                card.classList.remove("is-zoomed");
            };

            card.addEventListener("mouseenter", enter);
            card.addEventListener("mouseleave", leave);
            card.addEventListener("focusin", enter);
            card.addEventListener("focusout", leave);
        });
    }

    /* ------------------------------------------------------------------
     * 3. 商品图加载失败兜底
     *    图片由 res-01 提供、dev-01 定路径；若某张占位图缺失，
     *    退化为带边框的灰底块，避免出现浏览器默认的破图图标。
     * ------------------------------------------------------------------ */
    function bindImageFallback() {
        var images = document.querySelectorAll(".product-card__img, .detail__img");
        if (!images.length) {
            return;
        }

        Array.prototype.forEach.call(images, function (img) {
            img.addEventListener("error", function () {
                img.classList.add("is-missing");
                img.setAttribute("alt", (img.getAttribute("alt") || "商品图片") + "（图片暂缺）");
            });

            // 已缓存且已失败的图片不会再触发 error 事件
            if (img.complete && img.naturalWidth === 0) {
                img.classList.add("is-missing");
            }
        });
    }

    /* ------------------------------------------------------------------
     * 4. 规格下拉：仅同步「当前选中项」的视觉提示
     *    不提交表单、不写购物车 —— 购物车功能待实现。
     * ------------------------------------------------------------------ */
    function bindSpecSelect() {
        var select = document.getElementById("spec-select");
        if (!select) {
            return;
        }

        var note = document.createElement("p");
        note.className = "detail__spec-picked";
        note.setAttribute("aria-live", "polite");
        select.parentNode.appendChild(note);

        var sync = function () {
            var index = select.selectedIndex;
            var text = index >= 0 ? select.options[index].textContent.trim() : "";
            note.textContent = text ? "当前选中规格：" + text : "";
        };

        select.addEventListener("change", sync);
        sync();
    }

    /* ------------------------------------------------------------------
     * 启动
     * ------------------------------------------------------------------ */
    function init() {
        highlightNav();
        bindZoom();
        bindImageFallback();
        bindSpecSelect();
    }

    if (document.readyState === "loading") {
        document.addEventListener("DOMContentLoaded", init);
    } else {
        init();
    }
})();
