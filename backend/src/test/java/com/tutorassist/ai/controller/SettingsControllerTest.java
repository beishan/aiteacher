package com.tutorassist.ai.controller;

import com.tutorassist.ai.gateway.AIGatewayFactory;
import com.tutorassist.ai.mapper.SystemSettingMapper;
import com.tutorassist.ai.service.DockIconService;
import com.tutorassist.ai.service.SiteIconService;
import org.junit.jupiter.api.Test;
import org.springframework.core.io.InputStreamResource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseEntity;

import java.io.ByteArrayInputStream;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class SettingsControllerTest {

    @Test
    void getDockIconUsesImmutableVersionedCache() {
        DockIconService dockIconService = mock(DockIconService.class);
        when(dockIconService.exists("dashboard")).thenReturn(true);
        when(dockIconService.getIcon("dashboard"))
                .thenReturn(new ByteArrayInputStream(new byte[]{1, 2, 3}));

        SettingsController controller = new SettingsController(
                mock(SystemSettingMapper.class),
                mock(AIGatewayFactory.class),
                mock(SiteIconService.class),
                dockIconService
        );

        ResponseEntity<InputStreamResource> response = controller.getDockIcon("dashboard");

        assertThat(response.getHeaders().getFirst(HttpHeaders.CACHE_CONTROL))
                .isEqualTo("max-age=31536000, public, immutable");
    }
}
