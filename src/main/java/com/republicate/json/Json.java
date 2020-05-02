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
import java.nio.charset.StandardCharsets;
import java.util.*;
import java.util.function.Consumer;

/**
 * <p>JSON container interface.</p>
 * <p>The two inner classes <code>Array</code> and <code>Object</code> represent the two flavors of this container.</p>
 */

public interface Json extends Serializable, Cloneable
{

/*****************************************************************
 *
 * Json static members and methods
 *
 *****************************************************************/

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
     * @throws IOException if parsing fails
     */
    static Json parse(String content) throws IOException
    {
        return new Parser(content).parse();
    }

    /**
     * Parse a JSON stream into a JSON container
     * @param reader JSON content reader
     * @return parsed json
     * @throws IOException if parsing fails
     */
    static Json parse(Reader reader) throws IOException
    {
        return new Parser(reader).parse();
    }

    /** creates a new Json.Object
     *
     * @return new Json.Object
     */
    static Json.Object newObject(Serializable... elements)
    {
        return new Json.Object(elements);
    }

    /** creates a new Json.Array
     *
     * @return new Json.Object
     */
    static Json.Array newArray(Serializable... elements)
    {
        return new Json.Array(elements);
    }

    /**
     * Parse a JSON stream into a JSON container or simple value
     * @param content JSON content
     * @return parsed json
     * @throws IOException if parsing fails
     */
    static Serializable parseValue(String content) throws IOException
    {
        return new Parser(content).parseValue(true);
    }

    /**
     * Parse a JSON stream into a JSON container or a simple value
     * @param reader JSON content reader
     * @return parsed json
     * @throws IOException if parsing fails
     */
    static Serializable parseValue(Reader reader) throws IOException
    {
        return new Parser(reader).parseValue(true);
    }

    /**
     * Commodity method to escape a JSON string
     * @param str string to escape
     * @return escaped string
     */
    static String escape(String str) throws IOException
    {
        return Serializer.escapeJson(str, new StringWriter()).toString();

    }

/*****************************************************************
 *
 * Json container
 *
 *****************************************************************/

    /**
     * Check if the underlying container is a JSON array.
     * @return true if underlying container is an array, false otherwise
     */
    boolean isArray();

    /**
     * Check if the underlying container is an object.
     * @return true if underlying container is an object, false otherwise
     */
    boolean isObject();

    /**
     * Ensure that the underlying container is an array.
     * @throws IllegalStateException otherwise
     */
    void ensureIsArray();

    /**
     * Ensure that the underlying container is an object.
     * @throws IllegalStateException otherwise
     */
    void ensureIsObject();

    /**
     * Get self as a Array
     * @return self as a Jon.Array
     * @throws IllegalStateException if container is not a Array
     */
    default Json.Array asArray()
    {
        ensureIsArray();
        return (Array) this;
    }

