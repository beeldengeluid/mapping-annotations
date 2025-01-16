/**
 * See {@link Mapper}
 */
module nl.beeldengeluid.mapping.annotations {
    requires static lombok;

    requires transitive org.slf4j;
    requires transitive com.fasterxml.jackson.databind;
    requires transitive jakarta.xml.bind;

    requires json.path;
    requires org.meeuw.functional;

    exports nl.beeldengeluid.mapping.annotations;
    exports nl.beeldengeluid.mapping.bind;
    exports nl.beeldengeluid.mapping;

}
