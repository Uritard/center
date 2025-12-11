# Alarm Threshold Model Generation Testing Guide

## Overview
This document describes how to test the alarm threshold model generation changes for 遥信 (tele-signal/YX) and 遥测 (telemetry/YC) points.

## Changes Made

### 1. SQL Query Update (SendToUpSystemMapper.xml)
**Location:** `iot-center-accesstcp/src/main/resources/mybatis/mapper/SendToUpSystemMapper.xml` (lines 667-682)

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
        (b.mete_kind = 1 AND b.alarm_level IS NOT NULL)
        OR (b.mete_kind = 2 AND (b.high_limit2 IS NOT NULL OR b.low_limit2 IS NOT NULL 
            OR b.high_limit3 IS NOT NULL OR b.low_limit3 IS NOT NULL 
            OR b.high_limit4 IS NOT NULL OR b.low_limit4 IS NOT NULL))
    )
```

### 2. Java Code Updates (SendToUpSystemServices.java)
**Location:** `iot-center-accesstcp/src/main/java/com/yjh/accesstcp/module/device/service/SendToUpSystemServices.java`

**Changes:**
- Updated `createAlarmThresholdModel()` method (lines 508-557)
- Added `getAlarmMapWithLevel()` method (lines 631-650) - for 遥信 points
- Added `getAlarmMapWithoutLevel()` method (lines 655-674) - for 遥测 points

## Test Scenarios

### Test Case 1: 遥信 with alarm_level and alarm rules
**Description:** 遥信 point with alarm_level configured and alarm rules should be included with alarm_level field in output

**Test Data:**
```sql
-- Insert test 遥信 point
INSERT INTO t_std_devicemete (device_mete_id, mete_kind, alarm_level, mete_name, ...)
VALUES (1001, 1, 131, '测试遥信点1', ...);
```

**Expected Result:**
- Point should be retrieved by SQL query
- XML output should include `alarm_level="1"` attribute
- alarm_desc should include "告警级别：一般"

**Verification:**
1. Trigger alarm threshold model generation (type "12")
2. Check generated XML file: `{stationCode}/Model/alarm_threshold_{stationCode}.xml`
3. Verify XML contains:
```xml
<Item device_id="..." device_name="测试遥信点1" alarm_level="1" alarm_desc="告警级别：一般;告警状态：..." .../>
```

### Test Case 2: 遥信 without alarm_level
**Description:** 遥信 point without alarm_level should be excluded from output

**Test Data:**
```sql
-- Insert test 遥信 point without alarm_level
INSERT INTO t_std_devicemete (device_mete_id, mete_kind, alarm_level, mete_name, ...)
VALUES (1002, 1, NULL, '测试遥信点2', ...);
```

**Expected Result:**
- Point should NOT be retrieved by SQL query
- Point should NOT appear in XML output

**Verification:**
1. Trigger alarm threshold model generation
2. Check generated XML file
3. Verify "测试遥信点2" does NOT appear in output

### Test Case 3: 遥测 with alarm rules (no alarm_level required)
**Description:** 遥测 point with high/low limits should be included WITHOUT alarm_level field in output

**Test Data:**
```sql
-- Insert test 遥测 point with alarm rules but no alarm_level
INSERT INTO t_std_devicemete (device_mete_id, mete_kind, alarm_level, high_limit2, low_limit2, high_limit3, low_limit3, mete_name, ...)
VALUES (2001, 2, NULL, 100.0, 10.0, 150.0, 5.0, '测试遥测点1', ...);
```

**Expected Result:**
- Point should be retrieved by SQL query (because high/low limits exist)
- XML output should include multiple items (one for each limit)
- XML output should NOT include `alarm_level` attribute
- alarm_desc should NOT include "告警级别"

**Verification:**
1. Trigger alarm threshold model generation
2. Check generated XML file
3. Verify XML contains items like:
```xml
<Item device_id="..." device_name="测试遥测点1" alarm_desc="告警上限：100.0" decide_rule="6" .../>
<Item device_id="..." device_name="测试遥测点1" alarm_desc="告警下限：10.0" decide_rule="7" .../>
<Item device_id="..." device_name="测试遥测点1" alarm_desc="告警上限：150.0" decide_rule="6" .../>
<Item device_id="..." device_name="测试遥测点1" alarm_desc="告警下限：5.0" decide_rule="7" .../>
```
4. Verify NO `alarm_level` attribute exists in these items

### Test Case 4: 遥测 without alarm rules
**Description:** 遥测 point without any high/low limits should be excluded from output

**Test Data:**
```sql
-- Insert test 遥测 point without alarm rules
INSERT INTO t_std_devicemete (device_mete_id, mete_kind, alarm_level, high_limit2, low_limit2, high_limit3, low_limit3, high_limit4, low_limit4, mete_name, ...)
VALUES (2002, 2, NULL, NULL, NULL, NULL, NULL, NULL, NULL, '测试遥测点2', ...);
```

**Expected Result:**
- Point should NOT be retrieved by SQL query
- Point should NOT appear in XML output

**Verification:**
1. Trigger alarm threshold model generation
2. Check generated XML file
3. Verify "测试遥测点2" does NOT appear in output

### Test Case 5: 遥测 with alarm_level but no alarm rules
**Description:** 遥测 point with alarm_level but without alarm rules should be excluded (alarm_level is not relevant for 遥测)

**Test Data:**
```sql
-- Insert test 遥测 point with alarm_level but no limits
INSERT INTO t_std_devicemete (device_mete_id, mete_kind, alarm_level, high_limit2, low_limit2, mete_name, ...)
VALUES (2003, 2, 131, NULL, NULL, '测试遥测点3', ...);
```

**Expected Result:**
- Point should NOT be retrieved by SQL query (no alarm rules)
- Point should NOT appear in XML output

**Verification:**
1. Trigger alarm threshold model generation
2. Check generated XML file
3. Verify "测试遥测点3" does NOT appear in output

## How to Trigger Model Generation

### Method 1: Via API/Controller
```bash
# Assuming there's an API endpoint
curl -X POST http://localhost:8080/api/sendToUpSystem/createModel?type=12
```

### Method 2: Via Service Method
```java
// In Java code or test
SendToUpSystemServices service = ...; // get service bean
service.creatModelList("12"); // type 12 is alarm threshold model
```

### Method 3: Check existing workflow
Look for scheduled jobs or existing test code that triggers model generation:
```bash
cd /home/runner/work/center/center
grep -r "creatModel\|createAlarmThreshold" --include="*.java"
```

## XML Output Validation

The generated XML file should have this structure:

```xml
<?xml version="1.0" encoding="UTF-8"?>
<Alarm_Threshold>
    <!-- 遥信 items - WITH alarm_level -->
    <Item device_id="..." device_name="..." alarm_level="1" alarm_desc="告警级别：一般;告警状态：..." 
          station_code="..." station_name="..." decide_rule="1" decision_value_class="1" 
          alarm_type="..." base_line_value="..." defect_type=""/>
    
    <!-- 遥测 items - WITHOUT alarm_level -->
    <Item device_id="..." device_name="..." alarm_desc="告警上限：100.0" 
          station_code="..." station_name="..." decide_rule="6" decision_value_class="1" 
          alarm_type="..." base_line_value="100.0" defect_type=""/>
