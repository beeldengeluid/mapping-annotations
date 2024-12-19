package nl.beeldengeluid.mapping;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class ExtendedSourceObject extends SourceObject{

    SubSourceObject subObject;
}
