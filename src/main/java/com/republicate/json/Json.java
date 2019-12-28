package com.republicate.json;

/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import sun.reflect.generics.reflectiveObjects.NotImplementedException;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.Reader;
import java.io.Serializable;
import java.io.StringWriter;
import java.io.Writer;
import java.math.BigDecimal;
import java.math.BigInteger;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.Map;

/**
 * <p>JSON container interface.</p>
 * <p>The two inner classes <code>JsonArray</code> and <code>JsonObject</code> represent the two flavors of this container.</p>
 */

public interface Json extends Serializable
{
    /**
     * Logger
     */
    Logger logger = LoggerFactory.getLogger("json");

    /**
     * Indentation
     */
    String INDENTATION = "  ";

    /**
     * Parse a JSON string into a JSON container
     * @param content JSON content
     * @return parsed json
     */
    static Json parse(String content) throws IOException
    {
        return new Parser(content).parse();
    }

    /**
     * Parse a JSON stream into a JSON container
     * @param reader
     * @return parsed json
     */
    static Json parse(Reader reader) throws IOException
    {
        return new Parser(reader).parse();
    }

    /**
     * Creates a new JSON array
     * @return empty array
     */
    static JsonArray newJsonArray()
    {
        return new JsonArray();
    }

    /**
     * Creates a new JSON object
     * @return empty array
     */
    static JsonObject newJsonObject()
    {
        return new JsonObject();
    }

    /**
     * Parse a JSON stream into a JSON container or simple value
     * @param content JSON content
     * @return parsed json
     */
    static Serializable parseValue(String content) throws IOException
    {
        return new Parser(content).parseValue();
    }

    /**
     * Parse a JSON stream into a JSON container
     * @param reader
     * @return parsed json
     */
    static Serializable parseValue(Reader reader) throws IOException
    {
        return new Parser(reader).parseValue(true);
    }

    /**
     * Check if the underlying container is a JSON array.
     * @return true if underlying container is an array, false otherwise
     */
    boolean isJsonArray();

    /**
     * Check if the underlying container is an object.
     * @return true if underlying container is an object, false otherwise
     */
    boolean isJsonObject();

    /**
     * Ensure that the underlying container is an array.
     * @throws IllegalStateException otherwise
     */
    void ensureIsJsonArray();

    /**
     * Ensure that the underlying container is an object.
     * @throws IllegalStateException otherwise
     */
    void ensureIsJsonObject();

    /**
     * Get self as a JsonArray
     * @throws IllegalStateException if container is not a JsonArray
     */
    default JsonArray asJsonArray()
    {
        ensureIsJsonArray();
        return (JsonArray) this;
    }

    /**
     * Get self as a JsonArray
     * @throws IllegalStateException if container is not a JsonObject
     */
    default JsonObject asJsonObject()
    {
        ensureIsJsonObject();
        return (JsonObject) this;
    }

    /**
     * Returns the number of elements in this collection.
     * @return the number of elements in this collection
     */
    int size();

    /**
     * Returns <tt>true</tt> if this collection contains no elements.
     * @return <tt>true</tt> if this collection contains no elements
     */
    boolean isEmpty();

    /**
     * Writes a representation of this container to the specified writer.
     * @param writer target writer
     * @return input writer
     */
    Writer toString(Writer writer) throws IOException;

    /**
     * Writes a pretty representation of this container to the specified writer.
     * @param writer target writer
     * @return input writer
     */
    Writer toPrettyString(Writer writer, String indent) throws IOException;

    /**
     * Gets a pretty representation of this container.
     * @return input writer
     */
    default String toPrettyString()
    {
        try
        {
            return toPrettyString(new StringWriter(), "").toString();
        }
        catch (IOException ioe)
        {
            logger.error("could not render Json container string", ioe);
            return null;
        }
    }

    /**
     * Implements a JSON array
     */
    class JsonArray extends ArrayList<Serializable> implements Json
    {
        private static final long serialVersionUID = 1272604422260086506L;

