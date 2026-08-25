package com.tutorassist.config;

import com.tutorassist.common.Result;
import com.tutorassist.config.dto.SystemInfoVO;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Tag(name = "系统信息", description = "查询应用版本、运行环境及服务状态")
@RestController
@RequestMapping("/api/v1/system")
@RequiredArgsConstructor
public class SystemInfoController {

    private final SystemInfoService systemInfoService;

    @Operation(summary = "获取系统信息")
    @GetMapping("/info")
    public Result<SystemInfoVO> info() {
        return Result.success(systemInfoService.getSystemInfo());
    }
}
