package com.vinncorp.erp.platform.auth.service;

import com.vinncorp.erp.platform.auth.dto.request.LoginRequest;
import com.vinncorp.erp.platform.user.dto.request.RegisterRequest;
import com.vinncorp.erp.platform.auth.dto.response.LoginResponse;
import com.vinncorp.erp.platform.user.dto.response.RegisterResponse;

public interface AuthService {

    RegisterResponse register(RegisterRequest request);

    LoginResponse login(LoginRequest request);

}
