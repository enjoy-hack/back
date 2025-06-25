package com.example.enjoy.dto.loginDto;

import lombok.Getter;
import org.springframework.stereotype.Service;

@Getter
@Service
public class MemberCommand {
    String sejongPortalId;
    String sejongPortalPassword;

}