        /**
         * Check if the underlying container is an array.
         *
         * @return true if underlying container is an array, false otherwise
         */
        @Override
        public boolean isJsonArray()
        {
            return true;
        }

        /**
         * Check if the underlying container is an object.
         *
         * @return true if underlying container is an object, false otherwise
         */
        @Override
        public boolean isJsonObject()
        {
            return false;
        }

        /**
         * Check that the underlying container is an array.
         * @throws IllegalStateException otherwise
         */
        @Override
        public void ensureIsJsonArray()
        {
        }

        /**
         * Check that the underlying container is an object.
         * @throws IllegalStateException otherwise
         */
        @Override
        public void ensureIsJsonObject()
        {
            throw new IllegalStateException("container must be a JSON object");
        }

        /**
         * Writes a representation of this container to the specified writer.
         * @param writer target writer
         */
        public Writer toString(Writer writer) throws IOException
        {
            writer.write('[');
            boolean first = true;
            for (Serializable value : this)
            {
                if (first)
                {
                    first = false;
                }
                else
                {
                    writer.write(',');
                }
                if (value instanceof Json)
                {
                    ((Json)value).toString(writer);
                }
                else
                {
                    Serializer.writeSerializable(value, writer);
                }
            }
            writer.write(']');
            return writer;
        }

        /**
         * Writes a pretty representation of this container to the specified writer.
         * @param writer target writer
         * @return input writer
         */
        @Override
        public Writer toPrettyString(Writer writer, String indent) throws IOException
        {
            writer.write(indent);
            String nextIndent = indent + INDENTATION;
            writer.write("[\n");
            boolean first = true;
            for (Serializable value : this)
            {
                if (first)
                {
                    first = false;
                }
                else
                {
                    writer.write(",\n");
                }
                if (value instanceof Json)
                {
                    ((Json)value).toPrettyString(writer, nextIndent);
                }
                else
                {
                    writer.write(nextIndent);
                    Serializer.writeSerializable(value, writer);
                }
            }
            writer.write('\n');
            writer.write(indent);
            writer.write(']');
            return writer;
        }

        /**
         * Returns a string representation of this container
         * @return container string representation
         */
        @Override
        public String toString()
        {
            try
            {
                return toString(new StringWriter()).toString();
            }
            catch (IOException ioe)
            {
                logger.error("could not render JsonArray string", ioe);
                return null;
            }
        }
    }

    /**
     * Implements a JSON object
     */
    class JsonObject extends LinkedHashMap<String, Serializable> implements Json
    {
        private static final long serialVersionUID = -8433114857911795160L;

        /**
         * Check if the underlying container is an array.
         *
         * @return true if underlying container is an array, false otherwise
         */
        @Override
        public boolean isJsonArray()
        {
            return false;
        }

        /**
         * Check if the underlying container is an object.
         *
         * @return true if underlying container is an object, false otherwise
         */
        @Override
        public boolean isJsonObject()
        {
            return true;
        }

        /**
         * Check that the underlying container is an array.
         * @throws IllegalStateException otherwise
         */
        @Override
        public void ensureIsJsonArray()
        {
            throw new IllegalStateException("container must be a JSON array");
        }

        /**
         * Check that the underlying container is an object.
         * @throws IllegalStateException otherwise
         */
        @Override
        public void ensureIsJsonObject()
        {
        }

        /**
         * Writes a representation of this container to the specified writer.
         * @param writer target writer
         */
        public Writer toString(Writer writer) throws IOException
        {
            writer.write('{');
            boolean first = true;
            for (Map.Entry<String, Serializable> entry : entrySet())
            {
                if (first)
                {
                    first = false;
                }
                else
                {
                    writer.write(',');
                }
                writer.write('"');
                writer.write(entry.getKey());
                writer.write("\":");
                Serializable value = entry.getValue();
                if (value instanceof Json)
                {
                    ((Json)value).toString(writer);
                }
                else
                {
                    Serializer.writeSerializable(value, writer);
                }
            }
            writer.write('}');
            return writer;
        }

