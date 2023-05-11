package org.example;
import javax.json.Json;
import javax.json.JsonArray;
import javax.json.JsonObject;
import javax.json.JsonReader;
import javax.json.JsonWriter;
import javax.json.stream.JsonCollectors;
import java.io.StringReader;
import java.io.StringWriter;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class Jsonizer {

    public static JsonObject jsonStringToObject(String jsonString) {
        JsonReader reader = Json.createReader(new StringReader(jsonString));
        JsonObject jsonObject = reader.readObject();
        reader.close();
        return jsonObject;
    }

    public static String jsonObjectToString(JsonObject jsonObject) {
        StringWriter writer = new StringWriter();
        JsonWriter jsonWriter = Json.createWriter(writer);
        jsonWriter.writeObject(jsonObject);
        jsonWriter.close();
        return writer.toString();
    }

    public static String[] jsonArrayToStringArray(JsonArray jsonArray) {
        return jsonArray.getValuesAs(JsonObject.class)
                .stream()
                .map(JsonObject::toString)
                .toArray(String[]::new);
    }

    public static JsonArray stringArrayToJsonArray(String[] stringArray) {
        Stream<JsonArray> jsonArrayStream = Stream.of(stringArray)
                .map(Jsonizer::jsonStringToObject)
                .map(jsonObject -> Json.createArrayBuilder().add(jsonObject).build());

        return jsonArrayStream.collect(JsonCollectors.toJsonArray());
    }
}