package com.zhaochuninhefei.orm.comparison.service;

import com.zhaochuninhefei.orm.comparison.dto.DataPrepareResponse;
import com.zhaochuninhefei.orm.comparison.jpa.entity.ConfigDict;
import com.zhaochuninhefei.orm.comparison.jpa.entity.Customer;
import com.zhaochuninhefei.orm.comparison.jpa.entity.OrderDetail;
import com.zhaochuninhefei.orm.comparison.jpa.entity.OrderMain;
import com.zhaochuninhefei.orm.comparison.jpa.entity.Product;
import com.zhaochuninhefei.orm.comparison.jpa.entity.ProductCategory;
import com.zhaochuninhefei.orm.comparison.jpa.entity.Region;
import com.zhaochuninhefei.orm.comparison.jpa.entity.UserProfile;
import com.zhaochuninhefei.orm.comparison.jpa.repository.ConfigDictRepository;
import com.zhaochuninhefei.orm.comparison.jpa.repository.CustomerRepository;
import com.zhaochuninhefei.orm.comparison.jpa.repository.OrderDetailRepository;
import com.zhaochuninhefei.orm.comparison.jpa.repository.OrderMainRepository;
import com.zhaochuninhefei.orm.comparison.jpa.repository.ProductCategoryRepository;
import com.zhaochuninhefei.orm.comparison.jpa.repository.ProductRepository;
import com.zhaochuninhefei.orm.comparison.jpa.repository.RegionRepository;
import com.zhaochuninhefei.orm.comparison.jpa.repository.UserProfileRepository;
import jakarta.persistence.EntityManager;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDateTime;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Random;

/**
 * 数据准备Service
 * 负责为各张表生成测试数据
 */
@Slf4j
@Service
@SuppressWarnings({"SameParameterValue", "java:S1192", "java:S3358", "DuplicatedCode"})
public class DataPrepareService {

    private final UserProfileRepository userProfileRepository;
    private final CustomerRepository customerRepository;
    private final ProductCategoryRepository productCategoryRepository;
    private final RegionRepository regionRepository;
    private final ConfigDictRepository configDictRepository;
    private final ProductRepository productRepository;
    private final OrderMainRepository orderMainRepository;
    private final OrderDetailRepository orderDetailRepository;

    private final EntityManager entityManager;

    @SuppressWarnings("unused")
    @Value("${spring.jpa.hibernate.jdbc.batch_size:100}")
    private int batchSize;

    private final Random random = new Random();

    private static final String[] DEPARTMENTS = {"研发部", "产品部", "市场部", "销售部", "人力资源部", "财务部", "运营部", "客服部"};
    private static final String[] POSITIONS = {"实习生", "专员", "主管", "经理", "总监", "VP"};
    private static final String[] BRANDS = {"品牌A", "品牌B", "品牌C", "品牌D", "品牌E"};
    private static final String[] DICT_TYPES = {"system", "business", "user", "product", "order"};

    public DataPrepareService(UserProfileRepository userProfileRepository,
                              CustomerRepository customerRepository,
                              ProductCategoryRepository productCategoryRepository,
                              RegionRepository regionRepository,
                              ConfigDictRepository configDictRepository,
                              ProductRepository productRepository,
                              OrderMainRepository orderMainRepository,
                              OrderDetailRepository orderDetailRepository,
                              EntityManager entityManager) {
        this.userProfileRepository = userProfileRepository;
        this.customerRepository = customerRepository;
        this.productCategoryRepository = productCategoryRepository;
        this.regionRepository = regionRepository;
        this.configDictRepository = configDictRepository;
        this.productRepository = productRepository;
        this.orderMainRepository = orderMainRepository;
        this.orderDetailRepository = orderDetailRepository;
        this.entityManager = entityManager;
    }

