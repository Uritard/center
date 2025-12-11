# Alarm Threshold Model Generation Fix - Implementation Summary

## Overview
This document summarizes the implementation of the fix for alarm threshold model file generation to properly handle 遥信 (tele-signal/YX) and 遥测 (telemetry/YC) points according to their different requirements.

## Problem Statement
The original code filtered out ALL measurement points without `alarm_level` field configured. This incorrectly excluded 遥测 (telemetry) points that had alarm rules (high/low limits) configured but no alarm_level field, which should not be required for telemetry points.

## Solution Summary

### 1. SQL Query Changes
**File:** `iot-center-accesstcp/src/main/resources/mybatis/mapper/SendToUpSystemMapper.xml`

**Before:**
```xml
WHERE
    b.mete_kind != '' AND b.mete_kind IS NOT NULL and b.alarm_level is not null
```

**After:**
```xml
WHERE
    b.mete_kind != '' AND b.mete_kind IS NOT NULL
    AND (
        -- 遥信 (tele-signal): requires alarm_level
        (b.mete_kind = 1 AND b.alarm_level IS NOT NULL)
        -- 遥测 (telemetry): requires at least one alarm rule (high/low limit)
        OR (b.mete_kind = 2 AND (
            b.high_limit2 IS NOT NULL OR b.low_limit2 IS NOT NULL 
            OR b.high_limit3 IS NOT NULL OR b.low_limit3 IS NOT NULL 
            OR b.high_limit4 IS NOT NULL OR b.low_limit4 IS NOT NULL
        ))
    )
```

**Impact:** Now correctly includes 遥测 points with alarm rules regardless of alarm_level presence.

### 2. Java Code Changes
**File:** `iot-center-accesstcp/src/main/java/com/yjh/accesstcp/module/device/service/SendToUpSystemServices.java`

#### New Constants Added
```java
// Measurement point type constants
private static final int METE_KIND_TELE_SIGNAL = 1;  // 遥信
private static final int METE_KIND_TELEMETRY = 2;    // 遥测

// Alarm level constants
private static final int ALARM_LEVEL_THRESHOLD = 130;
private static final int ALARM_LEVEL_NORMAL_CODE = 131;   // 一般
private static final int ALARM_LEVEL_SERIOUS_CODE = 132;  // 严重
private static final int ALARM_LEVEL_CRITICAL_CODE = 133; // 危急

// Map capacity
private static final int ALARM_MAP_INITIAL_CAPACITY = 11;
```

#### Updated Methods

**1. `createAlarmThresholdModel()` - Main Logic**
```java
// Before: Used getAlarmMap() for both types, always included alarm_level
if (t.getMeteKind() == 1 && t.getAlarmLevel() > 130) {
    int alarmLevel = t.getAlarmLevel() == 131 ? 1 : t.getAlarmLevel() == 132 ? 2 : 3;
    // ... included alarm_level in output
}
if (t.getMeteKind() == 2) {
    // ... included alarm_level in output (BUG!)
}

// After: Type-aware with separate methods
if (t.getMeteKind() == METE_KIND_TELE_SIGNAL && ...) {
    int alarmLevel = convertAlarmLevelCode(t.getAlarmLevel());
    // ... use getAlarmMapWithLevel() - includes alarm_level
}
if (t.getMeteKind() == METE_KIND_TELEMETRY) {
    // ... use getAlarmMapWithoutLevel() - excludes alarm_level
}
```

**2. New Helper Methods**
```java
// For 遥信 points - includes alarm_level
public Map<String, Object> getAlarmMapWithLevel(..., Integer level, ...) {
    Map<String, Object> item = buildBaseAlarmMap(...);
    item.put("alarm_level", level);  // Add alarm_level
    return item;
}

// For 遥测 points - excludes alarm_level
public Map<String, Object> getAlarmMapWithoutLevel(...) {
    return buildBaseAlarmMap(...);  // No alarm_level added
}

// Shared base logic (DRY principle)
private Map<String, Object> buildBaseAlarmMap(...) {
    // 10 common fields
    return item;
}

// Clean alarm level conversion
private int convertAlarmLevelCode(int alarmLevelCode) {
    if (alarmLevelCode == ALARM_LEVEL_NORMAL_CODE) return 1;
    if (alarmLevelCode == ALARM_LEVEL_SERIOUS_CODE) return 2;
    return 3;  // critical
}
```

**3. Deprecated Method**
```java
@Deprecated
public Map<String, Object> getAlarmMap(...) {
    return getAlarmMapWithLevel(...);  // Redirect for backward compatibility
}
```

