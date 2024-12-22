package nl.beeldengeluid.mapping.sources;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class ExtendedSourceObject extends SourceObject {

    SubSourceObject subObject;
}