        /**
         * Writes a pretty representation of this container to the specified writer.
         * @param writer target writer
         * @return input writer
         */
        @Override
        public Writer toPrettyString(Writer writer, String indent) throws IOException
        {
            writer.write("{\n");
            String nextIndent = indent + INDENTATION;
            boolean first = true;
            for (Map.Entry<String, Serializable> entry : entrySet())
            {
                if (first)
                {
                    first = false;
                }
                else
                {
                    writer.write(",\n");
                }
                writer.write(nextIndent);
                writer.write('"');
                writer.write(entry.getKey());
                writer.write("\" : ");
                Serializable value = entry.getValue();
                if (value instanceof Json)
                {
                    ((Json)value).toPrettyString(writer, nextIndent);
                }
                else
                {
                    writer.write(nextIndent);
                    Serializer.writeSerializable(value, writer);
                }
            }
            writer.write('\n');
            writer.write(indent);
            writer.write('}');
            return writer;
        }

        /**
         * Returns a string representation of this container
         * @return container string representation
         */
        @Override
        public String toString()
        {
            try
            {
                return toString(new StringWriter()).toString();
            }
            catch (IOException ioe)
            {
                logger.error("could not render JsonArray string", ioe);
                return null;
            }
        }
    }

    class Serializer
    {
        private static final String[] ESCAPED_CHARS;
        static
        {
            ESCAPED_CHARS = new String[128];
            for (int i = 0; i <= 0x1f; i++)
            {
                ESCAPED_CHARS[i] = String.format("\\u%04x", (int) i);
            }
            ESCAPED_CHARS['"'] = "\\\"";
            ESCAPED_CHARS['\\'] = "\\\\";
            ESCAPED_CHARS['\t'] = "\\t";
            ESCAPED_CHARS['\b'] = "\\b";
            ESCAPED_CHARS['\n'] = "\\n";
            ESCAPED_CHARS['\r'] = "\\r";
            ESCAPED_CHARS['\f'] = "\\f";
        }

        /**
         * Escape a string for Json special characters towards
         * the provided writer
         * @param str input string
         * @param writer target writer
         * @return input writer
         */
        static public Writer escapeJson(String str, Writer writer) throws IOException
        {
            // use com.google.gson.stream.JsonWriter method to minimize write() calls
            // in case the output writer is not buffered
            int last = 0;
            int len = str.length();
            for (int i = 0; i < len; ++i)
            {
                char c = str.charAt(i);
                String escaped;
                if (c < 128)
                {
                    escaped = ESCAPED_CHARS[c];
                    if (escaped == null)
                    {
                        continue;
                    }
                }
                else if (c == '\u2028')
                {
                    escaped = "\\u2028";
                }
                else if (c == '\u2029')
                {
                    escaped = "\\u2029";
                }
                else
                {
                    continue;
                }
                if (last < i)
                {
                    writer.write(str, last, i - last);
                }
                writer.write(escaped);
                last = i + 1;
            }
            if (last < len)
            {
                writer.write(str, last, len - last);
            }
            return writer;
        }

        /**
         * Write a serializable element to an output writer
         * @param serializable input element
         * @param writer output writer
         */
        static protected void writeSerializable(Serializable serializable, Writer writer) throws IOException
        {
            if (serializable == null)
            {
                writer.write("null");
            }
            else if (serializable instanceof Boolean)
            {
                writer.write(serializable.toString());
            }
            else if(serializable instanceof Number)
            {
                String number = ((Number)serializable).toString();
                if (number.equals("-Infinity") || number.equals("Infinity") || number.equals("NaN"))
                {
                    throw new IOException("invalid number: " + number);
                }
                writer.write(serializable.toString());
            }
            else
            {
                writer.write('\"');
                escapeJson(serializable.toString(), writer);
                writer.write('\"');
            }
        }
    }

