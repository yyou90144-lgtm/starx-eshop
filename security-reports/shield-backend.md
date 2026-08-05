# DeepSec Security Report

Findings: **7**
critical: 0 | high: 1 | medium: 6 | low: 0 | info: 0

## [MEDIUM] Spring Security permitAll() may expose an endpoint.

- Location: `C:\Users\WJG\Desktop\eshop-release-v1.01\eshop-release-v1.0\backend\src\main\java\com\eshop\backend\security\SecurityConfig.java:56`
- Rule: `insecure_config_spring_permit_all`
- Layer: `L1`

Spring Security permitAll() may expose an endpoint.

```text
.permitAll(
```

Remediation: Verify the endpoint is intentionally public.

## [MEDIUM] Spring Security permitAll() may expose an endpoint.

- Location: `C:\Users\WJG\Desktop\eshop-release-v1.01\eshop-release-v1.0\backend\src\main\java\com\eshop\backend\security\SecurityConfig.java:57`
- Rule: `insecure_config_spring_permit_all`
- Layer: `L1`

Spring Security permitAll() may expose an endpoint.

```text
.permitAll(
```

Remediation: Verify the endpoint is intentionally public.

## [MEDIUM] Spring Security permitAll() may expose an endpoint.

- Location: `C:\Users\WJG\Desktop\eshop-release-v1.01\eshop-release-v1.0\backend\src\main\java\com\eshop\backend\security\SecurityConfig.java:58`
- Rule: `insecure_config_spring_permit_all`
- Layer: `L1`

Spring Security permitAll() may expose an endpoint.

```text
.permitAll(
```

Remediation: Verify the endpoint is intentionally public.

## [MEDIUM] Spring Security permitAll() may expose an endpoint.

- Location: `C:\Users\WJG\Desktop\eshop-release-v1.01\eshop-release-v1.0\backend\src\main\java\com\eshop\backend\security\SecurityConfig.java:59`
- Rule: `insecure_config_spring_permit_all`
- Layer: `L1`

Spring Security permitAll() may expose an endpoint.

```text
.permitAll(
```

Remediation: Verify the endpoint is intentionally public.

## [MEDIUM] Spring Security permitAll() may expose an endpoint.

- Location: `C:\Users\WJG\Desktop\eshop-release-v1.01\eshop-release-v1.0\backend\src\main\java\com\eshop\backend\security\SecurityConfig.java:60`
- Rule: `insecure_config_spring_permit_all`
- Layer: `L1`

Spring Security permitAll() may expose an endpoint.

```text
.permitAll(
```

Remediation: Verify the endpoint is intentionally public.

## [MEDIUM] Spring Security permitAll() may expose an endpoint.

- Location: `C:\Users\WJG\Desktop\eshop-release-v1.01\eshop-release-v1.0\backend\src\main\java\com\eshop\backend\security\SecurityConfig.java:62`
- Rule: `insecure_config_spring_permit_all`
- Layer: `L1`

Spring Security permitAll() may expose an endpoint.

```text
.permitAll(
```

Remediation: Verify the endpoint is intentionally public.

## [HIGH] Spring Security CSRF protection is disabled.

- Location: `C:\Users\WJG\Desktop\eshop-release-v1.01\eshop-release-v1.0\backend\src\main\java\com\eshop\backend\security\SecurityConfig.java:49`
- Rule: `ai_pattern_spring_csrf_disabled`
- Layer: `L1`

Spring Security CSRF protection is disabled.

```text
.csrf(csrf -> csrf.disable())
```

Remediation: Keep CSRF enabled for browser-authenticated flows.
