package com.vinncorp.erp.modules.projects.service;

import com.vinncorp.erp.modules.projects.entity.FeatureFlag;
import com.vinncorp.erp.modules.projects.repository.FeatureFlagRepository;
import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
@Slf4j
public class FeatureFlagService {

    private final FeatureFlagRepository featureFlagRepository;

    @PostConstruct
    public void seedDefaultFlags() {
        seedIfMissing("slack.enabled", "Enable Slack integration");
        seedIfMissing("webhooks.enabled", "Enable outgoing webhooks");
        seedIfMissing("analytics.enabled", "Enable analytics and reporting");
        seedIfMissing("timeTracking.enabled", "Enable time tracking");
    }

    private void seedIfMissing(String key, String description) {
        if (featureFlagRepository.findByFlagKey(key).isEmpty()) {
            FeatureFlag flag = new FeatureFlag();
            flag.setFlagKey(key);
            flag.setFlagValue(true);
            flag.setDescription(description);
            featureFlagRepository.save(flag);
            log.info("Seeded feature flag: {} = {}", key, true);
        }
    }

    public boolean isEnabled(String flagKey) {
        return featureFlagRepository.findByFlagKey(flagKey)
                .map(FeatureFlag::isFlagValue)
                .orElse(false);
    }

    public Map<String, Boolean> getAllFlags() {
        List<FeatureFlag> flags = featureFlagRepository.findAll();
        Map<String, Boolean> result = new LinkedHashMap<>();
        for (FeatureFlag flag : flags) {
            result.put(flag.getFlagKey(), flag.isFlagValue());
        }
        return result;
    }

    public void setFlag(String flagKey, boolean value) {
        FeatureFlag flag = featureFlagRepository.findByFlagKey(flagKey)
                .orElseGet(() -> {
                    FeatureFlag f = new FeatureFlag();
                    f.setFlagKey(flagKey);
                    return f;
                });
        flag.setFlagValue(value);
        featureFlagRepository.save(flag);
    }
}