    class Parser
    {
        private Reader reader;
        private int row = 1;
        private int col = 0;
        private int ch = 0;
        private boolean prefetch = false;
        private int prefetched = 0;
        private char buffer[] = new char[1024];
        private int pos = 0;

        private Parser(String content)
        {
            this(new FastStringReader(content));
        }

        private Parser(Reader reader)
        {
            if (1==1/*reader.markSupported()*/)
            {
                this.reader = reader;
            }
            else
            {
                this.reader = new BufferedReader(reader);
            }
        }

        private int next() throws IOException
        {
            if (prefetch)
            {
                ch = prefetched;
                prefetch = false;
            }
            else
            {
                ch = reader.read();
                if (ch == '\n')
                {
                    ++row;
                    col = 0;
                }
                else
                {
                    ++col;
                }
            }
            return ch;
        }

        private void back() throws IOException
        {
            if (prefetch)
            {
                throw error("internal error: cannot go back twice");
            }
            prefetch = true;
            prefetched = ch;
        }

        private Json parse() throws IOException
        {
            Json ret = null;
            skipWhiteSpace();
            switch (ch)
            {
                case -1:
                    break;
                case '{':
                    ret = parseObject();
                    break;
                case '[':
                    ret = parseArray();
                    break;
                default:
                    throw error("expecting '[' or '{', got: '" + display(ch) + "'");
            }
            if (ret != null)
            {
                skipWhiteSpace();
                if (ch != -1)
                {
                    throw error("expecting end of stream");
                }
            }
            return ret;
        }

        private void skipWhiteSpace() throws IOException
        {
            for(; Character.isWhitespace(next()); );
        }

        private IOException error(String msg)
        {
            msg = "JSON parsing error at line " + row + ", column "  + col + ": " + msg;
            logger.error(msg);
            return new IOException(msg);
        }

        private String display(int c)
        {
            if (c == -1)
            {
                return "end of stream";
            }
            else if (Character.isISOControl(c))
            {
                return "0x" + Integer.toHexString(c);
            }
            else
            {
                return String.valueOf((char)c);
            }
        }

        private JsonArray parseArray() throws IOException
        {
            JsonArray ret = new JsonArray();
            skipWhiteSpace();
            if (ch != ']')
            {
                back();
                main:
                while (true)
                {
                    ret.add(parseValue());
                    skipWhiteSpace();
                    switch (ch)
                    {
                        case ']': break main;
                        case ',': break;
                        default: throw error("expecting ',' or ']', got: '" + display(ch) + "'");
                    }
                }
            }
            return ret;
        }

        private JsonObject parseObject() throws IOException
        {
            JsonObject ret = new JsonObject();
            skipWhiteSpace();
            if (ch != '}')
            {
                main:
                while (true)
                {
                    if (ch != '"')
                    {
                        throw error("expecting key string, got: '" + display(ch) + "'");
                    }
                    String key = parseString();
                    skipWhiteSpace();
                    if (ch != ':')
                    {
                        throw error("expecting ':', got: '" + display(ch) + "'");
                    }
                    Serializable value = parseValue();
                    Serializable previous = ret.put(key, value);
                    if (previous != null)
                    {
                        logger.warn("key '{}' is not unique at line {}, column {}", key, row, col);
                    }
                    skipWhiteSpace();
                    switch (ch)
                    {

                        case '}': break main;
                        case ',': break;
                        default: throw error("expecting ',' or '}', got: '" + display(ch) + "'");
                    }
                    skipWhiteSpace();
                }
            }
            return ret;
        }

        private Serializable parseValue() throws IOException
        {
            return parseValue(false);
        }

