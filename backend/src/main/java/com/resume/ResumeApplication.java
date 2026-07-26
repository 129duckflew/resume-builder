package com.resume;

import com.resume.dto.AuthResponse;
import com.resume.dto.DesensitizeRuleDTO;
import com.resume.dto.ExportDTO;
import com.resume.dto.JsonResumeDTO;
import com.resume.dto.LoginRequest;
import com.resume.dto.PreviewRequest;
import com.resume.dto.RegisterRequest;
import com.resume.dto.ResumeDTO;
import com.resume.dto.ResumeStyleDTO;
import com.resume.dto.ThemeDTO;
import com.resume.dto.VersionDiffResponse;
import com.resume.dto.VersionMeta;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.aot.hint.annotation.RegisterReflectionForBinding;

@SpringBootApplication
@RegisterReflectionForBinding({
        AuthResponse.class,
        DesensitizeRuleDTO.class,
        ExportDTO.class,
        JsonResumeDTO.class,
        LoginRequest.class,
        PreviewRequest.class,
        RegisterRequest.class,
        ResumeDTO.class,
        ResumeStyleDTO.class,
        ThemeDTO.class,
        VersionDiffResponse.class,
        VersionMeta.class
})
public class ResumeApplication {
    public static void main(String[] args) {
        SpringApplication.run(ResumeApplication.class, args);
    }
}
