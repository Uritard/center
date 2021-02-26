package com.yjh.gateway.common.utils;

import org.mortbay.util.LazyList;
import org.mortbay.util.MultiMap;

import java.util.*;

public class MultisMap extends LinkedHashMap implements Cloneable {
    public MultisMap() {
    }

    public MultisMap(int size) {
        super(size);
    }

    public MultisMap(Map map) {
        super(map.size() * 3 / 2);
        this.putAll(map);
    }

    public List getValues(Object name) {
        return LazyList.getList(super.get(name), true);
    }

    public Object getValue(Object name, int i) {
        Object l = super.get(name);
        return i == 0 && LazyList.size(l) == 0 ? null : LazyList.get(l, i);
    }

    public String getString(Object name) {
        Object l = super.get(name);
        switch(LazyList.size(l)) {
            case 0:
                return null;
            case 1:
                Object o = LazyList.get(l, 0);
                return o == null ? null : o.toString();
            default:
                StringBuffer values = new StringBuffer(128);
                synchronized(values) {
                    for(int i = 0; i < LazyList.size(l); ++i) {
                        Object e = LazyList.get(l, i);
                        if (e != null) {
                            if (values.length() > 0) {
                                values.append(',');
                            }

                            values.append(e.toString());
                        }
                    }

                    return values.toString();
                }
        }
    }

    public Object get(Object name) {
        Object l = super.get(name);
        switch(LazyList.size(l)) {
            case 0:
                return null;
            case 1:
                Object o = LazyList.get(l, 0);
                return o;
            default:
                return LazyList.getList(l, true);
        }
    }

    public Object put(Object name, Object value) {
        return super.put(name, LazyList.add((Object)null, value));
    }

    public Object putValues(Object name, List values) {
        return super.put(name, values);
    }

    public Object putValues(Object name, String[] values) {
        Object list = null;

        for(int i = 0; i < values.length; ++i) {
            list = LazyList.add(list, values[i]);
        }

        return this.put(name, list);
    }

    public void add(Object name, Object value) {
        Object lo = super.get(name);
        Object ln = LazyList.add(lo, value);
        if (lo != ln) {
            super.put(name, ln);
        }

    }

    public void addValues(Object name, List values) {
        Object lo = super.get(name);
        Object ln = LazyList.addCollection(lo, values);
        if (lo != ln) {
            super.put(name, ln);
        }

    }

    public void addValues(Object name, String[] values) {
        Object lo = super.get(name);
        Object ln = LazyList.addCollection(lo, Arrays.asList(values));
        if (lo != ln) {
            super.put(name, ln);
        }

    }

    public boolean removeValue(Object name, Object value) {
        Object lo = super.get(name);
        Object ln = lo;
        int s = LazyList.size(lo);
        if (s > 0) {
            ln = LazyList.remove(lo, value);
            if (ln == null) {
                super.remove(name);
            } else {
                super.put(name, ln);
            }
        }

        return LazyList.size(ln) != s;
    }

    public void putAll(Map m) {
        Iterator i = m.entrySet().iterator();
        boolean multi = m instanceof MultiMap;

        while(i.hasNext()) {
            Map.Entry entry = (Map.Entry)i.next();
            if (multi) {
                super.put(entry.getKey(), LazyList.clone(entry.getValue()));
            } else {
                this.put(entry.getKey(), entry.getValue());
            }
        }

    }

    public Map toStringArrayMap() {
        HashMap map = new HashMap(this.size() * 3 / 2);
        Iterator i = super.entrySet().iterator();

        while(i.hasNext()) {
            Map.Entry entry = (Map.Entry)i.next();
            Object l = entry.getValue();
            String[] a = LazyList.toStringArray(l);
            map.put(entry.getKey(), a);
        }

        return map;
    }

    public Object clone() {
        MultiMap mm = (MultiMap)super.clone();
        Iterator iter = mm.entrySet().iterator();

        while(iter.hasNext()) {
            Map.Entry entry = (Map.Entry)iter.next();
            entry.setValue(LazyList.clone(entry.getValue()));
        }

        return mm;
    }
}