    /**
     * 准备所有测试数据
     */
    @Transactional
    public DataPrepareResponse prepareAllData() {
        log.info("开始准备测试数据...");

        // 在插入新数据前先清空所有相关表
        truncateAllTables();

        Map<String, Integer> tableCounts = new LinkedHashMap<>();

        // 1. 生成商品分类数据（100条）
        int categoryCount = generateProductCategories(100);
        tableCounts.put("product_category", categoryCount);
        log.info("生成商品分类数据：{} 条", categoryCount);

        // 2. 生成地区数据（1000条）
        int regionCount = generateRegions(1000);
        tableCounts.put("region", regionCount);
        log.info("生成地区数据：{} 条", regionCount);

        // 3. 生成配置字典数据（1000条）
        int dictCount = generateConfigDicts(1000);
        tableCounts.put("config_dict", dictCount);
        log.info("生成配置字典数据：{} 条", dictCount);

        // 4. 生成商品数据（10万条）
        int productCount = generateProducts(100000);
        tableCounts.put("product", productCount);
        log.info("生成商品数据：{} 条", productCount);

        // 5. 生成客户数据（10万条）
        int customerCount = generateCustomers(100000);
        tableCounts.put("customer", customerCount);
        log.info("生成客户数据：{} 条", customerCount);

        // 6. 生成用户基础数据（10万条）
        int userProfileCount = generateUserProfiles(100000);
        tableCounts.put("user_profile", userProfileCount);
        log.info("生成用户基础数据：{} 条", userProfileCount);

        // 7. 生成订单主表数据（100万条）
        int orderMainCount = generateOrderMains(1000000);
        tableCounts.put("order_main", orderMainCount);
        log.info("生成订单主表数据：{} 条", orderMainCount);

        // 8. 生成订单明细数据（200万条）
        int orderDetailCount = generateOrderDetails(2000000);
        tableCounts.put("order_detail", orderDetailCount);
        log.info("生成订单明细数据：{} 条", orderDetailCount);

        log.info("所有测试数据准备完成！");
        return new DataPrepareResponse("数据准备完成", tableCounts);
    }

    /**
     * 恢复user_profile表数据
     * 先truncate表，然后重新插入10万数据
     */
    @Transactional
    public int restoreUserProfileData() {
        log.info("开始恢复user_profile表数据...");

        // 清空user_profile表
        entityManager.createNativeQuery("SET FOREIGN_KEY_CHECKS = 0").executeUpdate(); // 临时禁用外键检查
        entityManager.createNativeQuery("TRUNCATE TABLE user_profile").executeUpdate();
        entityManager.createNativeQuery("SET FOREIGN_KEY_CHECKS = 1").executeUpdate(); // 重新启用外键检查

        log.info("已清空user_profile表数据");

        // 生成10万条用户基础数据
        int userProfileCount = generateUserProfiles(100000);
        log.info("恢复user_profile表数据：{} 条", userProfileCount);

        return userProfileCount;
    }

    /**
     * 使用TRUNCATE清空所有表数据
     */
    private void truncateAllTables() {
        // 由于TRUNCATE不遵循外键约束，需要按正确的顺序执行
        entityManager.createNativeQuery("SET FOREIGN_KEY_CHECKS = 0").executeUpdate(); // 临时禁用外键检查

        entityManager.createNativeQuery("TRUNCATE TABLE user_profile").executeUpdate();

        entityManager.createNativeQuery("TRUNCATE TABLE order_detail").executeUpdate();
        entityManager.createNativeQuery("TRUNCATE TABLE order_main").executeUpdate();
        entityManager.createNativeQuery("TRUNCATE TABLE product").executeUpdate();
        entityManager.createNativeQuery("TRUNCATE TABLE customer").executeUpdate();
        entityManager.createNativeQuery("TRUNCATE TABLE region").executeUpdate();
        entityManager.createNativeQuery("TRUNCATE TABLE product_category").executeUpdate();

        entityManager.createNativeQuery("TRUNCATE TABLE config_dict").executeUpdate();

        entityManager.createNativeQuery("SET FOREIGN_KEY_CHECKS = 1").executeUpdate(); // 重新启用外键检查

        log.info("已清空所有相关表数据（使用TRUNCATE）");
    }

    /**
     * 生成用户基础数据
     */
    private int generateUserProfiles(int count) {

        for (int i = 1; i <= count; i++) {
            UserProfile profile = new UserProfile();
            profile.setUsername("user_" + String.format("%06d", i));
            profile.setEmail("user_" + i + "@example.com");
            profile.setPhone("1" + String.format("%010d", Math.abs(random.nextLong() % 10000000000L)));
            profile.setAge(20 + random.nextInt(40));
            profile.setGender(random.nextInt(2) + 1);
            profile.setStatus(random.nextBoolean() ? 1 : 0);
            profile.setDepartment(DEPARTMENTS[random.nextInt(DEPARTMENTS.length)]);
            profile.setPosition(POSITIONS[random.nextInt(POSITIONS.length)]);
            profile.setSalary(BigDecimal.valueOf(5000 + random.nextDouble() * 45000).setScale(2, RoundingMode.HALF_UP));
            profile.setDescription("这是用户" + i + "的描述信息");
            profile.setScore(BigDecimal.valueOf(60 + random.nextDouble() * 40).setScale(2, RoundingMode.HALF_UP));
            profile.setLevel(i % 10 + 1);

            userProfileRepository.save(profile);
            // 每达到 batch_size 数量，就 flush 并 clear 一次
            if (i % batchSize == 0) {
                // flush 将数据同步到数据库（执行批量 SQL）
                entityManager.flush();
                // clear 清除一级缓存，防止内存堆积，也防止对象重复更新
                entityManager.clear();
            }
        }
        // 循环结束后，处理剩余的数据
        entityManager.flush();
        entityManager.clear();

        return count;
    }