        private Serializable parseValue(boolean complete) throws IOException
        {
            Serializable ret = null;
            skipWhiteSpace();
            if (ch == -1)
            {
                throw error("unexpecting end of stream");
            }
            switch (ch)
            {
                case '"': ret = parseString(); break;
                case '[': ret = parseArray(); break;
                case '{': ret = parseObject(); break;
                case 't': ret = parseKeyword("true", true); break;
                case 'f': ret = parseKeyword("false", false); break;
                case 'n': ret = parseKeyword("null", null); break;
                case '-': case '0': case '1': case '2': case '3': case '4':
                case '5': case '6': case '7': case '8': case '9':
                    ret = parseNumber(); break;
                case -1: break;
                default: throw error("unexpected chararcter: '" + display(ch) + "'");
            }
            if (complete)
            {
                skipWhiteSpace();
                if (ch != -1)
                {
                    throw error("expecting end of stream");
                }
            }
            return ret;
        }

        private Serializable parseKeyword(String keyword, Serializable value) throws IOException
        {
            for (int i = 0; i < keyword.length(); ++i)
            {
                if (i > 0)
                {
                    next();
                }
                if (ch != keyword.charAt(i))
                {
                    if (ch == -1)
                    {
                        throw new IOException("encountered end of stream while parsing keyword '" + keyword + "'");
                    }
                    else
                    {
                        throw new IOException("invalid character '" + display(ch) + "' while parsing keyword '" + keyword + "'");
                    }
                }
            }
            return value;
        }

        private String parseString() throws IOException
        {
            // borrow some optimization ideas from com.google.gson.stream.JsonReader
            pos = 0;
            StringBuilder builder = null;
            while (true)
            {
                while (pos < buffer.length)
                {
                    buffer[pos++] = (char)next();
                    if (ch == '"')
                    {
                        if (builder == null)
                        {
                            return new String(buffer, 0, pos - 1);
                        }
                        else
                        {
                            builder.append(buffer, 0, pos - 1);
                            return builder.toString();
                        }
                    }
                    else if (ch == '\\')
                    {
                        if (builder == null)
                        {
                            builder = new StringBuilder(Math.max(2 * pos, 16));
                        }
                        builder.append(buffer, 0, pos - 1);
                        pos = 0;
                        char c = parseEscapeSequence();
                        builder.append(c);
                        if (Character.isHighSurrogate(c))
                        {
                            ch = next();
                            if (ch != '\\')
                            {
                                throw error("low surrogate escape sequence expected");
                            }
                            c = parseEscapeSequence();
                            builder.append(c);
                            if (!Character.isLowSurrogate(c))
                            {
                                throw error("low surrogate escape sequence expected");
                            }
                        }
                        else if (Character.isLowSurrogate(c))
                        {
                            throw error("lone low surrogate escape sequence unexpected");
                        }
                    }
                    else if (ch < 0x20)
                    {
                        throw error("unescaped control character");
                    }
                }
                if (builder == null)
                {
                    builder = new StringBuilder(Math.max(2 * pos, 16));
                }
                builder.append(buffer, 0, pos);
                pos = 0;
                if (next() == -1)
                {
                    throw error("unterminated string");
                }
            }
        }

        private char parseEscapeSequence() throws IOException
        {
            switch (next())
            {
                case -1:
                    throw error("unterminated escape sequence");
                case 'u':
                    char result = 0;
                    for (int i = 0; i < 4; ++i)
                    {
                        if (next() == -1)
                        {
                            throw error("unterminated escape sequence");
                        }
                        char c = (char)ch;
                        result <<= 4;
                        if (c >= '0' && c <= '9')
                        {
                            result += c - '0';
                        }
                        else if (c >= 'a' && c <= 'f')
                        {
                            result += c - 'a' + 10;
                        }
                        else if (c >= 'A' && c <= 'F')
                        {
                            result += c - 'A' + 10;
                        }
                        else
                        {
                            throw error("malformed escape sequence");
                        }
                    }
                    return result;
                case 't':
                    return '\t';
                case 'b':
                    return '\b';
                case 'n':
                    return '\n';
                case 'f':
                    return '\f';
                case 'r':
                    return '\r';
                case '"':
                    return '"';
                case '\\':
                    return '\\';
                case '/':
                    return '/';
                default:
                    throw error("unknown escape sequence");
            }
        }

