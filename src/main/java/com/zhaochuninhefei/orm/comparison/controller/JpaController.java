package com.zhaochuninhefei.orm.comparison.controller;

import com.zhaochuninhefei.orm.comparison.dto.JpaInsertRequest;
import com.zhaochuninhefei.orm.comparison.service.JpaService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * JPA相关API Controller
 * 提供JPA相关的数据库操作API
 */
@RestController
@RequestMapping("/api/jpa")
@SuppressWarnings({"NullableProblems", "unused"})
public class JpaController {

    private final JpaService jpaService;

    public JpaController(JpaService jpaService) {
        this.jpaService = jpaService;
    }

    /**
     * 单表插入API
     *
     * @param request 请求体，包含insertCount参数
     * @return 实际插入的件数
     */
    @PostMapping("/insert")
    public ResponseEntity<Integer> insertData(@RequestBody JpaInsertRequest request) {
        // 获取插入数量
        int insertCount = request.getInsertCount();

        // 限制单次插入数量不超过10000，避免内存问题
        if (insertCount > 10000) {
            insertCount = 10000;
        }

        // 执行插入操作
        int actualInsertCount = jpaService.insertUserProfiles(insertCount);

        return ResponseEntity.ok(actualInsertCount);
    }

    /**
     * 主键更新API
     *
     * @return 影响行数
     */
    @PostMapping("/update/pk")
    public ResponseEntity<Integer> updateByPk() {
        // 执行主键更新操作
        int affectedRows = jpaService.updateUserProfileByPk();

        return ResponseEntity.ok(affectedRows);
    }
}