    /**
     * Get self as a Array
     * @return self as a Json.Object
     * @throws IllegalStateException if container is not a Object
     */
    default Json.Object asObject()
    {
        ensureIsObject();
        return (Object) this;
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
     * @throws IOException if serialization failes
     */
    Writer toString(Writer writer) throws IOException;

    /**
     * Writes a pretty representation of this container to the specified writer.
     * @param writer target writer
     * @param indent current indentation
     * @return input writer
     * @throws IOException if serialization failes
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
     * deep-clone object
     * @return deep-cloned object
     */
    java.lang.Object clone();

    /**
     * Tries to convert standard Java containers/objects to a Json container
     * @param obj object to convert
     * @return converted object
     * @throws ClassCastException if input is not convertible to json
     */
    static Json toJson(java.lang.Object obj)
    {
        return (Json) toSerializable(obj);
    }

    /**
     * Tries to convert standard Java containers/objects to a Json value
     * @param obj object to convert
     * @return converted object
     * @throws ClassCastException if input is not convertible to json
     */
    static Serializable toSerializable(java.lang.Object obj)
    {
        if (obj instanceof Map)
        {
            Json.Object ret = new Json.Object();
            Map map = (Map)obj;
            for (Map.Entry entry : (Set<Map.Entry>)map.entrySet())
            {
                ret.put((String)entry.getKey(), toSerializable(entry.getValue()));
            }
            return ret;
        }
        else if (obj instanceof Collection)
        {
            Json.Array ret = new Json.Array();
            for (java.lang.Object elem : (Collection)obj)
            {
                ret.add(toSerializable(elem));
            }
            return ret;
        }
        else return (Serializable)obj;
    }


/*****************************************************************
 *
 * Json.Array
 *
 *****************************************************************/

    /**
     * Implements a JSON array
     */
    class Array extends ArrayList<Serializable> implements Json
    {
        private static final long serialVersionUID = 1272604422260086506L;

        /**
         * Builds an empty Json.Array.
         */
        public Array()
        {
        }

        /**
         * Builds a Json.Array with specified items
         */
        public Array(Serializable... items)
        {
            this(Arrays.asList(items));
        }


        /**
         * Builds a Json.Array with the content of an existing collection.
         */
        public Array(Collection<? extends Serializable> collection)
        {
            super(collection);
        }

        /**
         * Check if the underlying container is an array.
         *
         * @return true if underlying container is an array, false otherwise
         */
        @Override
        public boolean isArray()
        {
            return true;
        }

        /**
         * Check if the underlying container is an object.
         *
         * @return true if underlying container is an object, false otherwise
         */
        @Override
        public boolean isObject()
        {
            return false;
        }

        /**
         * Check that the underlying container is an array.
         * @throws IllegalStateException otherwise
         */
        @Override
        public void ensureIsArray()
        {
        }

        /**
         * Check that the underlying container is an object.
         * @throws IllegalStateException otherwise
         */
        @Override
        public void ensureIsObject()
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
            if (!first) writer.write('\n');
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
                logger.error("could not render Array string", ioe);
                return null;
            }
        }

        /**
         * Returns the element at the specified position as a String value. 
         * @param  index index of the element to return
         * @return the element at the specified position as a String value
         */
        public String getString(int index)
        {
            return TypeUtils.toString(get(index));
        }

        /**
         * Returns the element at the specified position as a Boolean value. 
         * @param  index index of the element to return
         * @return the element at the specified position as a Boolean value
         */
        public Boolean getBoolean(int index)
        {
            return TypeUtils.toBoolean(get(index));
        }

        /**
         * Returns the element at the specified position as a Character value. 
         * @param  index index of the element to return
         * @return the element at the specified position as a Character value
         */
        public Character getChar(int index)
        {
            return TypeUtils.toChar(get(index));
        }

        /**
         * Returns the element at the specified position as a Byte value. 
         * @param  index index of the element to return
         * @return the element at the specified position as a Byte value
         */
        public Byte getByte(int index)
        {
            return TypeUtils.toByte(get(index));
        }

        /**
         * Returns the element at the specified position as a Short value. 
         * @param  index index of the element to return
         * @return the element at the specified position as a Short value
         */
        public Short getShort(int index)
        {
            return TypeUtils.toShort(get(index));
        }

        /**
         * Returns the element at the specified position as a Integer value. 
         * @param  index index of the element to return
         * @return the element at the specified position as a Integer value
         */
        public Integer getInteger(int index)
        {
            return TypeUtils.toInteger(get(index));
        }

        /**
         * Returns the element at the specified position as a Long value. 
         * @param  index index of the element to return
         * @return the element at the specified position as a Long value
         */
        public Long getLong(int index)
        {
            return TypeUtils.toLong(get(index));
        }

        /**
         * Returns the element at the specified position as a BigInteger value. 
         * @param  index index of the element to return
         * @return the element at the specified position as a BigInteger value
         */
        public BigInteger getBigInteger(int index)
        {
            return TypeUtils.toBigInteger(get(index));
        }

        /**
         * Returns the element at the specified position as a Float value. 
         * @param  index index of the element to return
         * @return the element at the specified position as a Float value
         */
        public Float getFloat(int index)
        {
            return TypeUtils.toFloat(get(index));
        }

        /**
         * Returns the element at the specified position as a Double value. 
         * @param  index index of the element to return
         * @return the element at the specified position as a Double value
         */
        public Double getDouble(int index)
        {
            return TypeUtils.toDouble(get(index));
        }

        /**
         * Returns the element at the specified position as a BigDecimal value. 
         * @param  index index of the element to return
         * @return the element at the specified position as a BigDecimal value
         */
        public BigDecimal getBigDecimal(int index)
        {
            return TypeUtils.toBigDecimal(get(index));
        }