        private static long MIN_LONG_DECILE = Long.MIN_VALUE / 10;

        private Number parseNumber() throws IOException
        {
            // inspired from com.google.gson.stream.JsonReader, but much more readable
            // and handle Double/BigDecimal alternatives
            Number number;
            pos = 0;
            int digits = 0;
            boolean negative = false;
            boolean decimal = false;
            boolean fitsInLong = true;
            boolean fitsInDouble = true;
            long negValue = 0;
            // sign
            if (ch == '-')
            {
                negative = true;
                buffer[pos++] = (char)ch;
                if (next() == -1)
                {
                    throw error("malformed number");
                }
            }
            // mantissa
            digits += readDigits(false);
            // fractional part
            if (ch == '.')
            {
                decimal = true;
                buffer[pos++] = (char)ch;
                if (next() == -1)
                {
                    throw error("malformed number");
                }
                digits += readDigits(true);
            }
            else if (ch != 'e' && ch != 'E')
            {
                // check if number fits in long
                int i = negative ? 1 : 0;
                negValue = -(buffer[i++] - '0');
                for (; i < pos; ++i)
                {
                    long newNegValue = negValue * 10 - (buffer[i] - '0');
                    fitsInLong &= negValue > MIN_LONG_DECILE
                        || (negValue == MIN_LONG_DECILE && newNegValue < negValue);
                    if (!fitsInLong)
                    {
                        break;
                    }
                    negValue = newNegValue;
                }
            }
            if (digits > 15)
            {
                fitsInDouble = false;
            }
            // exponent
            if (ch == 'e' || ch == 'E')
            {
                decimal = true;
                buffer[pos++] = (char)ch;
                if (next() == -1)
                {
                    throw error("malformed number");
                }
                if (pos == buffer.length)
                {
                    throw error("number is too long at my taste");
                }
                if (ch == '+' || ch == '-')
                {
                    buffer[pos++] = (char)ch;
                    if (next() == -1)
                    {
                        throw error("malformed number");
                    }
                }
                int expPos = pos;
                int expDigits = readDigits(true); // or false ?
                if (fitsInDouble && expDigits >= 3
                    && (expDigits > 3
                        || buffer[expPos] > '3'
                        || buffer[expPos + 1] > '0'
                        || buffer[expPos + 2] > '7'))
                {
                    fitsInDouble = false;
                }
            }
            if (!decimal && fitsInLong && (negative || negValue != Long.MIN_VALUE) && (!negative || negValue != 0))
            {
                number = Long.valueOf(-negValue);
            }
            else
            {
                String strBuff = new String(buffer, 0, pos);
                if (!decimal)
                {
                    number = new BigInteger(strBuff);
                }
                else if (fitsInDouble)
                {
                    number = Double.valueOf(strBuff);
                }
                else
                {
                    number = new BigDecimal(strBuff);
                }
            }
            // we always end up reading one more character
            back();
            return number;
        }

        private int readDigits(boolean zeroFirstAllowed) throws IOException
        {
            int len = 0;
            while (pos < buffer.length)
            {
                if (!Character.isDigit(ch))
                {
                    break;
                }
                buffer[pos++] = (char)ch;
                ++len;
                next();
            }
            if (pos == buffer.length)
            {
                throw error("number is too long at my taste");
            }
            if (len == 0 || !zeroFirstAllowed && len > 1 && buffer[pos - len] == '0')
            {
                throw error("malformed number");
            }
            return len;
        }
    }

    class FastStringReader extends  Reader
    {
        String str;
        int len;
        int pos = 0;

        protected FastStringReader(String str)
        {
            this.str = str;
            len = str.length();
        }

        @Override
        public int read(char[] cbuf, int off, int len) throws IOException
        {
            throw new NotImplementedException();
        }


        @Override
        public void close() throws IOException
        {
        }

        @Override
        public int read()
        {
            return pos == len ? -1 : str.charAt(pos++);
        }
    }
}
