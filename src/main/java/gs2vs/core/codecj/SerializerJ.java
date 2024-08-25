package gs2vs.core.codecj;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.msgpack.jackson.dataformat.MessagePackFactory;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class SerializerJ {
    public static final Kingdom defaultKingdom = new Kingdom(1111L);

    public static void main(String[] args) throws IOException {
        ArrayList<Item> items = new ArrayList<>();
        items.add(new Item(2L, "name01"));

        MyState mystate = new MyState(null, new Kingdom(1L), items);
        ObjectMapper objectMapper = new ObjectMapper(new MessagePackFactory());
        ObjectMapper objectMapperJson = new ObjectMapper();
        objectMapperJson.setSerializationInclusion(JsonInclude.Include.NON_DEFAULT);
//        objectMapperJson.setSerializationInclusion(JsonInclude.Include.NON_EMPTY);

        byte[] bytes = objectMapper.writeValueAsBytes(mystate);

//        MyState myState1 = objectMapper.readValue(bytes, MyStateV2.class);
//        System.out.println("myState1: " + myState2);
// =====  OUTPUT: myState1: MyState[name=name01, kingdom=Kingdom[id=1], items=[Item[id=2, name=name01]]]

        // append field with null field
        MyStateV2 myState2 = objectMapper.readValue(bytes, MyStateV2.class);
        System.out.println("myStateV2: " + myState2);
// ===== OUTPUT: myStateV2: MyStateV2[name=name01, kingdom=Kingdom[id=1], items=[Item[id=2, name=name01]], kingdom2=null]

//        MyStateV3 myState3 = objectMapper.readValue(bytes, MyStateV3.class);
//        System.out.println("myState3: " + myState3);
        // ===== OUTPUT: Exception in thread "main" com.fasterxml.jackson.databind.exc.UnrecognizedPropertyException: Unrecognized field "items" (class gs2vs.core.codecj.SerializerJ$MyStateV3), not marked as ignorable (3 known properties: "name", "kingdom2", "kingdom"])
        // at [Source: (byte[])[50 bytes]; byte offset: #50] (through reference chain: gs2vs.core.codecj.SerializerJ$MyStateV3["items"])

//        MyStateV4 myState4 = objectMapper.readValue(bytes, MyStateV4.class);
//        System.out.println("myState4: " + myState4);
// ===== OUTPUT: myState4: MyStateV4[items=[Item[id=2, name=name01]], name=name01, kingdom=Kingdom[id=1]]

        PojoState pojoState1 = objectMapper.readValue(bytes, PojoState.class);
        String jsonPojoState1 = objectMapperJson.writeValueAsString(pojoState1);
        System.out.println("pojoState1: " + pojoState1);
        System.out.println("pojoState1 json string: " + jsonPojoState1);
// ===== OUTPUT:

    }

    public record MyState(String name, Kingdom kingdom, List<Item> items) {
    }

    public record MyStateV2(
//            @JsonProperty(defaultValue = "asd") not work
            String name, Kingdom kingdom, List<Item> items, Kingdom kingdom2) {
    }

    public record MyStateV3(String name, Kingdom kingdom, Kingdom kingdom2) {
    }

    public record MyStateV4(List<Item> items, String name, Kingdom kingdom) {
    }

    public record Kingdom(Long id) {
    }

    public record Item(Long id, String name) {
    }

    public record ItemV2(Long id, String name) {
    }


    public static class PojoState {
        public String name;
        public Kingdom kingdom;
        public List<Item> items;
        public Kingdom kingdom2 = defaultKingdom;
        public Kingdom kingdom3 = null;// new Kingdom(0L);
    }
}
