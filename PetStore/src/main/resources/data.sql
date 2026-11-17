-- ============================================================
-- PetStore 宠物商店 CMS —— 初始化数据（须同时兼容 H2 MySQL 模式 与 MySQL 5.7）
-- 内置：1 个管理员账号、3 个分类（犬/猫/小宠）、每类 4 个商品、每个商品 1–3 个 item
-- 图片路径统一为 /images/products/<文件名>.png，对应文件由 res-01 提供，
-- 本脚本只写路径，不生成任何图片文件。
-- 注1：显式写主键，便于 product/item 外键引用；AUTO_INCREMENT 列允许显式赋值。
-- 注2：布尔列 admin 用 1/0 而非 TRUE/FALSE —— MySQL 5.7 里 TRUE/FALSE 只是别名，
--      显式写 1/0 与 TINYINT(1) 语义更直接，避免两库解析差异。
-- 注3：日期统一 'YYYY-MM-DD' 字面量，MySQL 与 H2 均接受。
-- ============================================================

-- ---------- 账号：管理员 1 个 + 普通顾客 1 个（便于订单演示） ----------
INSERT INTO account (id, username, password, email, first_name, last_name, phone, address, city, state, zip, country, status, admin) VALUES
(1, 'admin', 'admin123', 'admin@petstore.local', '管理', '员', '13800000000', '示例路 1 号', '北京', '北京', '100000', 'CN', 'ACTIVE', 1),
(2, 'alice', 'alice123', 'alice@petstore.local', 'Alice', 'Wang', '13900000001', '示例路 2 号', '上海', '上海', '200000', 'CN', 'ACTIVE', 0);


-- ---------- 分类 ----------
INSERT INTO category (id, catid, name, description, sort_order) VALUES
(1, 'DOGS', '犬类', '各类犬只与犬用商品', 10),
(2, 'CATS', '猫类', '各类猫咪与猫用商品', 20),
(3, 'SMALL_ANIMALS', '小宠', '仓鼠、兔子、鸟等小动物用品', 30);

-- ---------- 商品（每类 4 个） ----------
INSERT INTO product (id, productid, category_id, name, description, image_path, list_price, status, sort_order) VALUES
-- 犬类
(1,  'DOG-001', 1, '金毛寻回犬幼犬', '金毛寻回犬，性格温顺，适合家庭饲养。',      '/images/products/dog-golden.png',        2800.00, 'ON_SALE', 10),
(2,  'DOG-002', 1, '柯基犬幼犬',     '短腿柯基，活泼亲人。',                        '/images/products/dog-corgi.png',         3200.00, 'ON_SALE', 20),
(3,  'DOG-003', 1, '中型犬粮',       '营养均衡的中型犬全价干粮。',                  '/images/products/dog-food-medium.png',    189.00, 'ON_SALE', 30),
(4,  'DOG-004', 1, '犬用牵引绳',     '耐磨尼龙牵引绳，适用于中小型犬。',            '/images/products/dog-leash.png',           69.00, 'ON_SALE', 40),
-- 猫类
(5,  'CAT-001', 2, '英国短毛猫',     '英短蓝猫，性格安静。',                        '/images/products/cat-bsh.png',           2600.00, 'ON_SALE', 10),
(6,  'CAT-002', 2, '布偶猫',         '布偶猫幼猫，粘人温顺。',                      '/images/products/cat-ragdoll.png',       4800.00, 'ON_SALE', 20),
(7,  'CAT-003', 2, '猫粮（成猫）',   '高蛋白成猫全价粮。',                          '/images/products/cat-food-adult.png',     159.00, 'ON_SALE', 30),
(8,  'CAT-004', 2, '猫砂盆',         '带盖防溅猫砂盆，易清洁。',                    '/images/products/cat-litter-box.png',     129.00, 'ON_SALE', 40),
-- 小宠
(9,  'SM-001',  3, '仓鼠（布丁）',   '布丁仓鼠，适合新手饲养。',                    '/images/products/small-hamster.png',       45.00, 'ON_SALE', 10),
(10, 'SM-002',  3, '垂耳兔',         '荷兰垂耳兔，温和亲人。',                      '/images/products/small-rabbit.png',       320.00, 'ON_SALE', 20),
(11, 'SM-003',  3, '仓鼠笼',         '双层仓鼠笼，含跑轮与水壶。',                  '/images/products/small-hamster-cage.png', 198.00, 'ON_SALE', 30),
(12, 'SM-004',  3, '兔粮',           '成年兔专用颗粒粮。',                          '/images/products/small-rabbit-food.png',   58.00, 'ON_SALE', 40);

