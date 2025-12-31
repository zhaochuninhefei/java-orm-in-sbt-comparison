package com.zhaochuninhefei.orm.comparison.jpa.repository;

import com.zhaochuninhefei.orm.comparison.dto.OrderDetailResult;
import com.zhaochuninhefei.orm.comparison.jpa.entity.OrderMain;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * 订单主表 Repository
 */
@SuppressWarnings({"NullableProblems", "unused"})
@Repository
public interface OrderMainRepository extends JpaRepository<OrderMain, Long> {

    /**
     * 复杂分页查询 - 使用原生SQL
     * 包含CTE、多表JOIN、GROUP BY、HAVING等复杂查询
     *
     * @param regionCode 地区代码
     * @param minActualPriceSum 最小实际价格总和
     * @param pageable 分页参数
     * @return 查询结果列表
     */
    @Query(value = """
            WITH category_sales AS (
                SELECT
                    pc.id AS category_id,
                    pc.category_name,
                    COUNT(od.id) AS sales_count,
                    SUM(od.actual_price) AS total_sales
                FROM product_category pc
                INNER JOIN product p ON pc.id = p.category_id
                INNER JOIN order_detail od ON p.id = od.product_id
                INNER JOIN order_main om ON om.id = od.order_id
                WHERE (:regionCode IS NULL OR om.region_code = :regionCode)
                GROUP BY pc.id, pc.category_name
            )
            SELECT
                om.id AS orderId,
                om.order_no AS orderNo,
                c.customer_name AS customerName,
                CAST(c.customer_type AS UNSIGNED) AS customerType,
                r.region_name AS regionName,
                CAST(om.total_amount AS DOUBLE) AS totalAmount,
                CAST(om.actual_amount AS DOUBLE) AS actualAmount,
                CAST(om.order_status AS UNSIGNED) AS orderStatus,
                COUNT(od.id) AS detailCount,
                CAST(SUM(od.actual_price) AS DOUBLE) AS detailTotalAmount,
                p.product_name AS productName,
                pc.category_name AS categoryName,
                cs.sales_count AS categorySalesCount,
                CAST(cs.total_sales AS DOUBLE) AS categoryTotalSales,
                om.receiver_address AS receiverAddress,
                DATE_FORMAT(om.create_time, '%Y-%m-%d %H:%i:%s') AS createTime
            FROM order_main om
            INNER JOIN customer c ON om.user_id = c.id
            INNER JOIN order_detail od ON om.id = od.order_id
            INNER JOIN product p ON od.product_id = p.id
            LEFT JOIN product_category pc ON p.category_id = pc.id
            LEFT JOIN region r ON om.region_code = r.region_code
            LEFT JOIN category_sales cs ON pc.id = cs.category_id
            WHERE (:regionCode IS NULL OR om.region_code = :regionCode)
            GROUP BY om.id, om.order_no, c.customer_name, c.customer_type,
                     r.region_name, om.total_amount, om.actual_amount, om.order_status,
                     p.product_name, pc.category_name,
                     cs.sales_count, cs.total_sales,
                     om.receiver_address, om.create_time
            HAVING (:minActualPriceSum IS NULL OR SUM(od.actual_price) > :minActualPriceSum)
            """, nativeQuery = true)
    Page<OrderDetailResult> complexQueryByPage(
            @Param("regionCode") String regionCode,
            @Param("minActualPriceSum") Double minActualPriceSum,
            Pageable pageable
    );

}
