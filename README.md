# Essential JSON

## Rationale

Although there are already tons of existing [Java JSON libraries](https://gitlab.renegat.net/claude/yajb), I needed one which would:

+ be **minimalistic**

  + **no reflection** of any sort, no validation, no schema, no custom POJO field/class support
  + **no external dependency**
  + easily pluggable in other projects: **one single source file**

+ be **performant**
+ have a nice and handy API with specialized getters and setters
+ avoid any kind of abstraction other than **Serializable** around values, without any wrapping
+ use a common **parent Serializable interface** for JSON objects and arrays

## Description

## Usage

The `com.republicate.json.Json` interface extends `Serializable` and is implemented by its two inner classes `Json.Array` and `Json.Object`.

`Json.Array` extends `List<Serializable>` and `Json.Object` extends `Map<String, Serializable>`.

### Inclusion in your project

Using Maven:

    <dependency>
        <groupId>com.republicate</groupId>
        <artifactId>essential-json</artifactId>
        <version>2.5</version>
    </dependency>

Using Gradle:

    implementation 'com.republicate:essential-json:2.3'

### Parsing JSON

The generic `Json.parse(string_or_reader)` method will return a `com.republicate.Json` value containing a `Json.Object` or `Json.Array` object.

If you want to parse a content without knowing if it's a JSON container or a simple JSON value,
you will call the `Json.parseValue(string_or_reader)` method to get a `Serializable`. 

    import com.republicate.json.Json;
    ...
    Json container = Json.parse(string_or_reader);
    // container will be a JSON object or a JSON array
    if (container.isObject())
    {
        Json.Object obj = container.asObject();
        ...
    }

    Serializable value = Json.parseValue(string_or_reader);
    // value will either be a JSON container or a single Serializable value

### Rendering JSON

Containers `toString()` and `toString(Writer)` methods will render JSON strings with proper quoting and encoding.

    import com.republicate.json.Json;
    ...
    // getting a String
    String json = container.toString();

    // rendering towards a Writer
    container.toString(writer);

### Building JSON

`Json.Array` and `Json.Object` constructors (or equivalents `Json.newArray()` and `Json.newObject()` helper methods) can respectively be given an existing Iterable or an existing Map ; both can also be given a JSON string.

Both containers have specialized getters (`getString`, `getBoolean`, etc.).

`Json.Array` has helper methods `push`, `pushAll` and `put` that return self (and rely on the standard `add`, `addAll` and `set` ArrayList methods).

`Json.Object` has helper methods `set` and `setAll` that return self (and rely on the standard `put` and `putAll` Map methods). 

    import com.republicate.json.Json;
    ...
    Json.Array arr = Json.newArray("[1,2,3]").add(4).add(5);
    Json.Object obj = new Json.Object(some_existing_map).set("foo", "bar").set(("baz", arr);

### Converting to JSON

The two reentrant methods `Json.toJson(java.lang.Object)` and `Json.toSerializable(java.lang.Object)` will try hard to convert any standard Java container to a JSON structure.

## References

+ [RFC 7159](https://tools.ietf.org/html/rfc7159)
+ [JSON Parsing Test Suite](https://github.com/nst/JSONTestSuite)
