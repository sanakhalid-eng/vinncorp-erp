package com.vinncorp.erp.modules.projects.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.thymeleaf.TemplateEngine;
import org.thymeleaf.context.Context;

import java.util.Map;

@Service
public class EmailTemplateService {

    private final TemplateEngine templateEngine;

    @Autowired
    public EmailTemplateService(TemplateEngine templateEngine) {
        this.templateEngine = templateEngine;
    }

    public String loadTemplate(String templateName, Map<String, Object> variables) {
        try {
            Context context = new Context();
            context.setVariables(variables);
            
            // Template path relative to templates/ directory
            return templateEngine.process("email/" + templateName, context);
            
        } catch (Exception e) {
            throw new RuntimeException("Failed to load email template: " + templateName, e);
        }
    }
}


