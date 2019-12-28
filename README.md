# Essential Json

## Rationale

Although there are already tons of existing [Java JSON libraries](https://gitlab.renegat.net/claude/yajb), I needed one which would:

+ be **minimalistic**

  + no validation, no schema, no custom POJO field/class support
  + easily pluggable in other projects: one single source file

+ be **performant**
+ avoid any kind of abstraction other than **Serializable** around values, without any wrapping
+ use a common **parent Serializable interface** for JSON objects and arrays

## Usage



### Maven dependency

    <dependency>
        <groupId>com.republicate.json</groupId>
        <artifactId>essential-json</artifactId>
        <version>1.0</version>
    </dependency>

### Parsing json


    import com.republicate.json.Json;

    ...

    Json container = Json.parse(string_or_reader)
    // container will be a JSON object or a JSON array

    Serializable value = Json.parseValue(string__or_reader)
    // value will either be a JSON container or a single value

## Rendering json

    import com.republicate.json.Json;

    ...

    container.toString(writer)

## Building json

    import com.republicate.json.Json;
    import com.republicate.json.Json.JsonObject;
    import com.republicate.json.Json.JsonArray;
    
    ...
    
    JsonArray arr = Json.newJsonArray();
    arr.add(4);
    arr.add(5);
    JsonObject obj = Json.newJsonObject();
    obj.put("foo", "bar");
    obj.put("baz", arr);


### References

+ [RFC 7159](https://tools.ietf.org/html/rfc7159)
+ [JSON Parsing Test Suite](https://github.com/nst/JSONTestSuite)


