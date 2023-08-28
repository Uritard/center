/*
 * Copyright (c) 2023 Yijiahe Technology Co., Ltd. All rights reserved.
 */

package com.yjh.platform.module.patrol.entity;

import org.apache.commons.collections4.keyvalue.AbstractKeyValue;

/**
 * 自定义 Key-value 实现类，作为 Map的key时，只对key进行比较，不对value进行比较，可以实现当key相同，但value不同时认为两个结果相同
 *
 * @author Chenfei
 * @date 2023/8/28
 * @since [产品/模块版本] （可选）
 */
public class LineKeyValue<K, V> extends AbstractKeyValue<K, V> {
    /**
     * Constructs a new pair with the specified key and given value.
     *
     * @param key   the key for the entry, may be null
     * @param value the value for the entry, may be null
     */
    public LineKeyValue(K key, V value) {
        super(key, value);
    }

    //-----------------------------------------------------------------------

    /**
     * Compares this <code>Map.Entry</code> with another <code>Map.Entry</code>.
     * <p>
     * Returns true if the compared object is also a <code>LineKeyValue</code>,
     * and its key and value are equal to this object's key and value.
     *
     * @param obj the object to compare to
     * @return true if equal key and value
     */
    @Override
    public boolean equals(final Object obj) {
        if (obj == this) {
            return true;
        }
        if (obj instanceof LineKeyValue == false) {
            return false;
        }

        final LineKeyValue<?, ?> other = (LineKeyValue<?, ?>)obj;
        return (getKey() == null ? other.getKey() == null : getKey().equals(other.getKey()));
    }

    /**
     * Gets a hashCode compatible with the equals method.
     * <p>
     * Implemented per API documentation of {@link java.util.Map.Entry#hashCode()},
     * however subclasses may override this.
     *
     * @return a suitable hash code
     */
    @Override
    public int hashCode() {
        return (getKey() == null ? 0 : getKey().hashCode());
    }
}