    /**
     * 生成客户数据
     */
    private int generateCustomers(int count) {
        for (int i = 1; i <= count; i++) {
            Customer customer = new Customer();
            customer.setCustomerNo("CUST" + String.format("%06d", i));
            customer.setCustomerName("客户" + i);
            customer.setEmail("customer" + i + "@example.com");
            customer.setPhone("1" + String.format("%010d", Math.abs(random.nextLong() % 10000000000L)));
            customer.setCustomerType(random.nextInt(3) + 1);
            customer.setLastLoginTime(LocalDateTime.now().minusDays(random.nextInt(365)));
            customer.setTotalOrders(random.nextInt(100));
            customer.setTotalConsumption(BigDecimal.valueOf(random.nextDouble() * 50000).setScale(2, RoundingMode.HALF_UP));
            customer.setStatus(random.nextBoolean() ? 1 : 0);

            customerRepository.save(customer);

            // 每达到 batch_size 数量，就 flush 并 clear 一次
            if (i % batchSize == 0) {
                entityManager.flush();
                entityManager.clear();
            }
        }

        // 循环结束后，处理剩余的数据
        entityManager.flush();
        entityManager.clear();

        return count;
    }

    /**
     * 生成商品分类数据
     */
    private int generateProductCategories(int count) {
        for (int i = 1; i <= count; i++) {
            ProductCategory category = new ProductCategory();
            category.setCategoryName("分类" + i);
            category.setParentId(i > 10 ? (long) (random.nextInt(10) + 1) : 0L);
            category.setLevel(i <= 10 ? 1 : 2);
            category.setSortOrder(i);
            category.setStatus(1);

            productCategoryRepository.save(category);

            // 每达到 batch_size 数量，就 flush 并 clear 一次
            if (i % batchSize == 0) {
                entityManager.flush();
                entityManager.clear();
            }
        }

        // 循环结束后，处理剩余的数据
        entityManager.flush();
        entityManager.clear();

        return count;
    }

    /**
     * 生成地区数据
     */
    private int generateRegions(int count) {
        for (int i = 1; i <= count; i++) {
            Region region = new Region();
            region.setRegionCode("REG" + String.format("%05d", i));
            region.setRegionName("地区" + i);
            region.setParentId(i > 30 ? (long) (random.nextInt(30) + 1) : 0L);
            region.setLevel(i <= 30 ? 1 : (i <= 300 ? 2 : 3));
            region.setSortOrder(i);
            region.setStatus(1);

            regionRepository.save(region);

            // 每达到 batch_size 数量，就 flush 并 clear 一次
            if (i % batchSize == 0) {
                entityManager.flush();
                entityManager.clear();
            }
        }

        // 循环结束后，处理剩余的数据
        entityManager.flush();
        entityManager.clear();

        return count;
    }

    /**
     * 生成配置字典数据
     */
    private int generateConfigDicts(int count) {
        for (int i = 1; i <= count; i++) {
            ConfigDict dict = new ConfigDict();
            dict.setDictCode("DICT_" + String.format("%04d", i));
            dict.setDictName("字典项" + i);
            dict.setDictValue("VALUE_" + i);
            dict.setDictType(DICT_TYPES[random.nextInt(DICT_TYPES.length)]);
            dict.setSortOrder(i);
            dict.setStatus(random.nextBoolean() ? 1 : 0);
            dict.setRemark("这是字典项" + i + "的说明");

            configDictRepository.save(dict);

            // 每达到 batch_size 数量，就 flush 并 clear 一次
            if (i % batchSize == 0) {
                entityManager.flush();
                entityManager.clear();
            }
        }

        // 循环结束后，处理剩余的数据
        entityManager.flush();
        entityManager.clear();

        return count;
    }

