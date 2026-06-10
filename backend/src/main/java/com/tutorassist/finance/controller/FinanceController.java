package com.tutorassist.finance.controller;

import com.tutorassist.common.PageResult;
import com.tutorassist.common.Result;
import com.tutorassist.finance.dto.*;
import com.tutorassist.finance.service.FinanceService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Tag(name = "收入管理", description = "费用记录查询、收入统计、趋势分析")
@RestController
@RequestMapping("/api/v1/finance")
@RequiredArgsConstructor
public class FinanceController {

    private final FinanceService financeService;

    @Operation(summary = "费用记录列表")
    @GetMapping("/records")
    public Result<PageResult<FinanceRecordVO>> listRecords(FinanceRecordQuery query) {
        return Result.success(financeService.listRecords(query));
    }

    @Operation(summary = "收入摘要统计")
    @GetMapping("/summary")
    public Result<FinanceSummaryVO> getSummary() {
        return Result.success(financeService.getSummary());
    }

    @Operation(summary = "月度收入趋势")
    @GetMapping("/trend")
    public Result<List<MonthlyTrendVO>> getMonthlyTrend(
            @RequestParam(defaultValue = "12") Integer months) {
        return Result.success(financeService.getMonthlyTrend(months));
    }
}
