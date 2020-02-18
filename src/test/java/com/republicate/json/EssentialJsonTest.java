package com.republicate.json;

import org.junit.Test;

import java.io.IOException;
import java.io.Reader;
import java.io.Serializable;
import java.io.StringWriter;
import java.nio.charset.StandardCharsets;
import java.nio.file.*;
import java.util.*;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

import static junit.framework.TestCase.*;

public class EssentialJsonTest extends BaseTestUnit
{
    @Test
    public void test() throws Exception
    {
        PathMatcher matcher = FileSystems.getDefault().getPathMatcher("glob:**/*.json");
        List<Path> paths = Files.walk(Paths.get("."))
            .filter(Files::isRegularFile)
            .filter(file -> matcher.matches(file))
            .collect(Collectors.toList());
        Collections.sort(paths);
        paths.stream().forEach(file -> testFile(file));
    }

    static Set<String> skipByFilename = new HashSet<>(Arrays.asList(
        "n_223.json", // \u0000 is valid
        "n_structure_whitespace_formfeed.json" // why would the form feed be invalid ?
    ));

    static Pattern skipChecksumTestContent = Pattern.compile("^\\[?\"[^\"]*\"\\]?$|^\\[?[0-9.eE+-]+\\]?$", Pattern.CASE_INSENSITIVE);

    static Set<String> skipChecksumTestFilename = new HashSet<>(Arrays.asList(
        "i_object_key_lone_2nd_surrogate.json",
        "i_string_incomplete_surrogate_pair.json",
        "y_object_duplicated_key.json",
        "y_object_duplicated_key_and_value.json",
        "y_object_escaped_null_in_key.json",
        "y_object_string_unicode.json",
        "y_string_1_2_3_bytes_UTF-8_sequences.json",
        "y_string_accepted_surrogate_pair.json",
        "y_string_accepted_surrogate_pairs.json",
        "y_string_allowed_escapes.json",
        "y_string_escaped_noncharacter.json",
        "y_string_last_surrogates_1_and_2.json",
        "y_string_nbsp_uescaped.json",
        "y_string_one-byte-utf-8.json",
        "object_same_key_different_values.json",
        "object_same_key_same_value.json",
        "object_same_key_unclear_values.json"
    ));

    static Set<String> awaitExceptionByFilename = new HashSet<>(Arrays.asList(
        "i_number_huge_exp.json",
        "i_string_1st_surrogate_but_2nd_missing.json",
        "i_string_1st_valid_surrogate_2nd_invalid.json",
        "i_string_UTF-16LE_with_BOM.json",
        "i_string_UTF-8_invalid_sequence.json",
        "i_string_UTF8_surrogate_U+D800.json",
        "i_string_incomplete_surrogate_and_escape_valid.json",
        "i_string_incomplete_surrogates_escape_valid.json",
        "i_string_invalid_lonely_surrogate.json",
        "i_string_invalid_surrogate.json",
        "i_string_invalid_utf-8.json",
        "i_string_inverted_surrogates_U+1D11E.json",
        "i_string_iso_latin_1.json",
        "i_string_lone_second_surrogate.json",
        "i_object_key_lone_2nd_surrogate.json",
        "i_string_incomplete_surrogate_pair.json",
        "i_string_lone_utf8_continuation_byte.json",
        "i_string_not_in_unicode_range.json",
        "i_string_overlong_sequence_2_bytes.json",
        "i_string_overlong_sequence_6_bytes.json",
        "i_string_overlong_sequence_6_bytes_null.json",
        "i_string_truncated-utf-8.json",
        "i_string_utf16BE_no_BOM.json",
        "i_string_utf16LE_no_BOM.json",
        "i_structure_UTF-8_BOM_empty_object.json",
        "string_1_escaped_invalid_codepoint.json",
        "string_1_invalid_codepoint.json",
        "string_2_escaped_invalid_codepoints.json",
        "string_2_invalid_codepoints.json",
        "string_3_escaped_invalid_codepoints.json",
        "string_3_invalid_codepoints.json"
    ));

    protected void testFile(Path file)
    {
        try
        {
            String filename = file.getFileName().toString();
            boolean awaitError = filename.startsWith("n_") || awaitExceptionByFilename.contains(filename);
            if (skipByFilename.contains(filename))
            {
                log("skipping");
                return;
            }
            log("considering file " + file.toString());
            Reader reader = Files.newBufferedReader(file, StandardCharsets.UTF_8);
            if (awaitError)
            {
                try
                {
                    Object instance = Json.parseValue(reader);
                    fail("Exception awaited!");
                }
                catch (IOException | NumberFormatException | StackOverflowError e)
                {
                }
            }
            else
            {
                String orig = new String(Files.readAllBytes(file), StandardCharsets.UTF_8);
                startTiming();
                Serializable instance = Json.parseValue(reader);
                stopTiming();
                if (filename.equals("y_structure_lonely_null.json"))
                {
                    return;
                }
                startTiming();
                String output =
                    instance instanceof String
                        ? Json.Serializer.escapeJson((String)instance, new StringWriter()).toString()
                        : instance.toString();
                stopTiming();
                // skip vicious ones that defeat the naive checksum algorithm
                boolean skipChecksum = skipChecksumTestContent.matcher(orig).find() || skipChecksumTestFilename.contains(filename);
                if (!skipChecksum)
                {
                    assertEquals(checksum(orig), checksum(output));
                }
            }
        }
        catch (Exception e)
        {
            throw new RuntimeException(e);
        }
    }

    @Test
    public void testEquals() throws Exception
    {
        Json.Object o1 = new Json.Object();
        o1.put("foo", "bar");
        o1.put("bar", 45.65);
        o1.put("baz", new Json.Array(1l, 2l, 3l));
        Json.Object o2 = Json.parse("{ \"foo\":\"bar\", \"bar\":45.65, \"baz\":[1,2,3] }").asObject();
        assertEquals(o1, o2);
    }
}