        /**
         * Returns the element at the specified position as a Date value. 
         * @param  index index of the element to return
         * @return the element at the specified position as a Date value
         */
        public Date getDate(int index)
        {
            return TypeUtils.toDate(get(index));
        }

        /**
         * Returns the element at the specified position as a Calendar value. 
         * @param  index index of the element to return
         * @return the element at the specified position as a Calendar value
         */
        public Calendar getCalendar(int index)
        {
            return TypeUtils.toCalendar(get(index));
        }

        /**
         * Returns the element at the specified position as a Json.Array value. 
         * @param  index index of the element to return
         * @return the element at the specified position as a Json.Array value
         * @throws ClassCastException if value is not a a Json.Array.
         */
        public Json.Array getArray(int index)
        {
            Serializable value = get(index);
            return (Json.Array)value;
        }

        /**
         * Returns the element at the specified position as a Json.Object value. 
         * @param  index index of the element to return
         * @return the element at the specified position as a Json.Object value
         * @throws ClassCastException if value is not a a Json.Object.
         */
        public Json.Object getObject(int index)
        {
            Serializable value = get(index);
            return (Json.Object)value;
        }

        /**
         * Returns the element at the specified position as a Json container.
         * @param  index index of the element to return
         * @return the element at the specified position as a Json.Object value
         * @throws ClassCastException if value is not a a Json container.
         */
        public Json getJson(int index)
        {
            Serializable value = get(index);
            return (Json)value;
        }

        /**
         * Appender returning self
         * @param elem element to add
         * @return the array
         */
        public Json.Array push(Serializable elem)
        {
            add(elem);
            return this;
        }

        /**
         * Setter returning self (old value is lost)
         * @param elems elements to add to set
         * @return the array
         */
        public Json.Array pushAll(Collection<? extends Serializable> elems)
        {
            addAll(elems);
            return this;
        }

        /**
         * Setter returning self (old value is lost)
         * @param index index of new element
         * @param elem element to set
         * @return the array
         */
        public Json.Array put(int index, Serializable elem)
        {
            set(index, elem);
            return this;
        }

        public java.lang.Object clone()
        {
            Json.Array clone = (Json.Array)super.clone();
            for (int i = 0; i < clone.size(); ++i)
            {
                // we make the assumption that an object is either Json or immutable (so already there)
                Serializable value = get(i);
                if (value instanceof Json)
                {
                    value = (Serializable)((Json)value).clone();
                    clone.put(i, value);
                }
            }
            return clone;
        }

    }