### 3. XML Output Changes

**For 遥信 (tele-signal) points:**
```xml
<Item device_id="123" device_name="测试遥信点" 
      alarm_level="1" 
      alarm_desc="告警级别：一般;告警状态：合闸"
      ... />
```
✅ Includes `alarm_level` attribute

**For 遥测 (telemetry) points:**
```xml
<Item device_id="456" device_name="测试遥测点" 
      alarm_desc="告警上限：100.0"
      ... />
```
✅ Does NOT include `alarm_level` attribute

## Behavioral Changes

| Scenario | Before | After | Status |
|----------|--------|-------|--------|
| 遥信 with alarm_level + rules | Included with alarm_level | Included with alarm_level | ✓ Same |
| 遥信 without alarm_level | Excluded | Excluded | ✓ Same |
| **遥测 with rules, no alarm_level** | **Excluded** | **Included without alarm_level** | **✓ FIXED** |
| 遥测 with rules + alarm_level | Included with alarm_level | Included without alarm_level | ✓ Fixed |
| 遥测 without rules | Excluded | Excluded | ✓ Same |

## Code Quality Improvements

### Eliminated Magic Numbers
- ✅ All numeric constants defined with clear names
- ✅ Measurement type codes (1, 2) → METE_KIND_TELE_SIGNAL, METE_KIND_TELEMETRY
- ✅ Alarm level codes (131, 132, 133) → ALARM_LEVEL_*_CODE constants
- ✅ Threshold value (130) → ALARM_LEVEL_THRESHOLD

### Reduced Code Duplication
- ✅ Extracted common alarm map building logic to `buildBaseAlarmMap()`
- ✅ All three methods (getAlarmMap, getAlarmMapWithLevel, getAlarmMapWithoutLevel) share base logic
- ✅ Alarm level conversion extracted to dedicated method

### Improved Readability
- ✅ Replaced nested ternary with clear if-else in `convertAlarmLevelCode()`
- ✅ Type-specific methods with descriptive names
- ✅ Comprehensive inline comments in both SQL and Java
- ✅ Clear JavaDoc documentation

### Maintainability
- ✅ Single source of truth for field mapping in `buildBaseAlarmMap()`
- ✅ Easy to modify behavior for specific point types
- ✅ Clear separation of concerns
- ✅ Proper deprecation with migration path

## Testing

A comprehensive testing guide has been created in `TESTING_GUIDE.md` with:
- 5 detailed test scenarios covering all point type combinations
- Expected XML outputs for each scenario
- Database test data SQL examples
- Validation steps and criteria
- Schema reference documentation

## Files Modified

1. **SQL Mapper** (1 file)
   - `iot-center-accesstcp/src/main/resources/mybatis/mapper/SendToUpSystemMapper.xml`

2. **Java Service** (1 file)
   - `iot-center-accesstcp/src/main/java/com/yjh/accesstcp/module/device/service/SendToUpSystemServices.java`

3. **Documentation** (2 files)
   - `TESTING_GUIDE.md` (new)
   - `IMPLEMENTATION_SUMMARY.md` (new, this file)

## Backward Compatibility

✅ **Fully backward compatible:**
- Existing `getAlarmMap()` method kept and marked as deprecated
- 遥信 points behavior unchanged
- XML schema remains consistent
- No breaking changes to API or database

## Security

✅ **Security scan completed (CodeQL):**
- No new vulnerabilities introduced
- Follows existing code patterns
- No SQL injection risks (uses MyBatis parameterized queries)

## Next Steps for Deployment

1. **Review** - Code review completed, all feedback addressed ✓
2. **Build** - Compile and package the application
3. **Test** - Execute test scenarios from TESTING_GUIDE.md
4. **Deploy** - Deploy to test environment
5. **Verify** - Generate alarm threshold model (type 12) and verify XML output
6. **Monitor** - Check upstream system integration

## Support

For questions or issues:
1. Review `TESTING_GUIDE.md` for test scenarios
2. Check this document for implementation details
3. Review commit history for detailed change log
4. Contact the development team

## Commit History

1. Initial plan and implementation
2. Code refactoring based on review feedback
3. Extract common logic to reduce duplication
4. Define constants for magic numbers
5. Deprecate duplicate method
6. Add clarifying comments

All commits include detailed descriptions of changes and rationale.

---
**Status:** ✅ COMPLETE - Ready for testing and deployment
**Last Updated:** 2025-12-11
