package com.vinncorp.erp.core.auth.service;

import com.vinncorp.erp.core.auth.request.LoginRequest;
import com.vinncorp.erp.core.user.request.RegisterRequest;
import com.vinncorp.erp.core.auth.response.LoginResponse;
import com.vinncorp.erp.core.user.response.RegisterResponse;

public interface AuthService {

    RegisterResponse register(RegisterRequest request);

    LoginResponse login(LoginRequest request);

}