/*****************************************************************
 *
 * Json.Array
 *
 *****************************************************************/

    /**
     * Implements a JSON object
     */
    class Object extends LinkedHashMap<String, Serializable> implements Json, Iterable<Map.Entry<String, Serializable>>
    {
        private static final long serialVersionUID = -8433114857911795160L;

        /**
         * Builds an emepty Json.Object.
         */
        public Object()
        {
        }

        /**
         * Builds an object with the content of an existing Map
         */
        public Object(Map<? extends String, ? extends Serializable> map)
        {
            super(map);
        }

        public Object(Serializable... elements)
        {
            if ((elements.length % 2) != 0)
            {
                throw new IllegalArgumentException("even numbers of arguments expected");
            }
            for (int i = 0; i < elements.length; i += 2)
            {
                if (elements[i] == null || !(elements[i] instanceof String))
                    throw new IllegalArgumentException("odd arguments must be strings");
                put((String) elements[i], elements[i + 1]);
            }
        }

        /**
         * Check if the underlying container is an array.
         *
         * @return true if underlying container is an array, false otherwise
         */
        @Override
        public boolean isArray()
        {
            return false;
        }

        /**
         * Check if the underlying container is an object.
         *
         * @return true if underlying container is an object, false otherwise
         */
        @Override
        public boolean isObject()
        {
            return true;
        }

        /**
         * Check that the underlying container is an array.
         * @throws IllegalStateException otherwise
         */
        @Override
        public void ensureIsArray()
        {
            throw new IllegalStateException("container must be a JSON array");
        }

        /**
         * Check that the underlying container is an object.
         * @throws IllegalStateException otherwise
         */
        @Override
        public void ensureIsObject()
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
                logger.error("could not render Array string", ioe);
                return null;
            }
        }

        /**
         * Returns an iterator over map entries. Equivalent to <code>entrySet().iterator()</code>.
         *
         * @return an Iterator.
         */
        @Override
        public Iterator<Map.Entry<String, Serializable>> iterator()
        {
            return entrySet().iterator();
        }

        /**
         * Performs the given action for each element of the {@code Iterable}
         * until all elements have been processed or the action throws an
         * exception.
         * @param action The action to be performed for each element
         */
        @Override
        public void forEach(Consumer<? super Map.Entry<String, Serializable>> action)
        {
            entrySet().stream().forEach(action);
        }

        /**
         * Creates a {@link Spliterator} over the elements described by this
         * {@code Iterable}.
         * @return a {@code Spliterator} over the elements described by this
         * {@code Iterable}.
         */
        @Override
        public Spliterator<Map.Entry<String, Serializable>> spliterator()
        {
            return Spliterators.spliterator(entrySet(), Spliterator.DISTINCT | Spliterator.SIZED | Spliterator.NONNULL | Spliterator.IMMUTABLE);
        }

        /**
         * Returns the element under the specified key as a String value. 
         * @param  key key of the element to return
         * @return the element under the specified key as a String value or null if the key doesn't exist
         */
        public String getString(String key)
        {
            return TypeUtils.toString(get(key));
        }

        /**
         * Returns the element under the specified key as a Boolean value. 
         * @param  key key of the element to return
         * @return the element under the specified key as a Boolean value or null if the key doesn't exist
         */
        public Boolean getBoolean(String key)
        {
            return TypeUtils.toBoolean(get(key));
        }

        /**
         * Returns the element under the specified key as a Character value. 
         * @param  key key of the element to return
         * @return the element under the specified key as a Character value or null if the key doesn't exist
         */
        public Character getChar(String key)
        {
            return TypeUtils.toChar(get(key));
        }

        /**
         * Returns the element under the specified key as a Byte value. 
         * @param  key key of the element to return
         * @return the element under the specified key as a Byte value or null if the key doesn't exist
         */
        public Byte getByte(String key)
        {
            return TypeUtils.toByte(get(key));
        }

        /**
         * Returns the element under the specified key as a Short value. 
         * @param  key key of the element to return
         * @return the element under the specified key as a Short value or null if the key doesn't exist
         */
        public Short getShort(String key)
        {
            return TypeUtils.toShort(get(key));
        }

        /**
         * Returns the element under the specified key as a Integer value. 
         * @param  key key of the element to return
         * @return the element under the specified key as a Integer value or null if the key doesn't exist
         */
        public Integer getInteger(String key)
        {
            return TypeUtils.toInteger(get(key));
        }

        /**
         * Returns the element under the specified key as a Long value. 
         * @param  key key of the element to return
         * @return the element under the specified key as a Long value or null if the key doesn't exist
         */
        public Long getLong(String key)
        {
            return TypeUtils.toLong(get(key));
        }

        /**
         * Returns the element under the specified key as a BigInteger value. 
         * @param  key key of the element to return
         * @return the element under the specified key as a BigInteger value or null if the key doesn't exist
         */
        public BigInteger getBigInteger(String key)
        {
            return TypeUtils.toBigInteger(get(key));
        }

        /**
         * Returns the element under the specified key as a Float value. 
         * @param  key key of the element to return
         * @return the element under the specified key as a Float value or null if the key doesn't exist
         */
        public Float getFloat(String key)
        {
            return TypeUtils.toFloat(get(key));
        }

        /**
         * Returns the element under the specified key as a Double value. 
         * @param  key key of the element to return
         * @return the element under the specified key as a Double value or null if the key doesn't exist
         */
        public Double getDouble(String key)
        {
            return TypeUtils.toDouble(get(key));
        }

        /**
         * Returns the element under the specified key as a BigDecimal value. 
         * @param  key key of the element to return
         * @return the element under the specified key as a BigDecimal value or null if the key doesn't exist
         */
        public BigDecimal getBigDecimal(String key)
        {
            return TypeUtils.toBigDecimal(get(key));
        }

        /**
         * Returns the element under the specified key as a Date value. 
         * @param  key key of the element to return
         * @return the element under the specified key as a Date value or null if the key doesn't exist
         */
        public Date getDate(String key)
        {
            return TypeUtils.toDate(get(key));
        }

        /**
         * Returns the element under the specified key as a Calendar value. 
         * @param  key key of the element to return
         * @return the element under the specified key as a Calendar value or null if the key doesn't exist
         */
        public Calendar getCalendar(String key)
        {
            return TypeUtils.toCalendar(get(key));
        }

        /**
         * Returns the element under the specified key as a Json.Array value. 
         * @param  key key of the element to return
         * @return the element under the specified key as a Json.Array value or null if the key doesn't exist
         * @throws ClassCastException if value is not a a Jon.Array.
         */
        public Json.Array getArray(String key)
        {
            Serializable value = get(key);
            return (Json.Array)value;
        }

        /**
         * Returns the element under the specified key as a Json.Object value. 
         * @param  key key of the element to return
         * @return the element under the specified key as a Json.Object value or null if the key doesn't exist
         * @throws ClassCastException if value is not a a Jon.Object.
         */
        public Json.Object getObject(String key)
        {
            Serializable value = get(key);
            return (Json.Object)value;
        }

        /**
         * Returns the element under the specified key as a Json container.
         * @param  key key of the element to return
         * @return the element at the specified position as a Json.Object value
         * @throws ClassCastException if value is not a a Json container.
         */
        public Json getJson(String key)
        {
            Serializable value = get(key);
            return (Json)value;
        }

        /**
         * Setter returning self (old value, if any, is lost)
         * @param key of new element
         * @param elem element to set
         * @return the object
         */
        public Json.Object set(String key, Serializable elem)
        {
            put(key, elem);
            return this;
        }

        /**
         * Setter returning self
         * @param elems elements to add
         * @return the object
         */
        public Json.Object setAll(Map<? extends String, ? extends Serializable> elems)
        {
            putAll(elems);
            return this;
        }


        public java.lang.Object clone()
        {
            Json.Object clone = (Json.Object)super.clone();
            for (Map.Entry<String, Serializable> entry : entrySet())
            {
                Serializable value = entry.getValue();
                if (value instanceof Json)
                {
                    value = (Serializable)((Json)value).clone();
                    entry.setValue(value);
                }
            }
            return clone;
        }

    }

