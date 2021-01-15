package com.yjh.accessvqd.common.utils;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.util.Objects;

/**
 * Created by libin on 2017/7/11.
 */
public class ValueUtil {

    public static boolean toBoolean(Object obj, boolean def) {
        if (Objects.isNull(obj)) {
            return def;
        } else {
            if (obj instanceof Boolean) {
                return (Boolean) obj;
            } else if (obj instanceof String) {
                return Boolean.valueOf((String) obj);
            } else {
                return def;
            }
        }
    }

    public static boolean toBoolean(Object obj) {
        return toBoolean(obj, false);
    }

    public static int safeParseInt(String value, int def) {
        int result = def;
        try {
            result = Integer.parseInt(String.valueOf(value));
        } catch (Exception e) {

        }
        return result;
    }

    public static Boolean getBoolean(String val) {
        Integer intVal = getInteger(val, null);
        return intVal != null ? intVal != 0 : Boolean.valueOf(val);
    }

    public static Integer getInteger(String val, Integer def) {
        Integer result = def;
        try {
            result = Integer.decode(val);
        } catch (Exception e) {
        }

        return result;
    }

    public static Double getDouble(String val, Double def) {
        Double result = def;
        try {
            result = Double.valueOf(val);
        } catch (Exception e) {
        }

        return result;
    }

    public static String Object2String(Object obj, String def) {
        if (obj == null)
            return def;

        return obj.toString();
    }

    public static Integer object2Integer(Object obj) {
        return object2Integer(obj, null);
    }

    public static Integer object2Integer(Object obj, Integer def) {
        if (obj == null)
            return def;

        if (obj instanceof Number) {
            return ((Number) obj).intValue();
        } else {
            return getInteger(obj.toString(), def);
        }
    }

    public static Double object2Double(Object obj, Double def) {
        if (obj == null)
            return def;

        if (obj instanceof Number) {
            return ((Number) obj).doubleValue();
        } else {
            return getDouble(obj.toString(), def);
        }
    }


//    public static List<Integer> IntegersFromString(String string, String div) {
//        if (string == null) {
//            return null;
//        }
//
//        if (div == null)
//            div = Constants.DIV_STRING;
//        // trim all spaces
//        string = string.replaceAll("\\s+","");
//        List<String> values = Arrays.asList(string.split(div));
//
//        return values.stream().map(Ints::tryParse).filter(Objects::nonNull)
//                .collect(Collectors.toList());
//    }

    public static boolean equals(int... values) {
        if (values == null || values.length <= 0) {
            return true;
        }

        for (int i = 1; i < values.length; i++) {
            if (values[i] != values[0]) {
                return false;
            }
        }
        return true;
    }

//    public static List<Integer> samplingIndex(List<String> values, int samples) {
//        if (CollectionUtils.isEmpty(values) || samples <= 0) {
//            return Lists.newArrayList();
//        }
//
//        if (values.size() == samples) { // 值数目和Samples吻合，直接返回
//            List<Integer> result = Lists.newArrayList();
//            for (int i = 0; i < samples; i++) {
//                result.add(i);
//            }
//            return result;
//        } else { // 值数目大于sample，采样
//            double interval = ((double) values.size()) / samples;
//            List<Integer> result = Lists.newArrayList();
//            for (int i = 0; i < samples; i++) {
//                double left = interval * i;
//                double right = interval * (i + 1);
//
//                double maxValue = Double.MIN_VALUE;
//                int maxIndex = 0;
//
//                for (double item = left; item < right; item++) {
//                    int index = (int)Math.round(item);
//                    double value = TextUtil.toDouble(values.get(index));
//                    if (value > maxValue) {
//                        maxValue = value;
//                        maxIndex = index;
//                    }
//                }
//                result.add(maxIndex);
//            }
//            return result;
//        }
//    }

    public static Double floatToDouble(Float f) {
        return f == null ? null : new Double(f.floatValue());
    }

//    public static String minNumericValue(String result, String div) {
//        return numericValue(result, div, true);
//    }
//    public static String maxNumericValue(String result, String div) {
//        return numericValue(result, div, false);
//    }

//    public static String numericValue(String result, String div, boolean min) {
//        if (Strings.isNullOrEmpty(result) || Strings.isNullOrEmpty(div)) {
//            return result;
//        }
//        String[] values = result.split(div);
//        Stream<BigDecimal> stream = StreamHelper.of(values).filter(v -> !Strings.isNullOrEmpty(v))
//                .map(v -> TextUtil.toBigDecimal(v)).filter(Objects::nonNull);
//        BigDecimal value;
//        if (min) {
//            value = stream.min(BigDecimal::compareTo).orElse(null);
//        } else {
//            value = stream.max(BigDecimal::compareTo).orElse(null);
//        }
//        return value != null ? value.toPlainString() : null;
//    }

    public static int toInt(byte[] bytes, int offset, int count, boolean bigEndian) {
        ByteBuffer buffer = ByteBuffer.wrap(bytes, offset, count);
        buffer.order(bigEndian ? ByteOrder.BIG_ENDIAN : ByteOrder.LITTLE_ENDIAN);
        if (count == 1) {
            return buffer.get();
        } else if (count == 2) {
            return buffer.getShort();
        }
        return buffer.getInt();
    }

    public static double maxAbs(double[] a, int offset, int count) {
        double result = 0;
        for (int i = offset; i < a.length && i < offset + count; i++) {
            if (Math.abs(a[i]) > Math.abs(result)) {
                result = a[i];
            }
        }
        return result;
    }

    public static float toFloat(Float floatValue, float defaultValue) {
        return floatValue == null ? defaultValue : floatValue.floatValue();
    }

    public static float toFloat(Float floatValue) {
        return toFloat(floatValue, 0);
    }

    public static float safeParseFloat(String value, float def) {
        float result = def;
        try {
            result = Float.parseFloat(String.valueOf(value));
        } catch (Exception e) {

        }

        return result;
    }

    public static float safeParseFloat(String value) {
        return safeParseFloat(value, 0);
    }
}
