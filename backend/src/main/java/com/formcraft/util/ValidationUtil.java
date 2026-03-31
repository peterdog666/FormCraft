package com.formcraft.util;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.util.HashMap;
import java.util.Map;
import java.util.regex.Pattern;

/**
 * 表单数据校验工具类
 * 支持：必填、最小长度、最大长度、内置模式（email/phone/url/number）、自定义正则
 */
public class ValidationUtil {

    private static final ObjectMapper MAPPER = new ObjectMapper();

    // 内置正则模式
    private static final Pattern EMAIL_PATTERN =
            Pattern.compile("^[a-zA-Z0-9._%+\\-]+@[a-zA-Z0-9.\\-]+\\.[a-zA-Z]{2,}$");
    private static final Pattern PHONE_PATTERN =
            Pattern.compile("^1[3-9]\\d{9}$");
    private static final Pattern URL_PATTERN =
            Pattern.compile("^(https?|ftp)://[^\\s/$.?#].[^\\s]*$", Pattern.CASE_INSENSITIVE);
    private static final Pattern NUMBER_PATTERN =
            Pattern.compile("^-?\\d+(\\.\\d+)?$");

    /**
     * 校验邮箱格式
     */
    public static boolean validateEmail(String email) {
        if (email == null || email.isBlank()) return false;
        return EMAIL_PATTERN.matcher(email.trim()).matches();
    }

    /**
     * 校验手机号（中国大陆 11 位）
     */
    public static boolean validatePhone(String phone) {
        if (phone == null || phone.isBlank()) return false;
        return PHONE_PATTERN.matcher(phone.trim()).matches();
    }

    /**
     * 校验 URL 格式
     */
    public static boolean validateUrl(String url) {
        if (url == null || url.isBlank()) return false;
        return URL_PATTERN.matcher(url.trim()).matches();
    }

    /**
     * 校验数字格式
     */
    public static boolean validateNumber(String value) {
        if (value == null || value.isBlank()) return false;
        return NUMBER_PATTERN.matcher(value.trim()).matches();
    }

    /**
     * 根据 pattern 名称校验值
     *
     * @param value         待校验的值
     * @param pattern       内置模式名称：email / phone / url / number / custom
     * @param customPattern 当 pattern=custom 时使用的自定义正则
     */
    public static boolean validatePattern(String value, String pattern, String customPattern) {
        if (value == null || value.isBlank()) return true; // 空值由 required 规则处理
        return switch (pattern.toLowerCase()) {
            case "email"  -> validateEmail(value);
            case "phone"  -> validatePhone(value);
            case "url"    -> validateUrl(value);
            case "number" -> validateNumber(value);
            case "custom" -> {
                if (customPattern == null || customPattern.isBlank()) yield true;
                yield Pattern.compile(customPattern).matcher(value).matches();
            }
            default -> true;
        };
    }

    /**
     * 校验整个表单数据
     *
     * @param schemaJson          表单字段定义 JSON（数组）
     * @param validationRulesJson 校验规则 JSON
     * @param data                提交的表单数据 Map
     * @return 错误信息 Map，key=fieldId，value=错误提示；空 Map 表示校验通过
     */
    public static Map<String, String> validateFormData(
            String schemaJson,
            String validationRulesJson,
            Map<String, Object> data) {

        Map<String, String> errors = new HashMap<>();

        // 如果没有校验规则，直接通过
        if (validationRulesJson == null || validationRulesJson.isBlank()
                || "null".equalsIgnoreCase(validationRulesJson.trim())) {
            return errors;
        }

        try {
            JsonNode rulesRoot = MAPPER.readTree(validationRulesJson);
            JsonNode fieldsNode = rulesRoot.get("fields");
            if (fieldsNode == null || !fieldsNode.isArray()) {
                return errors;
            }

            for (JsonNode fieldRule : fieldsNode) {
                String fieldId = getTextSafe(fieldRule, "id");
                if (fieldId == null) continue;

                JsonNode rules = fieldRule.get("rules");
                if (rules == null) continue;

                // 获取字段值（转为字符串处理）
                Object rawValue = data != null ? data.get(fieldId) : null;
                String value = rawValue != null ? rawValue.toString().trim() : "";
                boolean isEmpty = value.isEmpty()
                        || (rawValue instanceof java.util.List && ((java.util.List<?>) rawValue).isEmpty());

                String errorMsg = getTextSafe(rules, "errorMsg");

                // 1. 必填校验
                if (getBoolSafe(rules, "required") && isEmpty) {
                    errors.put(fieldId, errorMsg != null ? errorMsg : "此字段必填");
                    continue; // 必填未填，后续规则跳过
                }

                // 非必填且为空，跳过后续校验
                if (isEmpty) continue;

                // 2. 最小长度
                JsonNode minLengthNode = rules.get("minLength");
                if (minLengthNode != null && !minLengthNode.isNull()) {
                    int minLength = minLengthNode.asInt();
                    if (value.length() < minLength) {
                        errors.put(fieldId, errorMsg != null ? errorMsg : "最少需要 " + minLength + " 个字符");
                        continue;
                    }
                }

                // 3. 最大长度
                JsonNode maxLengthNode = rules.get("maxLength");
                if (maxLengthNode != null && !maxLengthNode.isNull()) {
                    int maxLength = maxLengthNode.asInt();
                    if (value.length() > maxLength) {
                        errors.put(fieldId, errorMsg != null ? errorMsg : "最多允许 " + maxLength + " 个字符");
                        continue;
                    }
                }

                // 4. 模式校验
                String pattern = getTextSafe(rules, "pattern");
                if (pattern != null && !pattern.isBlank()) {
                    String customPattern = getTextSafe(rules, "customPattern");
                    if (!validatePattern(value, pattern, customPattern)) {
                        String defaultMsg = switch (pattern.toLowerCase()) {
                            case "email"  -> "请输入有效的邮箱地址";
                            case "phone"  -> "请输入有效的手机号码";
                            case "url"    -> "请输入有效的 URL 地址";
                            case "number" -> "请输入有效的数字";
                            default       -> "格式不正确";
                        };
                        errors.put(fieldId, errorMsg != null ? errorMsg : defaultMsg);
                    }
                }
            }
        } catch (Exception e) {
            // 解析失败时不阻断提交，记录日志即可
            System.err.println("[ValidationUtil] 解析校验规则失败: " + e.getMessage());
        }

        return errors;
    }

    // ---- 辅助方法 ----

    private static String getTextSafe(JsonNode node, String field) {
        JsonNode n = node.get(field);
        return (n != null && !n.isNull()) ? n.asText() : null;
    }

    private static boolean getBoolSafe(JsonNode node, String field) {
        JsonNode n = node.get(field);
        return n != null && !n.isNull() && n.asBoolean();
    }
}