-- ---------- SKU（每个商品 1–3 个） ----------
INSERT INTO item (id, itemid, product_id, name, description, list_price, unit_cost, quantity, status, image_path) VALUES
-- DOG-001 金毛（2 个）
(1,  'DOG-001-01', 1, '公犬 / 2 月龄',   '已接种第一针疫苗',      2800.00, 1900.00,  3, 'INSTOCK',     '/images/products/dog-golden-male.png'),
(2,  'DOG-001-02', 1, '母犬 / 2 月龄',   '已接种第一针疫苗',      3000.00, 2100.00,  2, 'INSTOCK',     '/images/products/dog-golden-female.png'),
-- DOG-002 柯基（2 个）
(3,  'DOG-002-01', 2, '公犬 / 3 月龄',   '已驱虫',                3200.00, 2400.00,  2, 'INSTOCK',     '/images/products/dog-corgi-male.png'),
(4,  'DOG-002-02', 2, '母犬 / 3 月龄',   '已驱虫',                3500.00, 2600.00,  0, 'OUTOFSTOCK',  '/images/products/dog-corgi-female.png'),
-- DOG-003 中型犬粮（2 个）
(5,  'DOG-003-01', 3, '5kg 装',          '含鸡肉与糙米',           189.00,  120.00, 50, 'INSTOCK',     '/images/products/dog-food-medium-5kg.png'),
(6,  'DOG-003-02', 3, '10kg 装',         '家庭装更划算',           339.00,  230.00, 20, 'INSTOCK',     '/images/products/dog-food-medium-10kg.png'),
-- DOG-004 牵引绳（1 个）
(7,  'DOG-004-01', 4, '标准款 / 蓝色',   '长 1.5m',                69.00,   32.00, 100, 'INSTOCK',     '/images/products/dog-leash-blue.png'),
-- CAT-001 英短（2 个）
(8,  'CAT-001-01', 5, '公猫 / 3 月龄',   '已接种疫苗',            2600.00, 1800.00,  2, 'INSTOCK',     '/images/products/cat-bsh-male.png'),
(9,  'CAT-001-02', 5, '母猫 / 3 月龄',   '已接种疫苗',            2800.00, 1950.00,  1, 'INSTOCK',     '/images/products/cat-bsh-female.png'),
-- CAT-002 布偶（2 个）
(10, 'CAT-002-01', 6, '公猫 / 3 月龄',   '带血统证明',            4800.00, 3200.00,  1, 'INSTOCK',     '/images/products/cat-ragdoll-male.png'),
(11, 'CAT-002-02', 6, '母猫 / 3 月龄',   '带血统证明',            5200.00, 3600.00,  1, 'INSTOCK',     '/images/products/cat-ragdoll-female.png'),
-- CAT-003 猫粮（3 个）
(12, 'CAT-003-01', 7, '2kg 装',          '鸡肉味',                159.00,  95.00,  60, 'INSTOCK',     '/images/products/cat-food-adult-2kg.png'),
(13, 'CAT-003-02', 7, '4kg 装',          '鸡肉味家庭装',          289.00,  180.00, 30, 'INSTOCK',     '/images/products/cat-food-adult-4kg.png'),
(14, 'CAT-003-03', 7, '2kg 装（三文鱼）', '三文鱼味',              179.00,  110.00, 25, 'INSTOCK',     '/images/products/cat-food-adult-salmon-2kg.png'),
-- CAT-004 猫砂盆（1 个）
(15, 'CAT-004-01', 8, '大号 / 灰色',     '含猫砂铲',              129.00,  70.00,  40, 'INSTOCK',     '/images/products/cat-litter-box-gray.png'),
-- SM-001 仓鼠（2 个）
(16, 'SM-001-01',  9, '公鼠',            '布丁仓鼠，1 月龄',      45.00,   20.00,  10, 'INSTOCK',     '/images/products/small-hamster-male.png'),
(17, 'SM-001-02',  9, '母鼠',            '布丁仓鼠，1 月龄',      48.00,   22.00,   8, 'INSTOCK',     '/images/products/small-hamster-female.png'),
-- SM-002 垂耳兔（2 个）
(18, 'SM-002-01', 10, '公兔 / 2 月龄',   '已驱虫',                320.00, 200.00,   4, 'INSTOCK',     '/images/products/small-rabbit-male.png'),
(19, 'SM-002-02', 10, '母兔 / 2 月龄',   '已驱虫',                350.00, 220.00,   3, 'INSTOCK',     '/images/products/small-rabbit-female.png'),
-- SM-003 仓鼠笼（1 个）
(20, 'SM-003-01', 11, '双层标准款',      '含跑轮、水瓶、食盆',    198.00, 120.00,  15, 'INSTOCK',     '/images/products/small-hamster-cage-std.png'),
-- SM-004 兔粮（2 个）
(21, 'SM-004-01', 12, '1kg 装',          '成年兔专用',            58.00,   30.00,  35, 'INSTOCK',     '/images/products/small-rabbit-food-1kg.png'),
(22, 'SM-004-02', 12, '2kg 装',          '成年兔专用家庭装',      108.00,  58.00,  18, 'INSTOCK',     '/images/products/small-rabbit-food-2kg.png');

-- ---------- 订单状态字典 ----------
INSERT INTO order_status (id, code, name, description, sort_order) VALUES
(1, 'P', '待付款', '订单已提交，等待付款', 10),
(2, 'C', '已完成', '订单已付款并完成', 20),
(3, 'D', '已发货', '订单已发货，运输中', 30),
(4, 'X', '已取消', '订单已取消', 40);

-- ---------- 示例订单 ----------
INSERT INTO orders (id, orderid, account_id, status_id, order_date, ship_address, ship_city, ship_state, ship_zip, ship_country, total_price) VALUES
(1, 1000, 2, 2, DATE '2026-11-01', '示例路 2 号', '上海', '上海', '200000', 'CN', 258.00),
(2, 1001, 2, 1, DATE '2026-11-12', '示例路 2 号', '上海', '上海', '200000', 'CN', 577.00);

INSERT INTO lineitem (id, order_id, item_id, quantity, unit_price) VALUES
(1, 1, 12, 1, 159.00),
(2, 1, 15, 1, 129.00),
(3, 2, 5,  1, 189.00),
(4, 2, 13, 1, 289.00),
(5, 2, 15, 1, 129.00);