</Alarm_Threshold>
```

**Key Validation Points:**
1. 遥信 items MUST have `alarm_level` attribute
2. 遥测 items MUST NOT have `alarm_level` attribute
3. 遥信 alarm_desc includes "告警级别："
4. 遥测 alarm_desc does NOT include "告警级别："
5. 遥测 can have multiple items per point (one for each limit)

## Database Schema Reference

**t_std_devicemete table** - Key fields:
- `mete_kind`: 1 = 遥信, 2 = 遥测, 3 = 遥控
- `alarm_level`: Alarm level (131=一般, 132=严重, 133=危急) - required for 遥信
- `high_limit2`, `low_limit2`: Alarm limits level 1 (一般)
- `high_limit3`, `low_limit3`: Alarm limits level 2 (严重)
- `high_limit4`, `low_limit4`: Alarm limits level 3 (危急)
- `alarm_state`: State that triggers alarm (0 or 1) - for 遥信
- `state_zero`, `state_one`: State descriptions - for 遥信

## Regression Testing

Ensure existing functionality still works:

1. **Other model types** (1-11) should generate correctly
2. **Existing 遥信 points with alarm_level** should still be included
3. **XML file format** should remain valid and parseable
4. **Upstream system integration** should work with new format

## Known Issues / Notes

1. The project uses Java 8 but Java 17 is installed in the environment, causing compilation issues with Maven. This is unrelated to our changes.
2. No existing unit test infrastructure was found for this module.
3. The `getAlarmMap()` method is kept for backward compatibility but should not be used for new alarm threshold generation.

## Summary of Behavioral Changes

| Scenario | Before | After |
|----------|--------|-------|
| 遥信 with alarm_level + rules | Included with alarm_level | ✓ Same (included with alarm_level) |
| 遥信 without alarm_level | Excluded | ✓ Same (excluded) |
| 遥测 with rules but no alarm_level | Excluded (Bug!) | ✓ Fixed (included without alarm_level) |
| 遥测 with rules and alarm_level | Included with alarm_level | ✓ Fixed (included without alarm_level) |
| 遥测 without rules | Excluded | ✓ Same (excluded) |

The key fix is that 遥测 points are now included when they have alarm rules, regardless of alarm_level presence, and the alarm_level field is not included in the XML output for 遥测 points.
