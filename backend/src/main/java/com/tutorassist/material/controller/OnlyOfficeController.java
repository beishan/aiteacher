package com.tutorassist.material.controller;

import com.tutorassist.common.Result;
import com.tutorassist.material.dto.MaterialVO;
import com.tutorassist.material.service.MaterialService;
import com.tutorassist.material.service.OnlyOfficeService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@Slf4j
@Tag(name = "OnlyOffice 文档编辑", description = "在线文档编辑集成")
@RestController
@RequestMapping("/api/v1/onlyoffice")
@RequiredArgsConstructor
public class OnlyOfficeController {

    private final OnlyOfficeService onlyOfficeService;
    private final MaterialService materialService;

    @Operation(summary = "获取编辑器配置")
    @GetMapping("/config/{materialId}")
    public Result<Map<String, Object>> getEditorConfig(@PathVariable Long materialId) {
        MaterialVO material = materialService.getMaterial(materialId);
        return Result.success(onlyOfficeService.buildEditorConfig(material));
    }

    @Operation(summary = "OnlyOffice 回调（编辑完成保存）")
    @PostMapping("/callback")
    public Map<String, Object> handleCallback(@RequestBody Map<String, Object> body) {
        try {
            onlyOfficeService.handleCallback(body);
        } catch (Exception e) {
            log.error("OnlyOffice 回调处理失败", e);
        }
        // OnlyOffice 要求返回 {"error": 0} 表示成功
        return Map.of("error", 0);
    }
}
