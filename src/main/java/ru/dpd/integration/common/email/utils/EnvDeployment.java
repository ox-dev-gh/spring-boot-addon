package ru.dpd.integration.common.email.utils;

import java.util.Arrays;
import java.util.Optional;

/**
 * Среды развертывание в k8s.
 * Должна быть определена переменная ENV_NAME,
 * можно пробросить штатную CI_ENVIRONMENT_NAME переменную при развертывании приложения в deployment.yaml
 */
public enum EnvDeployment {
    DEV, STAGE, RC, PROD, UNKNOWN;
    public static EnvDeployment get(String name) {
        Optional<EnvDeployment> optionalEnvName =
                Arrays.stream(EnvDeployment.values()).filter(envDeployment -> envDeployment.name().equalsIgnoreCase(name)).findFirst();
        return optionalEnvName.orElse(UNKNOWN);
    }
    /**
     * Статический метод получения среды развертывание в k8s.
     * @return enum EnvDeployment;
     */
    public static EnvDeployment getEnvApp() {
        return EnvDeployment.get(System.getenv("ENV_NAME"));
    }
}