/*****************************************************************
 *
 * Serializer
 *
     *****************************************************************/

    /**
     * The Serializer class gathers static methods for JSON containers serialization.
     */
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
         * @throws IOException if escaping fails
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
         * @throws IOException if serialization fails
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

/*****************************************************************
 *
 * Parser
 *
 *****************************************************************/

    /**
     * JSON parser.
     */
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
            /*
              We need a reader that has an internal buffer otherwise read() calls
              are gonna become a performance bottleneck. The markSuported() method
              is a good indicator.
             */
            if (reader.markSupported())
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

        private Array parseArray() throws IOException
        {
            Array ret = new Array();
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

        private Object parseObject() throws IOException
        {
            Object ret = new Object();
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
                    else if (ch == -1)
                    {
                        throw error("unterminated string");
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
                number = Long.valueOf(negative ? negValue : -negValue);
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

/*****************************************************************
 *
 * Helpers
 *
 *****************************************************************/

    /**
     * Like a StringReader, but without any synchronization lock.
     */
    class FastStringReader extends Reader
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
        public boolean markSupported()
        {
            // That's an obvious lie. We don't. See Parser.Parser.parse(Reader) to see why.
            return true;
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

    /**
     * Conversion helpers
     */
    class TypeUtils
    {
        private static String toString(java.lang.Object value)
        {
            return value == null ? null : value.toString();
        }

        private static Character toChar(java.lang.Object value)
        {
            if (value == null)
            {
                return null;
            }
            if (value instanceof Character)
            {
                return (Character)value;
            }
            if (value instanceof Boolean)
            {
                return ((Boolean)value).booleanValue()
                    ? 't'
                    : 'f';
            }
            if (value instanceof String && ((String) value).length() == 1)
            {
                return ((String)value).charAt(0);
            }
            return null;
        }

        private static Boolean toBoolean(java.lang.Object value)
        {
            if (value == null)
            {
                return null;
            }
            if (value instanceof Boolean)
            {
                return (Boolean)value;
            }
            if (value instanceof String)
            {
                String str = (String)value;
                if ("true".equals(str))
                {
                    return true;
                }
                if ("false".equals(str))
                {
                    return false;
                }
                try
                {
                    value = Long.valueOf(str);
                }
                catch (NumberFormatException nfe)
                {
                    return false;
                }
            }
            if (value instanceof Number)
            {
                return ((Number)value).longValue() != 0l;
            }
            return false;
        }

        private static Byte toByte(java.lang.Object value)
        {
            if (value == null)
            {
                return null;
            }
            if (value instanceof Number)
            {
                return ((Number)value).byteValue();
            }
            if (value instanceof String)
            {
                try
                {
                    return Byte.valueOf((String)value);
                }
                catch (NumberFormatException nfe)
                {
                }
            }
            return null;
        }


        private static Short toShort(java.lang.Object value)
        {
            if (value == null)
            {
                return null;
            }
            if (value instanceof Number)
            {
                return ((Number)value).shortValue();
            }
            if (value instanceof String)
            {
                try
                {
                    return Short.valueOf((String)value);
                }
                catch (NumberFormatException nfe)
                {
                }
            }
            return null;
        }

        private static Integer toInteger(java.lang.Object value)
        {
            if (value == null)
            {
                return null;
            }
            if (value instanceof Number)
            {
                return ((Number)value).intValue();
            }
            if (value instanceof String)
            {
                try
                {
                    return Integer.valueOf((String)value);
                }
                catch (NumberFormatException nfe)
                {
                }
            }
            return null;
        }

        private static Long toLong(java.lang.Object value)
        {
            if (value == null)
            {
                return null;
            }
            if (value instanceof Number)
            {
                return ((Number)value).longValue();
            }
            if (value instanceof String)
            {
                try
                {
                    return Long.valueOf((String)value);
                }
                catch (NumberFormatException nfe)
                {
                }
            }
            return null;
        }

        private static BigInteger toBigInteger(java.lang.Object value)
        {
            if (value == null)
            {
                return null;
            }
            if (value instanceof BigInteger)
            {
                return ((BigInteger)value);
            }
            if (value instanceof Number)
            {
                return BigInteger.valueOf(((Number)value).longValue());
            }
            if (value instanceof String)
            {
                return new BigInteger((String) value);
            }
            return null;
        }
        
        private static Float toFloat(java.lang.Object value)
        {
            if (value == null)
            {
                return null;
            }
            if (value instanceof Number)
            {
                return ((Number)value).floatValue();
            }
            if (value instanceof String)
            {
                try
                {
                    return Float.valueOf((String)value);
                }
                catch (NumberFormatException nfe)
                {
                }
            }
            return null;
        }

        private static Double toDouble(java.lang.Object value)
        {
            if (value == null)
            {
                return null;
            }
            if (value instanceof Number)
            {
                return ((Number)value).doubleValue();
            }
            if (value instanceof String)
            {
                try
                {
                    return Double.valueOf((String)value);
                }
                catch (NumberFormatException nfe)
                {
                }
            }
            return null;
        }

        private static BigDecimal toBigDecimal(java.lang.Object value)
        {
            if (value == null)
            {
                return null;
            }
            if (value instanceof BigDecimal)
            {
                return ((BigDecimal)value);
            }
            if (value instanceof Number)
            {
                return BigDecimal.valueOf(((Number)value).doubleValue());
            }
            if (value instanceof String)
            {
                return new BigDecimal((String) value);
            }
            return null;
        }

        private static Date toDate(java.lang.Object value)
        {
            if (value == null || value instanceof Date)
            {
                return (Date)value;
            }
            if (value instanceof Calendar)
            {
                return ((Calendar)value).getTime();
            }
            return null;
        }

        private static Calendar toCalendar(java.lang.Object value)
        {
            if (value == null || value instanceof Calendar)
            {
                return (Calendar)value;
            }
            if (value instanceof Date)
            {
                // CB TODO - use model locale
                Calendar calendar = GregorianCalendar.getInstance();
                calendar.setTime((Date)value);
                return calendar;
            }
            return null;
        }

        private static byte[] toBytes(java.lang.Object value)
        {
            if (value == null || value instanceof byte[])
            {
                return (byte[])value;
            }
            return String.valueOf(value).getBytes(StandardCharsets.UTF_8);
        }
    }
}
