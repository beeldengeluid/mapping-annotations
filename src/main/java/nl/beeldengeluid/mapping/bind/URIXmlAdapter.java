package nl.beeldengeluid.mapping.bind;

import java.net.URI;

import jakarta.xml.bind.annotation.adapters.XmlAdapter;

public class URIXmlAdapter extends XmlAdapter<String, URI> {
    @Override
    public URI unmarshal(String v) {
        return URI.create(v);
    }

    @Override
    public String marshal(URI v) {
        return v.toString();
    }
}
