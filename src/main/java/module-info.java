/**
 * See {@link Mapper}
 */
module org.meeuw.mapping.annotations {
    requires static lombok;
    requires transitive org.slf4j;

    requires transitive com.fasterxml.jackson.databind;
    requires json.path;
    requires transitive jakarta.xml.bind;
    requires org.meeuw.functional;

    exports nl.beeldengeluid.mapping.annotations;
    exports nl.beeldengeluid.mapping;
}
