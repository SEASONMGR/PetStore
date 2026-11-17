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
     *    规则：/category 与 /product 归入「分类」，/catalog 与根路径归入「首页」。
     *    页面上的 .nav-link 带 data-nav="catalog|category" 供匹配。
     * ------------------------------------------------------------------ */
    function highlightNav() {
        var links = document.querySelectorAll(".nav-link[data-nav]");
        if (!links.length) {
            return;
        }

        var path = (window.location.pathname || "/").toLowerCase();
        var current = "catalog";

        if (path.indexOf("/product") === 0) {
            // 商品详情属于分类体系的一部分
            current = "category";
        } else if (path.indexOf("/category") === 0) {
            current = "category";
        } else if (path.indexOf("/catalog") === 0) {
            current = "catalog";
        }

        Array.prototype.forEach.call(links, function (link) {
            var isCurrent = link.getAttribute("data-nav") === current;
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
