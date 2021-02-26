package com.yjh.gateway.common.utils;

import org.mortbay.util.StringUtil;
import org.mortbay.util.TypeUtil;
import org.mortbay.util.Utf8StringBuffer;

import java.io.UnsupportedEncodingException;

public class Decode extends  MultisMap{
    public static final String ENCODING = System.getProperty("org.mortbay.util.UrlEncoding.charset", "UTF-8");
    public static String decodeString(String encoded, int offset, int length, String charset) {
        int i;
        char c;
        if (charset != null && !StringUtil.isUTF8(charset)) {
            StringBuffer buffer = null;

            try {
                for(i = 0; i < length; ++i) {
                    c = encoded.charAt(offset + i);
                    if (c >= 0 && c <= 255) {
                        if (c == '+') {
                            if (buffer == null) {
                                buffer = new StringBuffer(length);
                                buffer.append(encoded.substring(offset, offset + i));
                            }

                            buffer.append(' ');
                        } else if (c == '%' && i + 2 < length) {
                            if (buffer == null) {
                                buffer = new StringBuffer(length);
                                buffer.append(encoded.substring(offset, offset + i));
                            }

                            byte[] ba = new byte[length];

                            int n;
                            for(n = 0; c >= 0 && c <= 255; c = encoded.charAt(offset + i)) {
                                if (c == '%') {
                                    if (i + 2 < length) {
                                        try {
                                            ba[n++] = (byte) TypeUtil.parseInt(encoded, offset + i + 1, 2, 16);
                                            i += 3;
                                        } catch (NumberFormatException var11) {
                                            ba[n - 1] = 37;

                                            while(true) {
                                                ++i;
                                                char next;
                                                if ((next = encoded.charAt(i + offset)) == '%') {
                                                    break;
                                                }

                                                ba[n++] = (byte)(next == '+' ? 32 : next);
                                            }
                                        }
                                    } else {
                                        ba[n++] = 37;
                                        ++i;
                                    }
                                } else if (c == '+') {
                                    ba[n++] = 32;
                                    ++i;
                                } else {
                                    ba[n++] = (byte)c;
                                    ++i;
                                }

                                if (i >= length) {
                                    break;
                                }
                            }

                            --i;
                            buffer.append(new String(ba, 0, n, charset));
                        } else if (buffer != null) {
                            buffer.append(c);
                        }
                    } else if (buffer == null) {
                        buffer = new StringBuffer(length);
                        buffer.append(encoded.substring(offset, offset + i + 1));
                    } else {
                        buffer.append(c);
                    }
                }

                if (buffer == null) {
                    if (offset == 0 && encoded.length() == length) {
                        return encoded;
                    } else {
                        return encoded.substring(offset, offset + length);
                    }
                } else {
                    return buffer.toString();
                }
            } catch (UnsupportedEncodingException var12) {
                throw new RuntimeException(var12);
            }
        } else {
            Utf8StringBuffer buffer = null;

            for(i = 0; i < length; ++i) {
                c = encoded.charAt(offset + i);
                if (c >= 0 && c <= 255) {
                    if (c == '+') {
                        if (buffer == null) {
                            buffer = new Utf8StringBuffer(length);
                            buffer.getStringBuffer().append(encoded.substring(offset, offset + i));
                        }

                        buffer.getStringBuffer().append(' ');
                    } else if (c == '%' && i + 2 < length) {
                        if (buffer == null) {
                            buffer = new Utf8StringBuffer(length);
                            buffer.getStringBuffer().append(encoded.substring(offset, offset + i));
                        }

                        while(c == '%' && i + 2 < length) {
                            try {
                                byte b = (byte) TypeUtil.parseInt(encoded, offset + i + 1, 2, 16);
                                buffer.append(b);
                                i += 3;
                            } catch (NumberFormatException var13) {
                                buffer.getStringBuffer().append('%');

                                while(true) {
                                    ++i;
                                    char next;
                                    if ((next = encoded.charAt(i + offset)) == '%') {
                                        break;
                                    }

                                    buffer.getStringBuffer().append(next == '+' ? ' ' : next);
                                }
                            }

                            if (i < length) {
                                c = encoded.charAt(offset + i);
                            }
                        }

                        --i;
                    } else if (buffer != null) {
                        buffer.getStringBuffer().append(c);
                    }
                } else if (buffer == null) {
                    buffer = new Utf8StringBuffer(length);
                    buffer.getStringBuffer().append(encoded.substring(offset, offset + i + 1));
                } else {
                    buffer.getStringBuffer().append(c);
                }
            }

            if (buffer == null) {
                if (offset == 0 && encoded.length() == length) {
                    return encoded;
                } else {
                    return encoded.substring(offset, offset + length);
                }
            } else {
                return buffer.toString();
            }
        }
    }


    public static void decodeTo(String content, MultisMap map, String charset) {
        if (charset == null) {
            charset = ENCODING;
        }

        synchronized(map) {
            String key = null;
            String value = null;
            int mark = -1;
            boolean encoded = false;

            int i;
            for(i = 0; i < content.length(); ++i) {
                char c = content.charAt(i);
                switch(c) {
                    case '%':
                        encoded = true;
                        break;
                    case '&':
                        int l = i - mark - 1;
                        value = l == 0 ? "" : (encoded ? decodeString(content, mark + 1, l, charset) : content.substring(mark + 1, i));
                        mark = i;
                        encoded = false;
                        if (key != null) {
                            map.add(key, value);
                        } else if (value != null && value.length() > 0) {
                            map.add(value, "");
                        }

                        key = null;
                        value = null;
                        break;
                    case '+':
                        encoded = true;
                        break;
                    case '=':
                        if (key == null) {
                            key = encoded ? decodeString(content, mark + 1, i - mark - 1, charset) : content.substring(mark + 1, i);
                            mark = i;
                            encoded = false;
                        }
                }
            }

            if (key != null) {
                i = content.length() - mark - 1;
                value = i == 0 ? "" : (encoded ? decodeString(content, mark + 1, i, charset) : content.substring(mark + 1));
                map.add(key, value);
            } else if (mark < content.length()) {
                key = encoded ? decodeString(content, mark + 1, content.length() - mark - 1, charset) : content.substring(mark + 1);
                map.add(key, "");
            }

        }
    }
}
