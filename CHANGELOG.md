# 2.6.2

Maintenance release.

+ fix `toPrettyString` methods formatting

# 2.6.1

Maintenance release.

+ fix `Json.Object.getJson()` signature.

# 2.6

+ added `Json.toJson(java.lang.Object)` method, which converts Java standard containers to Json

# 2.5

+ added `Array.pushAll(collection)` and `Object.setAll(collection)`, returning self

# 2.4

+ added Cloneable support (deep cloning)
+ removed an extra `\n` in empty arrays pretty output
+ added facility methods `Json.newArray(Serializable...)` and `Json.newObject(Serializable...)`
+ added `getJson(...)` specialized getters returning Json containers
+ added `Array.push(value)`, `Array.put(index, value)` and `Object.set(key, value)`, returning self

# 2.3

Maintenance release.

+ fix a parsing bug.

# 2.2

+ added `Json.Array(Serializable...)` constructor
+ added `Json.escape(String)` commodity method

#2.1

Maintenance release.

+ fix a bug in the parsing of negative longs.

# 2.0

+ renamed container classes to `Json.Array` and `Json.Object`
+ added specialized getters

# 1.0

Initial release.