    /**
     * 生成商品数据
     */
    private int generateProducts(int count) {
        for (int i = 1; i <= count; i++) {
            Product product = new Product();
            product.setProductNo("PROD" + String.format("%06d", i));
            product.setProductName("商品名称" + i);
            product.setProductSku("SKU-" + String.format("%06d", i));
            product.setCategoryId((long) (random.nextInt(100) + 1));
            product.setBrand(BRANDS[random.nextInt(BRANDS.length)]);
            product.setPrice(BigDecimal.valueOf(50 + random.nextDouble() * 500).setScale(2, RoundingMode.HALF_UP));
            product.setCostPrice(BigDecimal.valueOf(30 + random.nextDouble() * 200).setScale(2, RoundingMode.HALF_UP));
            product.setStock(random.nextInt(1000));
            product.setSales(random.nextInt(5000));
            product.setStatus(random.nextDouble() > 0.2 ? 1 : 0);

            productRepository.save(product);

            // 每达到 batch_size 数量，就 flush 并 clear 一次
            if (i % batchSize == 0) {
                entityManager.flush();
                entityManager.clear();
            }
        }

        // 循环结束后，处理剩余的数据
        entityManager.flush();
        entityManager.clear();

        return count;
    }

    /**
     * 生成订单主表数据
     */
    private int generateOrderMains(int count) {
        for (int i = 1; i <= count; i++) {
            OrderMain order = new OrderMain();
            order.setOrderNo("ORD" + LocalDateTime.now().format(java.time.format.DateTimeFormatter.ofPattern("yyyyMMdd")) + String.format("%09d", i));
            order.setUserId((long) (random.nextInt(100000) + 1));
            order.setRegionCode("REG" + String.format("%05d", random.nextInt(1000) + 1));
            order.setTotalAmount(BigDecimal.valueOf(100 + random.nextDouble() * 10000).setScale(2, RoundingMode.HALF_UP));
            order.setDiscountAmount(BigDecimal.valueOf(random.nextDouble() * 100).setScale(2, RoundingMode.HALF_UP));
            order.setActualAmount(BigDecimal.valueOf(100 + random.nextDouble() * 9900).setScale(2, RoundingMode.HALF_UP));
            order.setOrderStatus(random.nextInt(5));
            order.setPaymentMethod(random.nextInt(3) + 1);

            if (random.nextDouble() > 0.3) {
                order.setPaymentTime(LocalDateTime.now().minusDays(random.nextInt(365)));
            }
            if (random.nextDouble() > 0.4) {
                order.setShipTime(LocalDateTime.now().minusDays(random.nextInt(360)));
            }
            if (random.nextDouble() > 0.5) {
                order.setFinishTime(LocalDateTime.now().minusDays(random.nextInt(355)));
            }

            order.setReceiverName("收货人" + i);
            order.setReceiverPhone("1" + String.format("%010d", Math.abs(random.nextLong() % 10000000000L)));
            order.setReceiverAddress("北京市朝阳区某街道" + i + "号");
            if (random.nextDouble() > 0.7) {
                order.setRemark("备注信息" + i);
            }

            orderMainRepository.save(order);

            // 每达到 batch_size 数量，就 flush 并 clear 一次
            if (i % batchSize == 0) {
                entityManager.flush();
                entityManager.clear();
            }
        }

        // 循环结束后，处理剩余的数据
        entityManager.flush();
        entityManager.clear();

        return count;
    }

    /**
     * 生成订单明细数据
     * 为每个订单生成2个明细（100万订单 x 2明细 = 200万明细）
     */
    private int generateOrderDetails(int totalCount) {
        int recordCount = 0;

        // 为每个订单生成2个明细
        for (long orderId = 1; orderId <= 1000000; orderId++) {
            for (int j = 1; j <= 2; j++) {
                OrderDetail detail = new OrderDetail();
                detail.setOrderId(orderId);
                detail.setProductId((long) (random.nextInt(100000) + 1));
                detail.setProductName("商品名称" + orderId + "-" + j);
                detail.setProductSku("SKU" + String.format("%06d", random.nextInt(100000) + 1));
                detail.setQuantity(random.nextInt(10) + 1);
                detail.setUnitPrice(BigDecimal.valueOf(50 + random.nextDouble() * 500).setScale(2, RoundingMode.HALF_UP));
                detail.setTotalPrice(BigDecimal.valueOf((50 + random.nextDouble() * 500) * (1 + random.nextDouble() * 10)).setScale(2, RoundingMode.HALF_UP));
                detail.setDiscount(BigDecimal.valueOf(random.nextDouble() * 20).setScale(2, RoundingMode.HALF_UP));
                detail.setActualPrice(BigDecimal.valueOf((50 + random.nextDouble() * 500) * (1 + random.nextDouble() * 10) - random.nextDouble() * 20).setScale(2, RoundingMode.HALF_UP));

                orderDetailRepository.save(detail);
                recordCount++;

                // 每达到 batch_size 数量，就 flush 并 clear 一次
                if (recordCount % batchSize == 0) {
                    entityManager.flush();
                    entityManager.clear();
                }
            }
        }

        // 循环结束后，处理剩余的数据
        entityManager.flush();
        entityManager.clear();

        return totalCount;
    }
}